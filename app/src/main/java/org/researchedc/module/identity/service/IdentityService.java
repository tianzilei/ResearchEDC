package org.researchedc.module.identity.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.identity.dto.AssignRoleRequest;
import org.researchedc.module.identity.dto.ChangePasswordRequest;
import org.researchedc.module.identity.dto.CreateUserRequest;
import org.researchedc.module.identity.dto.RoleDTO;
import org.researchedc.module.identity.dto.UpdateProfileRequest;
import org.researchedc.module.identity.dto.UserDTO;
import org.researchedc.module.identity.entity.RoleEntity;
import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.identity.repository.RoleRepository;
import org.researchedc.module.identity.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IdentityService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private static final int SYSADMIN_USER_TYPE_ID = 1;
    private static final int REGULAR_USER_TYPE_ID = 2;
    private static final int TECHADMIN_USER_TYPE_ID = 3;

    public IdentityService(UserAccountRepository userAccountRepository,
                           RoleRepository roleRepository,
                           AuditService auditService,
                           PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> searchUsers(String query) {
        return userAccountRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query)
            .stream()
            .map(this::toUserDto)
            .toList();
    }

    public UserDTO getUserByUsername(String username) {
        UserAccountEntity entity = userAccountRepository.findByUserName(username)
            .orElseThrow(() -> new NoSuchElementException(
                "User not found: " + username));
        return toUserDto(entity);
    }

    public UserDTO getUser(Integer userId) {
        UserAccountEntity entity = userAccountRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException(
                "User not found: " + userId));
        return toUserDto(entity);
    }

    public List<RoleDTO> getUserRoles(String userName) {
        return roleRepository.findByUserName(userName)
            .stream()
            .map(this::toRoleDto)
            .toList();
    }

    public List<RoleDTO> getStudyRoles(Integer studyId) {
        return roleRepository.findByStudyId(studyId)
            .stream()
            .map(this::toRoleDto)
            .toList();
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request, Integer ownerId) {
        if (request.getUserName() == null || request.getUserName().isBlank()) {
            throw new IllegalArgumentException("userName is required");
        }
        if (userAccountRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new IllegalArgumentException(
                "User already exists: " + request.getUserName());
        }

        UserAccountEntity entity = new UserAccountEntity();
        entity.setUserName(request.getUserName());
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setInstitutionalAffiliation(request.getInstitutionalAffiliation());
        entity.setStatusId(request.getStatusId());
        entity.setEnabled(true);
        entity.setAccountNonLocked(true);
        entity.setDateCreated(LocalDateTime.now());
        entity.setOwnerId(ownerId);

        UserAccountEntity saved = userAccountRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.CREATE, "UserAccount",
                saved.getUserId().longValue(), saved.getUserName(),
                null, null, ownerId, null, "identity");

        return toUserDto(saved);
    }

    @Transactional
    public void assignRole(AssignRoleRequest request, Integer ownerId) {
        RoleEntity entity = new RoleEntity();
        entity.setUserName(request.getUserName());
        entity.setStudyId(request.getStudyId());
        entity.setRoleName(request.getRoleName());
        entity.setStatusId(request.getStatusId());
        entity.setOwnerId(ownerId);

        RoleEntity saved = roleRepository.save(entity);

        auditService.recordAudit(
                request.getStudyId(), AuditEventType.ASSIGN, "StudyUserRole",
                saved.getStudyUserRoleId().longValue(), request.getRoleName(),
                null, null, ownerId, null, "identity");
    }

    @Transactional
    public UserDTO updateProfile(Integer userId, UpdateProfileRequest request, Integer updaterId) {
        UserAccountEntity entity = userAccountRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException(
                "User not found: " + userId));

        if (request.getFirstName() != null) {
            entity.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            entity.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            entity.setPhone(request.getPhone());
        }
        if (request.getInstitution() != null) {
            entity.setInstitutionalAffiliation(request.getInstitution());
        }
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(updaterId);

        UserAccountEntity saved = userAccountRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.UPDATE, "UserAccount",
                saved.getUserId().longValue(), saved.getUserName(),
                null, null, updaterId, "Profile updated", "identity");

        return toUserDto(saved);
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request, Integer updaterId) {
        UserAccountEntity entity = userAccountRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException(
                "User not found: " + userId));

        if (entity.getPasswordHash() == null || !passwordEncoder.matches(request.getOldPassword(), entity.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        entity.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(updaterId);

        userAccountRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.UPDATE, "UserAccount",
                entity.getUserId().longValue(), entity.getUserName(),
                null, null, updaterId, "Password changed", "identity");
    }

    private UserDTO toUserDto(UserAccountEntity e) {
        UserDTO dto = new UserDTO();
        dto.setUserId(e.getUserId());
        dto.setUserName(e.getUserName());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setInstitutionalAffiliation(e.getInstitutionalAffiliation());
        if (e.getUserTypeId() != null) {
            dto.setUserType(userTypeName(e.getUserTypeId()));
        }
        dto.setEnabled(e.getEnabled());
        dto.setActiveStudyId(e.getActiveStudyId());
        dto.setDateCreated(e.getDateCreated());
        return dto;
    }

    private String userTypeName(Integer userTypeId) {
        return switch (userTypeId) {
            case SYSADMIN_USER_TYPE_ID -> "business_administrator";
            case REGULAR_USER_TYPE_ID -> "user";
            case TECHADMIN_USER_TYPE_ID -> "technical_administrator";
            default -> "invalid";
        };
    }

    private RoleDTO toRoleDto(RoleEntity e) {
        RoleDTO dto = new RoleDTO();
        dto.setStudyUserRoleId(e.getStudyUserRoleId());
        dto.setRoleName(e.getRoleName());
        dto.setUserName(e.getUserName());
        dto.setStudyId(e.getStudyId());
        dto.setStatusId(e.getStatusId());
        return dto;
    }
}
