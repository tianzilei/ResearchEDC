/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.control.core.SecureController;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.view.Page;
import org.researchedc.web.InconsistentStateException;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

public class ViewUserAccountServlet extends SecureController {
    @Autowired
    IStudyDAO studyDao;

    @Autowired
    private IUserAccountDAO userAccountDao;
    public static final String PATH = "ViewUserAccount";
    public static final String ARG_USER_ID = "userId";

    public static String getLink(int userId) {
        return PATH + '?' + ARG_USER_ID + '=' + userId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("you_may_not_perform_administrative_functions"), "1");
        }

        return;
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int userId = fp.getInt(ARG_USER_ID, true);
        IUserAccountDAO udao = this.userAccountDao;

        UserAccountBean user = getBean(udao, userId);

        techAdminProtect(user);
        if (user.isActive()) {
            request.setAttribute("user", user);
        } else {
            throw new InconsistentStateException(Page.ADMIN_SYSTEM, resexception.getString("the_user_attemping_to_view_not_exists"));
        }
        // BWP>>To provide the view with the correct date format pattern, locale
        // sensitive
        String pattn = "";
        pattn = ResourceBundleProvider.getFormatBundle().getString("date_format_string");
        request.setAttribute("dateFormatPattern", pattn);
        forwardPage(Page.VIEW_USER_ACCOUNT);
    }

    // public void processRequest(HttpServletRequest request,
    // HttpServletResponse response)
    // throws OpenClinicaException {
    // session = request.getSession();
    // session.setMaxInactiveInterval(60 * 60 * 3);
    // logger.setLevel(Level.ALL);
    // UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
    // try {
    // String userName = request.getRemoteUser();
    //
    // sm = new SessionManager(ub, userName);
    // ub = sm.getUserBean();
    // if (logger.isLoggable(Level.INFO)) {
    // logger.info("user bean from DB" + ub.getName());
    // }
    //
    // FormProcessor fp = new FormProcessor(request);
    // int userId = fp.getInt(ARG_USER_ID);
    //
    // SQLFactory factory = SQLFactory.getInstance();
    // IUserAccountDAO udao = this.userAccountDao;
    //
    // UserAccountBean user = getBean(udao, userId);
    //
    // if ((user.getFirstName() != null) && (!user.getFirstName().equals(""))) {
    // request.setAttribute("user", user);
    // request.setAttribute("message", "");
    // }
    // else {
    // request.setAttribute("user", new UserAccountBean());
    // request.setAttribute("message", "The specified user does not exist!");
    // }
    //
    // forwardPage(Page.VIEW_USER_ACCOUNT, request, response);
    // } catch (Exception e) {
    // e.printStackTrace();
    // logger.warn("OpenClinicaException::
    // OpenClinica.control.viewUserAccount: " + e.getMessage());
    //
    // forwardPage(Page.ERROR, request, response);
    // }
    // }

    private UserAccountBean getBean(IUserAccountDAO udao, int id) {
        UserAccountBean answer = (UserAccountBean) udao.findByPK(id);
        IStudyDAO sdao = this.studyDao;

        ArrayList roles = answer.getRoles();

        for (int i = 0; i < roles.size(); i++) {
            StudyUserRoleBean sur = (StudyUserRoleBean) roles.get(i);
            StudyBean sb = (StudyBean) sdao.findByPK(sur.getStudyId());
            sur.setStudyName(sb.getName());
            roles.set(i, sur);
        }
        answer.setRoles(roles);

        return answer;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}
