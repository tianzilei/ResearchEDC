package org.researchedc.web.filter;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.spi.AuditUserLoginDao;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.domain.technicaladmin.AuditUserLoginBean;
import org.researchedc.domain.technicaladmin.LoginStatus;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Date;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Call Super Class SecurityContextLogoutHandler that Performs a logout by modifying the {@see org.springframework.security.context.SecurityContextHolder}.
 * <p>
 * Will log this event to an OpenClinica user logging table
 * 
 * @author Krikor Krumlian
 */
public class OpenClinicaSecurityContextLogoutHandler extends SecurityContextLogoutHandler {

    AuditUserLoginDao auditUserLoginDao;
    @Autowired
    IUserAccountDAO userAccountDao;
    DataSource dataSource;

    // ~ Methods ========================================================================================================

    /**
     * Requires the request to be passed in.
     * 
     * @param request
     *            from which to obtain a HTTP session (cannot be null)
     * @param response
     *            not used (can be <code>null</code>)
     * @param authentication
     *            not used (can be <code>null</code>)
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            auditLogout(authentication.getName());
        }
        super.logout(request, response, authentication);
    }

    void auditLogout(String username) {
        ResourceBundleProvider.updateLocale(Locale.of("en_US"));
        UserAccountBean userAccount = (UserAccountBean) getUserAccountDao().findByUserName(username);
        AuditUserLoginBean auditUserLogin = new AuditUserLoginBean();
        auditUserLogin.setUserName(username);
        auditUserLogin.setLoginStatus(LoginStatus.SUCCESSFUL_LOGOUT);
        auditUserLogin.setLoginAttemptDate(new Date());
        auditUserLogin.setUserAccountId(userAccount != null ? userAccount.getId() : null);
        getAuditUserLoginDao().saveOrUpdate(auditUserLogin);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public IUserAccountDAO getUserAccountDao() {
        return userAccountDao != null ? userAccountDao : this.userAccountDao;
    }

    public AuditUserLoginDao getAuditUserLoginDao() {
        return auditUserLoginDao;
    }

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

}
