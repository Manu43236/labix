package com.labix.api.repository;

import com.labix.api.domain.ReferenceRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReferenceRangeRepository extends JpaRepository<ReferenceRange, String> {

    List<ReferenceRange> findByParameterId(String parameterId);

    @Transactional
    void deleteByParameterId(String parameterId);
}
