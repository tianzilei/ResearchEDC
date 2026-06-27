package org.researchedc.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.module.identity.entity.RoleEntity;
import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.identity.repository.RoleRepository;
import org.researchedc.module.identity.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class CurrentStudyAccessServiceTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private RoleRepository roleRepository;

    @Test
    void canExportStudy_allowsSysAdminWithoutStudyRole() {
        UserAccountEntity user = user("sysadmin", 1);
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(user));

        assertTrue(service().canExportStudy(7, 11));
    }

    @Test
    void canExportStudy_allowsAvailableExportRoleOnSameStudy() {
        UserAccountEntity user = user("coordinator", 2);
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserNameAndStudyId("coordinator", 11))
                .thenReturn(List.of(role("coordinator", 1)));

        assertTrue(service().canExportStudy(7, 11));
    }

    @Test
    void canExportStudy_deniesNonExportRoleOnSameStudy() {
        UserAccountEntity user = user("monitor", 2);
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserNameAndStudyId("monitor", 11))
                .thenReturn(List.of(role("monitor", 1)));

        assertFalse(service().canExportStudy(7, 11));
    }

    @Test
    void canExportStudy_deniesInactiveExportRole() {
        UserAccountEntity user = user("coordinator", 2);
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserNameAndStudyId("coordinator", 11))
                .thenReturn(List.of(role("coordinator", 5)));

        assertFalse(service().canExportStudy(7, 11));
    }

    private CurrentStudyAccessService service() {
        return new CurrentStudyAccessService(userAccountRepository, roleRepository);
    }

    private static UserAccountEntity user(String userName, int userTypeId) {
        UserAccountEntity user = new UserAccountEntity();
        user.setUserName(userName);
        user.setUserTypeId(userTypeId);
        return user;
    }

    private static RoleEntity role(String roleName, int statusId) {
        RoleEntity role = new RoleEntity();
        role.setRoleName(roleName);
        role.setStatusId(statusId);
        return role;
    }
}
