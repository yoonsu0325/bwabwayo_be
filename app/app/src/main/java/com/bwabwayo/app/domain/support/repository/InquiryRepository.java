package com.bwabwayo.app.domain.support.repository;

import com.bwabwayo.app.domain.support.domain.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry,Integer> {
    Page<Inquiry> findAll(Pageable pageable);
}
