package com.bwabwayo.app.domain.support.service;

import com.bwabwayo.app.domain.support.domain.Inquiry;
import com.bwabwayo.app.domain.support.domain.InquiryImage;
import com.bwabwayo.app.domain.support.dto.request.InquiryRequest;
import com.bwabwayo.app.domain.support.dto.response.InquiryResponse;
import com.bwabwayo.app.domain.support.repository.InquiryRepository;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.global.storage.service.StorageService;
import com.bwabwayo.app.global.storage.util.StorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final StorageService storageService;
    private final StorageUtil storageUtil;

    // 문의 게시물 페이지 페이징
    @Transactional(readOnly = true)
    public Page<InquiryResponse> findAll(Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findAll(pageable);

        // Inquiry response 객체로 변환
        return inquiries.map(inquiry ->
                InquiryResponse.builder()
                        .id(inquiry.getId())
                        .title(inquiry.getTitle())
                        .imageUrlList(inquiry.getInquiryImages().stream().map(img->storageService.getUrlFromKey(img.getUrl())).toList())
                        .name(inquiry.getUser().getNickname())
                        .description(inquiry.getDescription())
                        .reply(inquiry.getReply())
                        .createdAt(inquiry.getCreatedAt().toString())
                        .build());
    }

    public Page<InquiryResponse> findInquiryByUserId(User user, Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findInquiriesByUser_Id(user.getId(), pageable);

        // Inquiry response 객체로 변환
        return inquiries.map(inquiry ->
                InquiryResponse.builder()
                        .id(inquiry.getId())
                        .title(inquiry.getTitle())
                        .imageUrlList(inquiry.getInquiryImages().stream().map(img->storageService.getUrlFromKey(img.getUrl())).toList())
                        .name(inquiry.getUser().getNickname())
                        .description(inquiry.getDescription())
                        .reply(inquiry.getReply())
                        .createdAt(inquiry.getCreatedAt().toString())
                        .build());
    }

    // 문의 게시물 작성
    @Transactional
    public void save(InquiryRequest inquiryRequest, User user) {
        Inquiry inquiry = new Inquiry();
        inquiry.setTitle(inquiryRequest.getTitle());
        inquiry.setDescription(inquiryRequest.getDescription());
        inquiry.setUser(user);

        storageUtil.copyToDirectory(inquiryRequest.getImageUrlList(), "temp", "inquiries");

        int index = 1;
        for (String imageUrl : inquiryRequest.getImageUrlList()) {
            inquiry.getInquiryImages().add(
                    InquiryImage.builder()
                            .no(index++)
                            .inquiry(inquiry)
                            .url(imageUrl)
                            .build()
            );
        }

        inquiryRepository.save(inquiry);
    }
}
