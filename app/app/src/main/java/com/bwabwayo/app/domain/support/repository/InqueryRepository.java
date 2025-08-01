package com.bwabwayo.app.domain.support.repository;

import com.bwabwayo.app.domain.support.domain.Inquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InqueryRepository extends JpaRepository<Inquery,Integer> {
    Page<Inquery> findAll(Pageable pageable);
}
