package org.researchedc.config;

import java.io.IOException;
import javax.sql.DataSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.researchedc.bean.core.Status;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class OidcSessionBridgeSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OidcSessionBridgeSuccessHandler.class);

    private final SavedRequestAwareAuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();

    public OidcSessionBridgeSuccessHandler() {
        delegate.setDefaultTargetUrl("/MainMenu");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String username = oidcUser.getPreferredUsername();
            if (username != null) {
                DataSource ds = resolveDataSource(request);
                if (ds != null) {
                    IUserAccountDAO dao = new UserAccountDAO(ds);
                    UserAccountBean ub = (UserAccountBean) dao.findByUserName(username);
                    if (ub == null || ub.getId() <= 0) {
                        // JIT provisioning: create local user from OIDC claims
                        ub = createUserFromOidc(dao, oidcUser, username);
                    }
                    if (ub != null && ub.getId() > 0) {
                        HttpSession session = request.getSession(true);
                        session.setAttribute("userBean", ub);
                        session.setAttribute("user_name", username);
                        log.info("OIDC session bridge: mapped Keycloak user '{}' to UserAccountBean id={}",
                                username, ub.getId());
                    } else {
                        log.warn("OIDC session bridge: could not create local user for '{}'", username);
                    }
                }
            }
        }
        delegate.onAuthenticationSuccess(request, response, authentication);
    }

    private UserAccountBean createUserFromOidc(IUserAccountDAO dao, OidcUser oidcUser, String username) {
        try {
            UserAccountBean ub = new UserAccountBean();
            ub.setName(username);
            ub.setFirstName(oidcUser.getGivenName() != null ? oidcUser.getGivenName() : username);
            ub.setLastName(oidcUser.getFamilyName() != null ? oidcUser.getFamilyName() : "User");
            ub.setEmail(oidcUser.getEmail() != null ? oidcUser.getEmail() : username + "@researchedc.org");
            ub.setEnabled(true);
            ub.setStatus(Status.AVAILABLE);
            ub.setPasswd("");
            ub.setPasswdTimestamp(new java.util.Date());
            ub.setCreatedDate(new java.util.Date());
            UserAccountBean created = (UserAccountBean) dao.create(ub);
            log.info("JIT provisioned local user '{}' from OIDC (id={})", username, created.getId());
            return created;
        } catch (Exception e) {
            log.error("Failed to JIT provision local user '{}': {}", username, e.getMessage());
            return null;
        }
    }

    private DataSource resolveDataSource(HttpServletRequest request) {
        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(request.getServletContext());
        if (ctx != null) {
            return ctx.getBean(DataSource.class);
        }
        return null;
    }
}
