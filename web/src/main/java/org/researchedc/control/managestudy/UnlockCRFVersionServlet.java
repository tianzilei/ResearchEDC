/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import java.util.ArrayList;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.ICrfDAO;

public class UnlockCRFVersionServlet extends SecureController {
    
    @Autowired
    private ICrfVersionDAO crfVersionDao;

/**
    *
    */
   @Override
   public void mayProceed() throws InsufficientPermissionException {
       if (ub.isSysAdmin()) {
           return;
       }

       if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
           return;
       }

       addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
       throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

   }
   
   @Override
   public void processRequest() throws Exception {
       FormProcessor fp = new FormProcessor(request);
       
       int crfVersionId = fp.getInt("id");
       String action = fp.getString("action");
       
       // checks which module the requests are from
       String module = fp.getString(MODULE);
       request.setAttribute(MODULE, module);
       
       if(crfVersionId ==0) {
           addPageMessage(respage.getString("no_have_correct_privilege_current_study"));
           forwardPage(Page.CRF_LIST_SERVLET);
           return;
       }
       
       ICrfVersionDAO cvdao = this.crfVersionDao;
        ICrfDAO cdao = this.crfDao;
       
       CRFVersionBean version = (CRFVersionBean)cvdao.findByPK(crfVersionId);
       CRFBean crf = (CRFBean)cdao.findByPK(version.getCrfId());
       
       EventCRFDao ecdao = this.eventCrfDao;
       ArrayList eventCRFs = ecdao.findAllStudySubjectByCRFVersion(crfVersionId);
       
       if (StringUtil.isBlank(action)) {
           request.setAttribute("crfVersionToUnlock", version);
           request.setAttribute("crf", crf);
           request.setAttribute("eventSubjectsUsingVersion", eventCRFs);
           forwardPage(Page.CONFIRM_UNLOCKING_CRF_VERSION);
           
       } else if ("confirm".equalsIgnoreCase(action)) {
           version.setStatus(Status.AVAILABLE);
           version.setUpdater(ub);
           cvdao.update(version);
           addPageMessage(respage.getString("crf_version_unarchived_successfully"));
           forwardPage(Page.CRF_LIST_SERVLET);
       }
   }

}
