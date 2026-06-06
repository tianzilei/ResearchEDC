package org.researchedc.web.filter;

import java.util.Date;
import java.util.Locale;

import javax.sql.DataSource;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.core.CRFLocker;
import org.researchedc.dao.spi.AuditUserLoginDao;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.domain.technicaladmin.AuditUserLoginBean;
import org.researchedc.domain.technicaladmin.LoginStatus;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.beans.factory.annotation.Autowired;

public class OpenClinicaSessionRegistryImpl extends SessionRegistryImpl {

    AuditUserLoginDao auditUserLoginDao;
    @Autowired
    IUserAccountDAO userAccountDao;
    DataSource dataSource;
    CRFLocker crfLocker;

    @Override
    public void removeSessionInformation(String sessionId) {
        SessionInformation info = getSessionInformation(sessionId);

        if (info != null) {
            String username = null;
            Object p = info.getPrincipal();
            if (p instanceof User) {
                username = ((User) p).getUsername();
            } else if (p instanceof LdapUserDetails) {
                username = ((LdapUserDetails) p).getUsername();
            }

            auditLogout(username);
        }
        super.removeSessionInformation(sessionId);
    }

    void auditLogout(String username) {
        ResourceBundleProvider.updateLocale(Locale.of("en_US"));
        UserAccountBean userAccount = (UserAccountBean) getUserAccountDao().findByUserName(username);
        crfLocker.unlockAllForUser(userAccount.getId());

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

    public void setCrfLocker(CRFLocker crfLocker) {
        this.crfLocker = crfLocker;
    }
}
