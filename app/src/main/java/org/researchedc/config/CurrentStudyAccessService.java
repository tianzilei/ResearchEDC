package org.researchedc.config;

import java.util.Locale;
import java.util.Set;

import org.researchedc.module.identity.entity.RoleEntity;
import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.identity.repository.RoleRepository;
import org.researchedc.module.identity.repository.UserAccountRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class CurrentStudyAccessService {

    private static final int SYSADMIN_USER_TYPE_ID = 1;
    private static final int TECHADMIN_USER_TYPE_ID = 3;
    private static final int STATUS_AVAILABLE = 1;

    private static final Set<String> EXPORT_ROLES = Set.of(
            "admin",
            "business_administrator",
            "study_director",
            "studydirector",
            "director",
            "coordinator",
            "datamanager",
            "data_manager",
            "data specialist"
    );

    private static final Set<String> IMPORT_ROLES = Set.of(
            "admin",
            "business_administrator",
            "study_director",
            "studydirector",
            "director",
            "coordinator",
            "datamanager",
            "data_manager",
            "data specialist"
    );

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;

    public CurrentStudyAccessService(UserAccountRepository userAccountRepository,
                                     RoleRepository roleRepository) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
    }

    public boolean canExportStudy(Integer userId, Integer studyId) {
        return canAccessStudy(userId, studyId, EXPORT_ROLES);
    }

    public boolean canImportStudy(Integer userId, Integer studyId) {
        return canAccessStudy(userId, studyId, IMPORT_ROLES);
    }

    private boolean canAccessStudy(Integer userId, Integer studyId, Set<String> roleNames) {
        if (userId == null || studyId == null) {
            return false;
        }
        return userAccountRepository.findById(userId)
                .map(user -> isAdministrator(user) || hasStudyRole(user.getUserName(), studyId, roleNames))
                .orElse(false);
    }

    private boolean isAdministrator(UserAccountEntity user) {
        return Integer.valueOf(SYSADMIN_USER_TYPE_ID).equals(user.getUserTypeId())
                || Integer.valueOf(TECHADMIN_USER_TYPE_ID).equals(user.getUserTypeId());
    }

    private boolean hasStudyRole(String username, Integer studyId, Set<String> roleNames) {
        return roleRepository.findByUserNameAndStudyId(username, studyId).stream()
                .filter(this::isAvailable)
                .map(RoleEntity::getRoleName)
                .map(this::normalizeRoleName)
                .anyMatch(roleNames::contains);
    }

    private boolean isAvailable(RoleEntity role) {
        return Integer.valueOf(STATUS_AVAILABLE).equals(role.getStatusId());
    }

    private String normalizeRoleName(String roleName) {
        return roleName == null ? "" : roleName.toLowerCase(Locale.ROOT);
    }
}
