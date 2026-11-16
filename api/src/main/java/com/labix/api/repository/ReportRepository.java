package com.labix.api.repository;

import com.labix.api.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, String> {

    List<Report> findByVisitIdAndDeletedAtIsNull(String visitId);

    Optional<Report> findFirstByVisitIdAndStatusAndDeletedAtIsNull(String visitId, String status);

    Optional<Report> findFirstByVisitIdAndDeletedAtIsNullOrderByVersionDesc(String visitId);
}
