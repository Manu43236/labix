package com.labix.api.auth;

import com.labix.api.auth.dto.*;
import com.labix.api.common.dto.ApiResponse;
import com.labix.api.common.exception.AppException;
import com.labix.api.security.LabixUserDetails;
import com.labix.api.security.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        checkRateLimit(clientIp(httpRequest));
        AuthResponse response = authService.loginUser(request);
        rateLimiter.reset(clientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/super/login")
    public ResponseEntity<ApiResponse<AuthResponse>> superLogin(
            @Valid @RequestBody SuperAdminLoginRequest request,
            HttpServletRequest httpRequest) {
        checkRateLimit(clientIp(httpRequest));
        AuthResponse response = authService.loginSuperAdmin(request);
        rateLimiter.reset(clientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(Authentication auth) {
        LabixUserDetails user = (LabixUserDetails) auth.getPrincipal();
        var roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(
                new MeResponse(user.getUserId(), user.getTenantId(), user.isSuperAdmin(), roles)
        ));
    }

    private void checkRateLimit(String ip) {
        if (!rateLimiter.isAllowed(ip)) {
            throw new AppException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED",
                    "Too many login attempts. Please wait and try again.");
        }
    }

    private String clientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
