package org.researchedc.module.identity.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.researchedc.module.identity.dto.AssignRoleRequest;
import org.researchedc.module.identity.dto.ChangePasswordRequest;
import org.researchedc.module.identity.dto.CreateUserRequest;
import org.researchedc.module.identity.dto.RoleDTO;
import org.researchedc.module.identity.dto.UpdateProfileRequest;
import org.researchedc.module.identity.dto.UserDTO;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.identity.service.IdentityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
public class IdentityController {

    private final IdentityService identityService;
    private final CurrentUserUtils currentUserUtils;

    public IdentityController(IdentityService identityService, CurrentUserUtils currentUserUtils) {
        this.identityService = identityService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(identityService.searchUsers(query));
    }

    @GetMapping("/users/by-username")
    public ResponseEntity<UserDTO> getUserByUsername(@RequestParam String username) {
        return ResponseEntity.ok(identityService.getUserByUsername(username));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Integer id) {
        return ResponseEntity.ok(identityService.getUser(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        UserDTO dto = identityService.createUser(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/roles/assign")
    public ResponseEntity<Void> assignRole(@Valid @RequestBody AssignRoleRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        identityService.assignRole(request, ownerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles/by-user")
    public ResponseEntity<List<RoleDTO>> getUserRoles(@RequestParam String userName) {
        return ResponseEntity.ok(identityService.getUserRoles(userName));
    }

    @GetMapping("/roles/by-study")
    public ResponseEntity<List<RoleDTO>> getStudyRoles(@RequestParam Integer studyId) {
        return ResponseEntity.ok(identityService.getStudyRoles(studyId));
    }

    @PutMapping("/users/{id}/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateProfileRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        if (!currentUserId.equals(id)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "You can only update your own profile");
        }
        return ResponseEntity.ok(identityService.updateProfile(id, request, currentUserId));
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Integer id,
            @Valid @RequestBody ChangePasswordRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        if (!currentUserId.equals(id)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "You can only change your own password");
        }
        identityService.changePassword(id, request, currentUserId);
        return ResponseEntity.ok().build();
    }
}
