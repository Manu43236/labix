package com.labix.api.repository;

import com.labix.api.domain.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, String> {

    Optional<Bill> findByVisitId(String visitId);

    @Query("SELECT COALESCE(MAX(b.billNo), 0) FROM Bill b WHERE b.tenantId = :tenantId")
    int findMaxBillNo(@Param("tenantId") String tenantId);
}
