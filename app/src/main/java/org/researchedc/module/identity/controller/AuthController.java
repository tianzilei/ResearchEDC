package org.researchedc.module.identity.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.researchedc.module.identity.dto.CurrentUserResponse;
import org.researchedc.module.identity.dto.CurrentUserResponse.StudyRoleInfo;
import org.researchedc.module.identity.dto.LoginRequest;
import org.researchedc.module.identity.dto.LoginResponse;
import org.researchedc.module.identity.entity.RoleEntity;
import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.identity.repository.RoleRepository;
import org.researchedc.module.identity.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          UserAccountRepository userAccountRepository,
                          RoleRepository roleRepository) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.securityContextRepository = new HttpSessionSecurityContextRepository();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        String username = request.username().trim();
        String password = request.password();

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
        } catch (BadCredentialsException | DisabledException | LockedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserAccountEntity user = userAccountRepository.findByUserName(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> roles = roleRepository.findByUserName(username)
                .stream()
                .map(RoleEntity::getRoleName)
                .distinct()
                .toList();

        LoginResponse response = new LoginResponse(
                "",
                user.getUserName(),
                user.getFirstName(),
                user.getLastName(),
                roles
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        UserAccountEntity user = userAccountRepository.findByUserName(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> roles = roleRepository.findByUserName(username)
                .stream()
                .map(RoleEntity::getRoleName)
                .distinct()
                .toList();

        List<StudyRoleInfo> studyRoles = roleRepository.findByUserName(username)
                .stream()
                .map(r -> new StudyRoleInfo(r.getStudyId(), r.getRoleName()))
                .toList();

        CurrentUserResponse response = new CurrentUserResponse(
                user.getUserId(),
                user.getUserName(),
                user.getFirstName(),
                user.getLastName(),
                user.getEnabled(),
                roles,
                studyRoles
        );

        return ResponseEntity.ok(response);
    }
}
