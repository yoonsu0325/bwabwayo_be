package com.bwabwayo.app.domain.support.repository;

import com.bwabwayo.app.domain.support.domain.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report,Long> {
    Page<Report> findAll(Pageable pageable);

    Page<Report> findReportsByReporter_Id(String reporterId, Pageable pageable);
}
