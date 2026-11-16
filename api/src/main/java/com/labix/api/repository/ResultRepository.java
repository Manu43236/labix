package com.labix.api.repository;

import com.labix.api.domain.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, String> {

    List<Result> findByVisitTestId(String visitTestId);

    Optional<Result> findByVisitTestIdAndParameterId(String visitTestId, String parameterId);

    @Query("SELECT r FROM Result r WHERE r.visitTestId IN " +
           "(SELECT vt.id FROM VisitTest vt WHERE vt.visitId = :visitId)")
    List<Result> findByVisitId(@Param("visitId") String visitId);
}
