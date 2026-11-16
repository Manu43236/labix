package com.labix.api.labuser;

import com.labix.api.common.dto.PageResponse;
import com.labix.api.common.exception.AppException;
import com.labix.api.domain.LabFile;
import com.labix.api.domain.User;
import com.labix.api.domain.UserRole;
import com.labix.api.labuser.dto.CreateLabUserRequest;
import com.labix.api.labuser.dto.LabUserResponse;
import com.labix.api.labuser.dto.UpdateLabUserRequest;
import com.labix.api.repository.LabFileRepository;
import com.labix.api.repository.UserRepository;
import com.labix.api.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final LabFileRepository fileRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.uploads.dir:./uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public PageResponse<LabUserResponse> listUsers(String tenantId, Pageable pageable) {
        return PageResponse.of(userRepository.findByTenantId(tenantId, pageable).map(u -> toResponse(u, getRoles(u.getId()))));
    }

    @Transactional
    public LabUserResponse createUser(String tenantId, CreateLabUserRequest req) {
        if (userRepository.findByTenantIdAndEmail(tenantId, req.getEmail()).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Email already in use");
        }
        User u = new User();
        u.setTenantId(tenantId);
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setStatus("ACTIVE");
        userRepository.save(u);

        List<String> roles = req.getRoles() != null ? req.getRoles() : List.of();
        for (String role : roles) {
            UserRole ur = new UserRole();
            ur.setUserId(u.getId());
            ur.setRole(role);
            userRoleRepository.save(ur);
        }
        return toResponse(u, roles);
    }

    @Transactional(readOnly = true)
    public LabUserResponse getUser(String tenantId, String id) {
        User u = findUser(tenantId, id);
        return toResponse(u, getRoles(id));
    }

    @Transactional
    public LabUserResponse updateUser(String tenantId, String id, UpdateLabUserRequest req) {
        User u = findUser(tenantId, id);
        u.setName(req.getName());
        u.setPhone(req.getPhone());
        userRepository.save(u);
        return toResponse(u, getRoles(id));
    }

    @Transactional
    public LabUserResponse setStatus(String tenantId, String id, String status) {
        User u = findUser(tenantId, id);
        u.setStatus(status);
        userRepository.save(u);
        return toResponse(u, getRoles(id));
    }

    @Transactional
    public LabUserResponse addRole(String tenantId, String id, String role) {
        findUser(tenantId, id);
        boolean exists = userRoleRepository.findByUserId(id).stream()
                .anyMatch(r -> r.getRole().equals(role));
        if (!exists) {
            UserRole ur = new UserRole();
            ur.setUserId(id);
            ur.setRole(role);
            userRoleRepository.save(ur);
        }
        return toResponse(findUser(tenantId, id), getRoles(id));
    }

    @Transactional
    public LabUserResponse removeRole(String tenantId, String id, String role) {
        findUser(tenantId, id);
        userRoleRepository.findByUserId(id).stream()
                .filter(r -> r.getRole().equals(role))
                .forEach(r -> userRoleRepository.deleteById(r.getId()));
        return toResponse(findUser(tenantId, id), getRoles(id));
    }

    @Transactional
    public LabUserResponse uploadSignature(String tenantId, String id, MultipartFile file) {
        User u = findUser(tenantId, id);
        validateSignatureFile(file);

        String ext = getExt(file.getOriginalFilename());
        String storageKey = "signatures/" + tenantId + "/" + id + "." + ext;

        try {
            Path dir = Paths.get(uploadDir, "signatures", tenantId);
            Files.createDirectories(dir);
            Files.write(dir.resolve(id + "." + ext), file.getBytes());
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_FAILED", "File upload failed");
        }

        LabFile labFile = new LabFile();
        labFile.setTenantId(tenantId);
        labFile.setStorageKey(storageKey);
        labFile.setMimeType(file.getContentType());
        labFile.setSizeBytes(file.getSize());
        labFile.setPurpose("SIGNATURE");
        fileRepository.save(labFile);

        u.setSignatureFileId(labFile.getId());
        userRepository.save(u);
        return toResponse(u, getRoles(id));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User findUser(String tenantId, String id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
        if (!u.getTenantId().equals(tenantId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found");
        }
        return u;
    }

    private List<String> getRoles(String userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getRole).collect(Collectors.toList());
    }

    private void validateSignatureFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "File is empty");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_FILE", "Only image files are accepted");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new AppException(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE", "Max file size is 2MB");
        }
    }

    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private LabUserResponse toResponse(User u, List<String> roles) {
        LabUserResponse r = new LabUserResponse();
        r.setId(u.getId());
        r.setName(u.getName());
        r.setEmail(u.getEmail());
        r.setPhone(u.getPhone());
        r.setStatus(u.getStatus());
        r.setSignatureFileId(u.getSignatureFileId());
        r.setRoles(roles);
        return r;
    }
}
