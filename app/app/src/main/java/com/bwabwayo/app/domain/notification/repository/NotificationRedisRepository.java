package com.bwabwayo.app.domain.notification.repository;

import com.bwabwayo.app.domain.notification.dto.NotificationDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Repository
public class NotificationRedisRepository {

    private final RedisTemplate<String, String> template;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public NotificationRedisRepository(@Qualifier("notificationRedisTemplate")
                                       RedisTemplate<String, String> template) {
        this.template = template;
    }

    private String uniqKey(String receiverId, Long productId, Long roomId) {
        long p = (productId == null ? 0L : productId);
        long r = (roomId == null ? 0L : roomId);
        return String.format("notif:idx:uniq:%s:%d:%d", receiverId, p, r);
    }

    private String chatIdxKey(String receiverId, Long roomId) {
        return String.format("notif:idx:chat:%s:%d", receiverId, roomId);
    }

    private String prodIdxKey(String receiverId, Long productId) {
        return String.format("notif:idx:prod:%s:%d", receiverId, productId);
    }

    private String inboxKey(String receiverId) {
        return String.format("notif:inbox:%s", receiverId);
    }

    private String notifKey(long id) {
        return String.format("notif:%d", id);
    }

    /** 업서트 (WATCH/MULTI/EXEC) — 같은 커넥션 보장 */
    public void upsert(String receiverId, Long productId, Long roomId,
                       String message, LocalDateTime updatedAtKst) {

        final String uniq = uniqKey(receiverId, productId, roomId);
        final long nowMillis = updatedAtKst.atZone(KST).toInstant().toEpochMilli();

        template.execute(new SessionCallback<Void>() {
            @Override
            public <K, V> Void execute(RedisOperations<K, V> operations) throws DataAccessException {
                @SuppressWarnings("unchecked")
                RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;

                while (true) {
                    // 1) WATCH + 조회 (같은 커넥션)
                    ops.watch(uniq);
                    String existedId = ops.opsForValue().get(uniq);

                    if (existedId != null) {
                        String nkey = notifKey(Long.parseLong(existedId));

                        // 2) MULTI
                        ops.multi();
                        ops.opsForHash().put(nkey, "message", message);
                        ops.opsForHash().put(nkey, "updatedAtMillis", String.valueOf(nowMillis));
                        ops.opsForHash().put(nkey, "isRead", "false");
                        ops.opsForHash().increment(nkey, "unreadCount", 1);
                        ops.opsForZSet().add(inboxKey(receiverId), existedId, nowMillis);

                        // 3) EXEC
                        List<Object> exec = ops.exec();
                        if (exec != null) break; // 성공. 충돌이면 null → 루프 재시도
                    } else {
                        Long id = ops.opsForValue().increment("notif:seq");
                        String nkey = notifKey(Objects.requireNonNull(id));

                        ops.multi();
                        Map<String, String> body = new LinkedHashMap<>();
                        body.put("id", String.valueOf(id));
                        body.put("receiverId", receiverId);
                        body.put("productId", String.valueOf(productId == null ? 0L : productId));
                        body.put("chatroomId", String.valueOf(roomId == null ? 0L : roomId));
                        body.put("message", message);
                        body.put("updatedAtMillis", String.valueOf(nowMillis));
                        body.put("isRead", "false");
                        body.put("unreadCount", "1");

                        ops.opsForHash().putAll(nkey, body);
                        ops.opsForValue().set(uniq, String.valueOf(id));
                        if (roomId != null)    ops.opsForValue().set(chatIdxKey(receiverId, roomId), String.valueOf(id));
                        if (productId != null) ops.opsForValue().set(prodIdxKey(receiverId, productId), String.valueOf(id));
                        ops.opsForZSet().add(inboxKey(receiverId), String.valueOf(id), nowMillis);

                        List<Object> exec = ops.exec();
                        if (exec != null) break;
                    }
                }
                return null;
            }
        });
    }

    /** 단건 읽음 처리 — MULTI/EXEC (WATCH 불필요) */
    public void markRead(String receiverId, long notificationId) {
        final String nkey = notifKey(notificationId);
        final String zkey = inboxKey(receiverId);

        template.execute(new SessionCallback<Void>() {
            @Override
            public <K, V> Void execute(RedisOperations<K, V> operations) throws DataAccessException {
                @SuppressWarnings("unchecked")
                RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;

                ops.multi();
                ops.opsForHash().put(nkey, "isRead", "true");
                ops.opsForHash().put(nkey, "unreadCount", "0");
                ops.opsForZSet().remove(zkey, String.valueOf(notificationId));
                ops.exec();
                return null;
            }
        });
    }

    /** 채팅 읽음 처리 by (receiverId, roomId) */
    public void markChatRead(String receiverId, long roomId) {
        String idStr = template.opsForValue().get(chatIdxKey(receiverId, roomId));
        if (idStr != null) {
            markRead(receiverId, Long.parseLong(idStr));
        }
    }

    /** 상품 알림 읽음 처리 by (receiverId, productId) */
    public void markProductRead(String receiverId, long productId) {
        String idStr = template.opsForValue().get(prodIdxKey(receiverId, productId));
        if (idStr != null) {
            markRead(receiverId, Long.parseLong(idStr));
        }
    }

    /** 인박스 페이지 조회 (미읽음만 최신순) */
    public Page<NotificationDTO> findInbox(String receiverId, Pageable pageable) {
        String zkey = inboxKey(receiverId);
        long start = pageable.getOffset();
        long end = start + pageable.getPageSize() - 1;

        Long total = template.opsForZSet().zCard(zkey);
        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        Set<String> ids = template.opsForZSet().reverseRange(zkey, start, end);
        if (ids == null || ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        List<NotificationDTO> list = ids.stream()
                .map(idStr -> {
                    String nkey = notifKey(Long.parseLong(idStr));
                    Map<Object, Object> m = template.opsForHash().entries(nkey);
                    if (m == null || m.isEmpty()) return null;

                    long updatedAtMillis = Long.parseLong((String)m.getOrDefault("updatedAtMillis","0"));
                    LocalDateTime kst = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(updatedAtMillis), KST);

                    return NotificationDTO.builder()
                            .id(Long.valueOf((String)m.get("id")))
                            .receiverId((String)m.get("receiverId"))
                            .productId(parseLongNull((String)m.get("productId")))
                            .chatroomId(parseLongNull((String)m.get("chatroomId")))
                            .message((String)m.get("message"))
                            .updatedAt(kst)
                            .isRead(Boolean.parseBoolean((String)m.getOrDefault("isRead","false")))
                            .unreadCount(Integer.parseInt((String)m.getOrDefault("unreadCount","0")))
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(list, pageable, total);
    }

    private Long parseLongNull(String s) {
        if (s == null) return null;
        long v = Long.parseLong(s);
        return v == 0L ? null : v;
    }
}
