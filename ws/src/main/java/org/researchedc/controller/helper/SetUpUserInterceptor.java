package org.researchedc.controller.helper;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.service.StudyConfigService;
import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * An "interceptor" class that sets up a UserAccount and stores it in the Session, before
 * another class is initialized and potentially uses that UserAccount.
 */
public class SetUpUserInterceptor implements HandlerInterceptor {

    public static final String USER_BEAN_NAME = "userBean";

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    private UserAccountDAO userAccountDAO;
    @Autowired
    private IStudyDAO studyDao;
    @Autowired
    private StudyParameterValueDAO studyParameterValueDao;
    @Autowired
    private StudyConfigService studyConfigService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        Locale locale = ResourceBundleProvider.localeMap.get(Thread.currentThread());
        if (locale == null) {
            ResourceBundleProvider.updateLocale(httpServletRequest.getLocale());
        }

        //Set up the user account bean: check the Session first
        HttpSession currentSession = httpServletRequest.getSession();
        UserAccountBean userBean = (UserAccountBean) currentSession.getAttribute("userBean");
        String userName = "";
        boolean userBeanIsInvalid;

        if (userBean == null) {

            userName = httpServletRequest.getRemoteUser();
            userBeanIsInvalid = "".equalsIgnoreCase(userName);
            if (!userBeanIsInvalid) {
                userBean = (UserAccountBean) userAccountDAO.findByUserName(userName);
                userBeanIsInvalid = (userBean == null);
                if (!userBeanIsInvalid) {
                    currentSession.setAttribute(USER_BEAN_NAME, userBean);
                }

            }
        }

        //The user bean could still be null at this point
        if (userBean == null) {
            userBean = new UserAccountBean();
            userBean.setName("unknown");
            currentSession.setAttribute(USER_BEAN_NAME, userBean);
        }

        userBean = userBean.getId() > 0 ? (UserAccountBean) userAccountDAO.findByPK(userBean.getId()) : userBean;

        SetUpStudyRole setupStudy = new SetUpStudyRole(dataSource, studyDao, studyParameterValueDao, studyConfigService);
        setupStudy.setUp(currentSession, userBean);

        return true;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
