package org.researchedc.module.identity.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import jakarta.validation.Valid;

import org.researchedc.dao.hibernate.UserAccountDao;
import org.researchedc.domain.user.UserAccount;
import org.researchedc.module.identity.dto.LoginRequest;
import org.researchedc.module.identity.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserAccountDao userAccountDao;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserAccountDao userAccountDao) {
        this.userAccountDao = userAccountDao;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String username = request.username().trim();
        String password = request.password();

        UserAccount user = userAccountDao.findByUserName(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!matchesPassword(password, user.getPasswd())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String token = generateToken(username);
        List<String> roles = extractRoles(user);
        String email = user.getEmail() != null ? user.getEmail() : "";

        LoginResponse response = new LoginResponse(
            token,
            user.getUserName(),
            user.getFirstName(),
            user.getLastName(),
            email,
            roles
        );

        return ResponseEntity.ok(response);
    }

    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        try {
            if (passwordEncoder.matches(rawPassword, encodedPassword)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        String sha1Hex = sha1Hex(rawPassword);
        return sha1Hex.equalsIgnoreCase(encodedPassword);
    }

    private String sha1Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not available", e);
        }
    }

    private String generateToken(String username) {
        long exp = Instant.now().plusSeconds(86400).toEpochMilli();
        String payload = "{\"sub\":\"" + username + "\",\"exp\":" + exp + "}";
        return "dev-" + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private List<String> extractRoles(UserAccount user) {
        return List.of();
    }
}
