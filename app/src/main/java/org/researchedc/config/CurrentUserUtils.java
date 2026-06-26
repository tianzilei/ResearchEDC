package org.researchedc.config;

import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.identity.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility to extract the current authenticated user's {@code userId} (Integer primary key
 * from the {@code user_account} table) from the Spring Security context.
 *
 * <p>Supports the current session-based authentication flow:
 * <ul>
 *   <li><b>Session / form login</b>: reads {@link UserDetails#getUsername()} from the
 *       principal and resolves it via {@link UserAccountRepository#findByUserName}.</li>
 * </ul>
 */
@Component
public class CurrentUserUtils {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserUtils.class);

    private final UserAccountRepository userAccountRepository;

    public CurrentUserUtils(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Returns the {@code userId} (primary key of {@code user_account}) for the currently
     * authenticated user.
     *
     * @throws IllegalStateException if no authentication is found or the username cannot
     *                               be resolved to a local user account
     */
    public Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException(
                    "Cannot determine current user: no authenticated user found in security context");
        }

        String username = null;
        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            username = auth.getName();
        }
        if (username != null && !username.isBlank()) {
            return resolveUserId(username);
        }

        throw new IllegalStateException(
                "Cannot determine current user: unsupported authentication type: "
                        + auth.getClass().getName());
    }

    private Integer resolveUserId(String username) {
        return userAccountRepository.findByUserName(username)
                .map(UserAccountEntity::getUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "No local user account found for username: " + username));
    }
}
