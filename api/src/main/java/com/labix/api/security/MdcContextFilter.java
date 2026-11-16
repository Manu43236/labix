package com.labix.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adds per-request metadata to MDC for structured logging.
 * Never logs request bodies — those may contain patient PII.
 */
@Component
public class MdcContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {
        try {
            MDC.put("requestId", UUID.randomUUID().toString().replace("-", "").substring(0, 12));
            MDC.put("method",    request.getMethod());
            MDC.put("path",      request.getRequestURI());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LabixUserDetails u) {
                MDC.put("tenantId", u.getTenantId());
                MDC.put("userId",   u.getUserId());
            }

            chain.doFilter(request, response);

            MDC.put("status", String.valueOf(response.getStatus()));
        } finally {
            MDC.clear();
        }
    }
}
