package org.researchedc.config;

import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.identity.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Utility to extract the current authenticated user's {@code userId} (Integer primary key
 * from the {@code user_account} table) from the Spring Security context.
 *
 * <p>Supports two authentication modes:
 * <ul>
 *   <li><b>JWT</b> (API endpoints behind {@code SecurityConfig} chain 1):
 *       extracts {@code preferred_username} from the {@link Jwt} token and resolves it
 *       via {@link UserAccountRepository#findByUserName}.</li>
 *   <li><b>Session / form login</b> (legacy web flow): reads {@link UserDetails#getUsername()}
 *       from the principal and resolves it the same way.</li>
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

        // Chain 1: JWT / OAuth2 resource-server (used by /api/** endpoints)
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String username = jwt.getClaimAsString("preferred_username");
            if (username != null) {
                return resolveUserId(username);
            }
        }

        // Chain 2: Session-based / form-login principal (legacy web flow)
        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            if (username != null) {
                return resolveUserId(username);
            }
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
