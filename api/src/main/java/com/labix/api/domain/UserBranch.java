package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_branches")
@Getter
@Setter
@NoArgsConstructor
public class UserBranch extends UuidEntity {

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "branch_id", nullable = false, length = 36)
    private String branchId;
}
