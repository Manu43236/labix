package com.labix.api.admin;

import com.labix.api.admin.dto.*;
import com.labix.api.common.exception.AppException;
import com.labix.api.domain.*;
import com.labix.api.repository.*;
import com.labix.api.seed.SeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantAdminService {

    private final TenantRepository tenantRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SupportAccessLogRepository supportAccessLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedService seedService;

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest req) {
        validatePlanCode(req.getPlanCode());

        if (tenantRepository.findBySlug(req.getSlug()).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "SLUG_TAKEN", "Slug already in use");
        }

        Tenant tenant = new Tenant();
        tenant.setName(req.getName());
        tenant.setSlug(req.getSlug());
        tenant.setStatus("ACTIVE");
        tenant.setPhone(req.getPhone());
        tenant.setEmail(req.getEmail());
        tenant.setAddress(req.getAddress());
        tenant.setGstin(req.getGstin());
        tenant.setGstEnabled(req.isGstEnabled());
        tenantRepository.save(tenant);

        createSubscription(tenant.getId(), req.getPlanCode(), LocalDate.now(), null, null);
        seedService.seedTenant(tenant.getId());
        return toResponse(tenant, req.getPlanCode());
    }

    @Transactional(readOnly = true)
    public Page<TenantResponse> listTenants(Pageable pageable) {
        return tenantRepository.findAll(pageable).map(t -> {
            String planCode = subscriptionRepository
                    .findFirstByTenantIdAndStatusOrderByStartDateDesc(t.getId(), "ACTIVE")
                    .map(TenantSubscription::getPlanCode)
                    .orElse(null);
            return toResponse(t, planCode);
        });
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenant(String id) {
        Tenant tenant = findTenant(id);
        String planCode = subscriptionRepository
                .findFirstByTenantIdAndStatusOrderByStartDateDesc(id, "ACTIVE")
                .map(TenantSubscription::getPlanCode)
                .orElse(null);
        return toResponse(tenant, planCode);
    }

    @Transactional
    public TenantResponse updateTenant(String id, UpdateTenantRequest req) {
        Tenant tenant = findTenant(id);
        tenant.setName(req.getName());
        tenant.setPhone(req.getPhone());
        tenant.setEmail(req.getEmail());
        tenant.setAddress(req.getAddress());
        tenant.setGstin(req.getGstin());
        tenant.setGstEnabled(req.isGstEnabled());
        tenantRepository.save(tenant);
        String planCode = subscriptionRepository
                .findFirstByTenantIdAndStatusOrderByStartDateDesc(id, "ACTIVE")
                .map(TenantSubscription::getPlanCode)
                .orElse(null);
        return toResponse(tenant, planCode);
    }

    @Transactional
    public TenantResponse suspendTenant(String id) {
        Tenant tenant = findTenant(id);
        tenant.setStatus("SUSPENDED");
        tenantRepository.save(tenant);
        return toResponse(tenant, null);
    }

    @Transactional
    public TenantResponse activateTenant(String id) {
        Tenant tenant = findTenant(id);
        tenant.setStatus("ACTIVE");
        tenantRepository.save(tenant);
        String planCode = subscriptionRepository
                .findFirstByTenantIdAndStatusOrderByStartDateDesc(id, "ACTIVE")
                .map(TenantSubscription::getPlanCode)
                .orElse(null);
        return toResponse(tenant, planCode);
    }

    @Transactional
    public SubscriptionResponse assignPlan(String tenantId, AssignPlanRequest req) {
        findTenant(tenantId);
        validatePlanCode(req.getPlanCode());

        // Cancel any current active subscription
        subscriptionRepository
                .findFirstByTenantIdAndStatusOrderByStartDateDesc(tenantId, "ACTIVE")
                .ifPresent(sub -> {
                    sub.setStatus("CANCELLED");
                    subscriptionRepository.save(sub);
                });

        LocalDate start = req.getStartDate() != null ? req.getStartDate() : LocalDate.now();
        TenantSubscription sub = createSubscription(tenantId, req.getPlanCode(), start, req.getEndDate(), req.getDiscountNote());
        return toSubResponse(sub);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> listSubscriptions(String tenantId) {
        findTenant(tenantId);
        return subscriptionRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toSubResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse createLabAdmin(String tenantId, CreateLabAdminRequest req) {
        findTenant(tenantId);

        if (userRepository.findByTenantIdAndEmail(tenantId, req.getEmail()).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Email already in use in this lab");
        }

        User user = new User();
        user.setTenantId(tenantId);
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setStatus("ACTIVE");
        userRepository.save(user);

        UserRole role = new UserRole();
        role.setUserId(user.getId());
        role.setRole("LAB_ADMIN");
        userRoleRepository.save(role);

        return toUserResponse(user, List.of("LAB_ADMIN"));
    }

    @Transactional
    public SupportAccessResponse startSupportAccess(String superAdminId, String tenantId, SupportAccessRequest req) {
        findTenant(tenantId);

        SupportAccessLog log = new SupportAccessLog();
        log.setSuperAdminId(superAdminId);
        log.setTenantId(tenantId);
        log.setReason(req.getReason());
        log.setStartedAt(LocalDateTime.now(ZoneOffset.UTC));
        supportAccessLogRepository.save(log);
        return toSupportResponse(log);
    }

    @Transactional
    public SupportAccessResponse endSupportAccess(String logId, String superAdminId) {
        SupportAccessLog log = supportAccessLogRepository.findById(logId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "LOG_NOT_FOUND", "Support access log not found"));

        if (!log.getSuperAdminId().equals(superAdminId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Cannot end another admin's session");
        }

        if (log.getEndedAt() != null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "ALREADY_ENDED", "Session already ended");
        }

        log.setEndedAt(LocalDateTime.now(ZoneOffset.UTC));
        supportAccessLogRepository.save(log);
        return toSupportResponse(log);
    }

    @Transactional(readOnly = true)
    public List<SupportAccessResponse> listSupportAccess(String tenantId) {
        findTenant(tenantId);
        return supportAccessLogRepository.findByTenantIdOrderByStartedAtDesc(tenantId)
                .stream().map(this::toSupportResponse).collect(Collectors.toList());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Tenant findTenant(String id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found"));
    }

    private void validatePlanCode(String code) {
        if (!planRepository.existsById(code)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_PLAN", "Invalid plan code: " + code);
        }
    }

    private TenantSubscription createSubscription(String tenantId, String planCode,
                                                   LocalDate start, LocalDate end, String discountNote) {
        TenantSubscription sub = new TenantSubscription();
        sub.setTenantId(tenantId);
        sub.setPlanCode(planCode);
        sub.setStartDate(start);
        sub.setEndDate(end);
        sub.setStatus("ACTIVE");
        sub.setDiscountNote(discountNote);
        return subscriptionRepository.save(sub);
    }

    private TenantResponse toResponse(Tenant t, String planCode) {
        TenantResponse r = new TenantResponse();
        r.setId(t.getId());
        r.setName(t.getName());
        r.setSlug(t.getSlug());
        r.setStatus(t.getStatus());
        r.setPhone(t.getPhone());
        r.setEmail(t.getEmail());
        r.setAddress(t.getAddress());
        r.setGstin(t.getGstin());
        r.setGstEnabled(t.isGstEnabled());
        r.setCurrentPlanCode(planCode);
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }

    private SubscriptionResponse toSubResponse(TenantSubscription s) {
        SubscriptionResponse r = new SubscriptionResponse();
        r.setId(s.getId());
        r.setPlanCode(s.getPlanCode());
        r.setStartDate(s.getStartDate());
        r.setEndDate(s.getEndDate());
        r.setStatus(s.getStatus());
        r.setDiscountNote(s.getDiscountNote());
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }

    private UserResponse toUserResponse(User u, List<String> roles) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setTenantId(u.getTenantId());
        r.setName(u.getName());
        r.setEmail(u.getEmail());
        r.setPhone(u.getPhone());
        r.setStatus(u.getStatus());
        r.setRoles(roles);
        return r;
    }

    private SupportAccessResponse toSupportResponse(SupportAccessLog l) {
        SupportAccessResponse r = new SupportAccessResponse();
        r.setId(l.getId());
        r.setSuperAdminId(l.getSuperAdminId());
        r.setTenantId(l.getTenantId());
        r.setReason(l.getReason());
        r.setStartedAt(l.getStartedAt());
        r.setEndedAt(l.getEndedAt());
        return r;
    }
}
