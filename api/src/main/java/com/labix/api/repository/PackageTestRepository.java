package com.labix.api.repository;

import com.labix.api.domain.PackageTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PackageTestRepository extends JpaRepository<PackageTest, String> {

    List<PackageTest> findByPackageId(String packageId);

    @Transactional
    void deleteByPackageId(String packageId);
}
