package org.researchedc.module.identity.service;

import java.util.Collection;
import java.util.stream.Collectors;

import org.researchedc.module.identity.entity.RoleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.identity.repository.RoleRepository;
import org.researchedc.module.identity.repository.UserAccountRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ResearchEdcUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(ResearchEdcUserDetailsService.class);
    private static final int SYSADMIN_USER_TYPE_ID = 1;
    private static final int TECHADMIN_USER_TYPE_ID = 3;

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;

    public ResearchEdcUserDetailsService(UserAccountRepository userAccountRepository,
                                          RoleRepository roleRepository) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccountEntity user = userAccountRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Invalid username or password"));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new DisabledException("Account disabled");
        }

        if (Boolean.FALSE.equals(user.getAccountNonLocked())) {
            throw new LockedException("Account locked");
        }

        Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();
        try {
            authorities = roleRepository.findByUserName(username)
                    .stream()
                    .map(RoleEntity::getRoleName)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.warn("Failed to load roles for user {}: {}", username, e.getMessage());
        }

        if (Integer.valueOf(SYSADMIN_USER_TYPE_ID).equals(user.getUserTypeId())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SYSADMIN"));
        }
        if (Integer.valueOf(TECHADMIN_USER_TYPE_ID).equals(user.getUserTypeId())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TECHADMIN"));
        }

        return new User(
                user.getUserName(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                authorities
        );
    }
}
