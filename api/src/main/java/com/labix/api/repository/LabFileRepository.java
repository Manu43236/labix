package com.labix.api.repository;

import com.labix.api.domain.LabFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabFileRepository extends JpaRepository<LabFile, String> {
}
