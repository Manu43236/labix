package com.labix.api.auth;

import com.labix.api.auth.dto.AuthResponse;
import com.labix.api.auth.dto.LoginRequest;
import com.labix.api.auth.dto.RefreshRequest;
import com.labix.api.auth.dto.SuperAdminLoginRequest;
import com.labix.api.common.exception.AppException;
import com.labix.api.repository.SuperAdminRepository;
import com.labix.api.repository.TenantRepository;
import com.labix.api.repository.UserRepository;
import com.labix.api.repository.UserRoleRepository;
import com.labix.api.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final TenantRepository tenantRepository;
    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public AuthResponse loginUser(LoginRequest req) {
        var tenant = tenantRepository.findBySlug(req.getTenantSlug())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "TENANT_NOT_FOUND", "Lab not found"));

        if ("SUSPENDED".equals(tenant.getStatus())) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "TENANT_SUSPENDED", "Lab account is suspended");
        }

        var user = userRepository.findByTenantIdAndEmail(tenant.getId(), req.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS", "Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS", "Invalid email or password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "USER_INACTIVE", "User account is inactive");
        }

        List<String> roles = userRoleRepository.findByUserId(user.getId())
                .stream().map(r -> r.getRole()).collect(Collectors.toList());

        return new AuthResponse(
                jwtUtil.generateAccessToken(user.getId(), "USER", tenant.getId(), roles),
                jwtUtil.generateRefreshToken(user.getId(), "USER", tenant.getId()),
                roles
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse loginSuperAdmin(SuperAdminLoginRequest req) {
        var admin = superAdminRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS", "Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), admin.getPasswordHash())) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS", "Invalid email or password");
        }

        return new AuthResponse(
                jwtUtil.generateAccessToken(admin.getId(), "SUPER_ADMIN", null, List.of("SUPER_ADMIN")),
                jwtUtil.generateRefreshToken(admin.getId(), "SUPER_ADMIN", null),
                List.of("SUPER_ADMIN")
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest req) {
        Claims claims;
        try {
            claims = jwtUtil.parseToken(req.getRefreshToken());
        } catch (Exception e) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN", "Invalid or expired refresh token");
        }

        if (!"REFRESH".equals(claims.get("tokenType"))) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN", "Not a refresh token");
        }

        String subject = claims.getSubject();
        String type = claims.get("type", String.class);
        String tenantId = claims.get("tenantId", String.class);

        List<String> roles;
        if ("USER".equals(type)) {
            roles = userRoleRepository.findByUserId(subject)
                    .stream().map(r -> r.getRole()).collect(Collectors.toList());
        } else {
            roles = List.of("SUPER_ADMIN");
        }

        return new AuthResponse(
                jwtUtil.generateAccessToken(subject, type, tenantId, roles),
                req.getRefreshToken(), // rotate refresh token only when near expiry
                roles
        );
    }
}
