package org.akaza.openclinica.module.identity.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.akaza.openclinica.module.identity.dto.AssignRoleRequest;
import org.akaza.openclinica.module.identity.dto.CreateUserRequest;
import org.akaza.openclinica.module.identity.dto.RoleDTO;
import org.akaza.openclinica.module.identity.dto.UserDTO;
import org.akaza.openclinica.module.identity.entity.RoleEntity;
import org.akaza.openclinica.module.identity.entity.UserAccountEntity;
import org.akaza.openclinica.module.identity.repository.RoleRepository;
import org.akaza.openclinica.module.identity.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IdentityService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;

    public IdentityService(UserAccountRepository userAccountRepository,
                           RoleRepository roleRepository) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
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

        roleRepository.save(entity);
    }

    private UserDTO toUserDto(UserAccountEntity e) {
        UserDTO dto = new UserDTO();
        dto.setUserId(e.getUserId());
        dto.setUserName(e.getUserName());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setEnabled(e.getEnabled());
        dto.setActiveStudyId(e.getActiveStudyId());
        dto.setDateCreated(e.getDateCreated());
        return dto;
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
