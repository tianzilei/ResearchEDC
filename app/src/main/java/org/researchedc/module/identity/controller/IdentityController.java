package org.researchedc.module.identity.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.researchedc.module.identity.dto.AssignRoleRequest;
import org.researchedc.module.identity.dto.CreateUserRequest;
import org.researchedc.module.identity.dto.RoleDTO;
import org.researchedc.module.identity.dto.UserDTO;
import org.researchedc.module.identity.service.IdentityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
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
        // TODO: extract ownerId from security context / JWT token
        Integer ownerId = 1;
        UserDTO dto = identityService.createUser(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/roles/assign")
    public ResponseEntity<Void> assignRole(@Valid @RequestBody AssignRoleRequest request) {
        // TODO: extract ownerId from security context / JWT token
        Integer ownerId = 1;
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
}
