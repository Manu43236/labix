package com.labix.api.branch;

import com.labix.api.branch.dto.BranchRequest;
import com.labix.api.branch.dto.BranchResponse;
import com.labix.api.common.exception.AppException;
import com.labix.api.domain.Branch;
import com.labix.api.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public List<BranchResponse> listBranches(String tenantId) {
        return branchRepository.findByTenantId(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public BranchResponse createBranch(String tenantId, BranchRequest req) {
        if (req.isMain() && branchRepository.existsByTenantIdAndMain(tenantId, true)) {
            throw new AppException(HttpStatus.CONFLICT, "MAIN_BRANCH_EXISTS",
                    "A main branch already exists; update it or unmark it first");
        }
        Branch b = new Branch();
        b.setTenantId(tenantId);
        b.setName(req.getName());
        b.setAddress(req.getAddress());
        b.setPhone(req.getPhone());
        b.setMain(req.isMain());
        branchRepository.save(b);
        return toResponse(b);
    }

    @Transactional
    public BranchResponse updateBranch(String tenantId, String id, BranchRequest req) {
        Branch b = findBranch(tenantId, id);
        if (req.isMain() && !b.isMain() && branchRepository.existsByTenantIdAndMain(tenantId, true)) {
            throw new AppException(HttpStatus.CONFLICT, "MAIN_BRANCH_EXISTS",
                    "A main branch already exists; update it or unmark it first");
        }
        b.setName(req.getName());
        b.setAddress(req.getAddress());
        b.setPhone(req.getPhone());
        b.setMain(req.isMain());
        branchRepository.save(b);
        return toResponse(b);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Branch findBranch(String tenantId, String id) {
        Branch b = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "BRANCH_NOT_FOUND", "Branch not found"));
        if (!b.getTenantId().equals(tenantId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "BRANCH_NOT_FOUND", "Branch not found");
        }
        return b;
    }

    private BranchResponse toResponse(Branch b) {
        BranchResponse r = new BranchResponse();
        r.setId(b.getId());
        r.setName(b.getName());
        r.setAddress(b.getAddress());
        r.setPhone(b.getPhone());
        r.setMain(b.isMain());
        return r;
    }
}
