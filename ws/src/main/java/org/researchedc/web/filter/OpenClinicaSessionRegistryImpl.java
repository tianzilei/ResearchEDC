package org.researchedc.web.filter;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.spi.AuditUserLoginDao;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.domain.technicaladmin.AuditUserLoginBean;
import org.researchedc.domain.technicaladmin.LoginStatus;
import org.researchedc.i18n.util.ResourceBundleProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;

import java.util.Date;
import java.util.Locale;

import javax.sql.DataSource;

public class OpenClinicaSessionRegistryImpl extends SessionRegistryImpl {

    AuditUserLoginDao auditUserLoginDao;
    @Autowired
    private IUserAccountDAO userAccountDao;
    DataSource dataSource;

    @Override
    public void removeSessionInformation(String sessionId) {
        SessionInformation info = getSessionInformation(sessionId);

        if (info != null) {
            User u = (User) info.getPrincipal();
            auditLogout(u.getUsername());
        }
        super.removeSessionInformation(sessionId);
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
        return userAccountDao;
    }

    public AuditUserLoginDao getAuditUserLoginDao() {
        return auditUserLoginDao;
    }

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }
}
