package org.akaza.openclinica.module.identity.controller;

import java.util.List;
import org.akaza.openclinica.module.identity.dto.RoleDTO;
import org.akaza.openclinica.module.identity.dto.UserDTO;
import org.akaza.openclinica.module.identity.service.IdentityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/roles/by-user")
    public ResponseEntity<List<RoleDTO>> getUserRoles(@RequestParam String userName) {
        return ResponseEntity.ok(identityService.getUserRoles(userName));
    }

    @GetMapping("/roles/by-study")
    public ResponseEntity<List<RoleDTO>> getStudyRoles(@RequestParam Integer studyId) {
        return ResponseEntity.ok(identityService.getStudyRoles(studyId));
    }
}
