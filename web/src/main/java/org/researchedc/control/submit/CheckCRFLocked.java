package org.researchedc.control.submit;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.web.util.HtmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * Created by IntelliJ IDEA.
 * User: A. Hamid
 * Date: Apr 12, 2010
 * Time: 3:32:44 PM
 */
public class CheckCRFLocked extends SecureController {
    @Override
    protected void processRequest() throws Exception {
        int userId;
        String ecId = request.getParameter("ecId");
        if (ecId != null && !ecId.equals("")) {
            int crfId = Integer.parseInt(ecId);
            if (getCrfLocker().isLocked(crfId)) {
                userId = getCrfLocker().getLockOwner(crfId);
                IUserAccountDAO udao = this.userAccountDao;
                UserAccountBean ubean = (UserAccountBean)udao.findByPK(userId);
                response.getWriter().print(HtmlUtils.htmlEscape(resword.getString("CRF_unavailable")) +
                        "\n"+HtmlUtils.htmlEscape(ubean.getName()) + " "+ HtmlUtils.htmlEscape(resword.getString("Currently_entering_data"))
                        + "\n"+HtmlUtils.htmlEscape(resword.getString("Leave_the_CRF")));
            } else {
                response.getWriter().print("true");
            }
            return;
        }else if(request.getParameter("userId")!=null) {
            getCrfLocker().unlockAllForUser(Integer.parseInt(request.getParameter("userId")));
            if(request.getParameter("exitTo")!=null){
                response.sendRedirect(request.getParameter("exitTo"));
            }else{
                response.sendRedirect("ListStudySubjects");
            }

        }
    }
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        return;
    }
}
