package org.researchedc.module.identity.dto;

import java.util.List;

public record CurrentUserResponse(
    Integer userId,
    String username,
    String firstName,
    String lastName,
    Boolean enabled,
    List<String> roles,
    List<StudyRoleInfo> studyRoles
) {

    public record StudyRoleInfo(
        Integer studyId,
        String roleName
    ) {}
}
