package org.researchedc.module.identity.dto;

import java.util.List;

public record LoginResponse(
    String token,
    String username,
    String firstName,
    String lastName,
    List<String> roles
) {}
