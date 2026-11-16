package com.labix.api.repository;

import com.labix.api.domain.TestParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestParameterRepository extends JpaRepository<TestParameter, String> {

    List<TestParameter> findByTestIdOrderBySortOrder(String testId);
}
