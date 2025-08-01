package com.bwabwayo.app.domain.support.service;

import com.bwabwayo.app.domain.support.domain.Inquery;
import com.bwabwayo.app.domain.support.dto.request.InqueryRequest;
import com.bwabwayo.app.domain.support.dto.response.InqueryResponse;
import com.bwabwayo.app.domain.support.repository.InqueryRepository;
import com.bwabwayo.app.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class InqueryService {

    private final InqueryRepository inqueryRepository;

    // 문의 게시물 페이지 페이징
    public Page<InqueryResponse> findAll(Pageable pageable) {

        Page<Inquery> Inqueries = inqueryRepository.findAll(pageable);

        // Inquery response 객체로 변환
        return Inqueries.map(Inquery ->
                InqueryResponse.builder()
                        .id(Inquery.getId())
                        .title(Inquery.getTitle())
                        .name(Inquery.getUser().getNickname())
                        .description(Inquery.getDescription())
                        .reply(Inquery.getReply())
                        .createdAt(Inquery.getCreatedAt().toString())
                        .build());
    }

    // 문의 게시물 작성
    public String save(InqueryRequest inqueryRequest, User user) {
        Inquery inquery = inqueryRequest.toEntity(inqueryRequest);
        inquery.setUser(user);
        inqueryRepository.save(inquery);

        return "게시물이 저장되었습니다.";
    }
}
