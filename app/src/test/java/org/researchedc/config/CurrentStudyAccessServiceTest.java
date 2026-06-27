package org.researchedc.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void canImportStudy_allowsAvailableImportRoleOnSameStudy() {
        UserAccountEntity user = user("datamanager", 2);
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserNameAndStudyId("datamanager", 11))
                .thenReturn(List.of(role("datamanager", 1)));

        assertTrue(service().canImportStudy(7, 11));
    }

    @Test
    void canImportStudy_deniesMonitorRoleOnSameStudy() {
        UserAccountEntity user = user("monitor", 2);
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserNameAndStudyId("monitor", 11))
                .thenReturn(List.of(role("monitor", 1)));

        assertFalse(service().canImportStudy(7, 11));
    }

    @Test
    void canReadStudy_allowsMonitorRoleOnSameStudy() {
        UserAccountEntity user = user("monitor", 2);
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserNameAndStudyId("monitor", 11))
                .thenReturn(List.of(role("monitor", 1)));

        assertTrue(service().canReadStudy(7, 11));
    }

    @Test
    void canWriteStudy_deniesMonitorRoleOnSameStudy() {
        UserAccountEntity user = user("monitor", 2);
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserNameAndStudyId("monitor", 11))
                .thenReturn(List.of(role("monitor", 1)));

        assertFalse(service().canWriteStudy(7, 11));
    }

    @Test
    void readableStudyIds_returnsAvailableReadableStudies() {
        UserAccountEntity user = user("reader", 2);
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserName("reader")).thenReturn(List.of(
                role("monitor", 1, 11),
                role("data_entry", 1, 12),
                role("monitor", 5, 13),
                role("unknown", 1, 14),
                role("monitor", 1, null)
        ));

        assertEquals(java.util.Set.of(11, 12), service().readableStudyIds(7));
    }

    @Test
    void canReadAllStudies_allowsSysAndTechAdmins() {
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(user("sysadmin", 1)));
        when(userAccountRepository.findById(8)).thenReturn(Optional.of(user("techadmin", 3)));

        CurrentStudyAccessService service = service();
        assertTrue(service.canReadAllStudies(7));
        assertTrue(service.canReadAllStudies(8));
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
        return role(roleName, statusId, null);
    }

    private static RoleEntity role(String roleName, int statusId, Integer studyId) {
        RoleEntity role = new RoleEntity();
        role.setRoleName(roleName);
        role.setStatusId(statusId);
        role.setStudyId(studyId);
        return role;
    }
}
