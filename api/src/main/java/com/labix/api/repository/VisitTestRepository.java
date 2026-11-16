package com.labix.api.repository;

import com.labix.api.domain.VisitTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitTestRepository extends JpaRepository<VisitTest, String> {

    List<VisitTest> findByVisitId(String visitId);
}
