package org.akaza.openclinica.module.identity.service;

import java.util.List;
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
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "User not found: " + username));
        return toUserDto(entity);
    }

    public UserDTO getUser(Integer userId) {
        UserAccountEntity entity = userAccountRepository.findById(userId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
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
