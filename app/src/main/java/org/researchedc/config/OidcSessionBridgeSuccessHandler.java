package org.researchedc.config;

import java.io.IOException;
import javax.sql.DataSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.login.UserAccountDAO;
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
                    UserAccountDAO dao = new UserAccountDAO(ds);
                    UserAccountBean ub = (UserAccountBean) dao.findByUserName(username);
                    if (ub != null && ub.getId() > 0) {
                        HttpSession session = request.getSession(true);
                        session.setAttribute("userBean", ub);
                        session.setAttribute("user_name", username);
                        log.info("OIDC session bridge: mapped Keycloak user '{}' to UserAccountBean id={}",
                                username, ub.getId());
                    } else {
                        log.warn("OIDC session bridge: no local user found for '{}'", username);
                    }
                }
            }
        }
        delegate.onAuthenticationSuccess(request, response, authentication);
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
