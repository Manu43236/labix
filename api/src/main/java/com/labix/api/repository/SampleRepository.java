package com.labix.api.repository;

import com.labix.api.domain.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SampleRepository extends JpaRepository<Sample, String> {

    List<Sample> findByVisitId(String visitId);

    @Query("SELECT COALESCE(MAX(s.sampleNo), 0) FROM Sample s WHERE s.tenantId = :tenantId")
    int findMaxSampleNo(@Param("tenantId") String tenantId);
}
