package org.researchedc.module.identity.service;

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
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class ResearchEdcUserDetailsServiceTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private RoleRepository roleRepository;

    @Test
    void loadUserByUsername_addsSysAdminAuthorityForLegacyUserType() {
        UserAccountEntity user = user("sysadmin", 1);
        RoleEntity studyRole = role("study_director");
        when(userAccountRepository.findByUserName("sysadmin")).thenReturn(Optional.of(user));
        when(roleRepository.findByUserName("sysadmin")).thenReturn(List.of(studyRole));

        UserDetails result = new ResearchEdcUserDetailsService(userAccountRepository, roleRepository)
                .loadUserByUsername("sysadmin");

        List<String> authorities = result.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();
        assertTrue(authorities.contains("ROLE_STUDY_DIRECTOR"));
        assertTrue(authorities.contains("ROLE_SYSADMIN"));
    }

    @Test
    void loadUserByUsername_doesNotAddSysAdminAuthorityForRegularUserType() {
        UserAccountEntity user = user("regular", 2);
        when(userAccountRepository.findByUserName("regular")).thenReturn(Optional.of(user));
        when(roleRepository.findByUserName("regular")).thenReturn(List.of());

        UserDetails result = new ResearchEdcUserDetailsService(userAccountRepository, roleRepository)
                .loadUserByUsername("regular");

        List<String> authorities = result.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();
        assertFalse(authorities.contains("ROLE_SYSADMIN"));
    }

    private static UserAccountEntity user(String username, int userTypeId) {
        UserAccountEntity user = new UserAccountEntity();
        user.setUserName(username);
        user.setPasswordHash("{noop}password");
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setUserTypeId(userTypeId);
        return user;
    }

    private static RoleEntity role(String roleName) {
        RoleEntity role = new RoleEntity();
        role.setRoleName(roleName);
        return role;
    }
}
