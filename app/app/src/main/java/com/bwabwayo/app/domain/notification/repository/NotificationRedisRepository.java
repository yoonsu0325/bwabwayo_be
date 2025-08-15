package com.bwabwayo.app.domain.notification.repository;

import com.bwabwayo.app.domain.notification.dto.NotificationDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class NotificationRedisRepository {

    @Qualifier("notificationRedisTemplate")
    private final RedisTemplate<String, String> template;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");


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

    /** ID 생성 */
    private long nextId() {
        return template.opsForValue().increment("notif:seq");
    }

    /** 업서트 (Lua 없이 WATCH/MULTI로 원자성 확보) */
    @Transactional // 트랜잭션 경계는 Redis에 영향 X, Spring AOP 구간 표시용
    public void upsert(String receiverId, Long productId, Long roomId,
                       String message, LocalDateTime updatedAtKst) {

        String uniq = uniqKey(receiverId, productId, roomId);
        long nowMillis = updatedAtKst.atZone(KST).toInstant().toEpochMilli();

        // 비관적보다 가벼운 낙관적 잠금
        template.setEnableTransactionSupport(true);
        while (true) {
            template.watch(uniq);
            String existedId = template.opsForValue().get(uniq);

            List<Object> exec;
            if (existedId != null) {
                String nkey = notifKey(Long.parseLong(existedId));
                template.multi(); // TX 시작

                // 본문 갱신
                Map<String, String> patch = new HashMap<>();
                patch.put("message", message);
                patch.put("updatedAtMillis", String.valueOf(nowMillis));
                patch.put("isRead", "false");
                // unreadCount + 1
                // Hash에선 HINCRBY
                template.opsForHash().increment(nkey, "unreadCount", 1);

                template.opsForHash().putAll(nkey, patch);

                // 인박스 정렬 최신화
                template.opsForZSet().add(inboxKey(receiverId), existedId, nowMillis);

                exec = template.exec(); // 커밋
            } else {
                long id = nextId();
                String nkey = notifKey(id);
                template.multi();

                Map<String, String> body = new LinkedHashMap<>();
                body.put("id", String.valueOf(id));
                body.put("receiverId", receiverId);
                body.put("productId", String.valueOf(productId == null ? 0L : productId));
                body.put("chatroomId", String.valueOf(roomId == null ? 0L : roomId));
                body.put("message", message);
                body.put("updatedAtMillis", String.valueOf(nowMillis));
                body.put("isRead", "false");
                body.put("unreadCount", "1");

                template.opsForHash().putAll(nkey, body);

                // 유니크/보조 인덱스
                template.opsForValue().set(uniq, String.valueOf(id));
                if (roomId != null)    template.opsForValue().set(chatIdxKey(receiverId, roomId), String.valueOf(id));
                if (productId != null) template.opsForValue().set(prodIdxKey(receiverId, productId), String.valueOf(id));

                // 인박스 ZSET
                template.opsForZSet().add(inboxKey(receiverId), String.valueOf(id), nowMillis);

                exec = template.exec();
            }

            if (exec != null) break; // WATCH 충돌 없었으면 성공
        }
        template.setEnableTransactionSupport(false);
    }

    /** 단건 읽음 처리 by notificationId */
    public void markRead(String receiverId, long notificationId) {
        String nkey = notifKey(notificationId);
        // 상태 읽고 미읽음이면 인박스에서 제거
        List<Object> vals = template.opsForHash().multiGet(nkey, List.of("isRead", "unreadCount"));
        if (vals == null || vals.isEmpty()) return;

        template.setEnableTransactionSupport(true);
        template.multi();
        template.opsForHash().put(nkey, "isRead", "true");
        template.opsForHash().put(nkey, "unreadCount", "0");
        template.opsForZSet().remove(inboxKey(receiverId), String.valueOf(notificationId));
        template.exec();
        template.setEnableTransactionSupport(false);
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

        // 총 개수(미읽음 건)
        Long total = template.opsForZSet().zCard(zkey);
        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 최신순 범위
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
                LocalDateTime kst = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(updatedAtMillis), KST);

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
