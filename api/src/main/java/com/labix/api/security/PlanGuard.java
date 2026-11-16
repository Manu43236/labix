package com.labix.api.security;

import com.labix.api.common.exception.AppException;
import com.labix.api.repository.TenantSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanGuard {

    private final TenantSubscriptionRepository subscriptionRepository;

    public void require(String tenantId, PlanFeature feature) {
        var sub = subscriptionRepository
                .findFirstByTenantIdAndStatusOrderByStartDateDesc(tenantId, "ACTIVE")
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN,
                        "NO_ACTIVE_SUBSCRIPTION", "No active subscription"));

        PlanCode planCode = PlanCode.valueOf(sub.getPlanCode());
        if (!planCode.includes(feature)) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "PLAN_INSUFFICIENT", "Your plan does not include this feature");
        }
    }
}
