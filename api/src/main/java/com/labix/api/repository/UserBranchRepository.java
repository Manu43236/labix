package com.labix.api.repository;

import com.labix.api.domain.UserBranch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBranchRepository extends JpaRepository<UserBranch, String> {

    List<UserBranch> findByUserId(String userId);

    List<UserBranch> findByBranchId(String branchId);
}
