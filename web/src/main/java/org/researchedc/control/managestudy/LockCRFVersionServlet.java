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
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.admin.RemoveCRFVersionServlet;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.ICrfDAO;

public class LockCRFVersionServlet extends SecureController {
    
    @Autowired
    private ICrfVersionDAO crfVersionDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;

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
       
       int crfVersionId = fp.getInt("id",true);
       String action = fp.getString("action");
       
       // checks which module the requests are from
       String module = fp.getString(MODULE);
       request.setAttribute(MODULE, module);
       
       if(crfVersionId ==0) {
           addPageMessage(respage.getString("choose_valid_crf_version"));
           forwardPage(Page.CRF_LIST_SERVLET);
           return;
       }
       
       ICrfVersionDAO cvdao = this.crfVersionDao;
       ICrfDAO cdao = this.crfDao;
       
       CRFVersionBean version = (CRFVersionBean)cvdao.findByPK(crfVersionId);
       //System.out.println("crf version found:" + version.getName());
       CRFBean crf = (CRFBean)cdao.findByPK(version.getCrfId());

       if (!ub.isSysAdmin() && (version.getOwnerId() != ub.getId())) {
           addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                   + " " + respage.getString("change_active_study_or_contact"));
           forwardPage(Page.MENU_SERVLET);
           return;
       }
      
       EventCRFDao ecdao = this.eventCrfDao;
       ArrayList eventCRFs = ecdao.findAllStudySubjectByCRFVersion(crfVersionId);
       
       if (StringUtil.isBlank(action)) {
           request.setAttribute("crfVersionToLock", version);
           request.setAttribute("crf", crf);
           request.setAttribute("eventSubjectsUsingVersion", eventCRFs);
           forwardPage(Page.CONFIRM_LOCKING_CRF_VERSION);
           
       } else if ("confirm".equalsIgnoreCase(action)) {
           version.setStatus(Status.LOCKED);
           version.setUpdater(ub);
           cvdao.update(version);

           ArrayList versionList = (ArrayList)cvdao.findAllByCRF(version.getCrfId());
           if(versionList.size() > 0){
               EventDefinitionCRFDao edCRFDao = this.eventDefinitionCrfDao;
               ArrayList edcList = (ArrayList)edCRFDao.findAllByCRF(version.getCrfId());
               for(int i = 0; i < edcList.size(); i++){
                   EventDefinitionCRFBean edcBean = (EventDefinitionCRFBean)edcList.get(i);
                    // @pgawade 18-May-2011 #5414 - Changes for setting the correct
                    // default crf version Id to event
                    // when existing default version is locked
                    // RemoveCRFVersionServlet.updateEventDef(edcBean, edCRFDao,
                    // versionList);
                    RemoveCRFVersionServlet.updateEventDef(edcBean, edCRFDao, versionList, crfVersionId);
               }
           }

           addPageMessage(respage.getString("crf_version_archived_successfully"));
           forwardPage(Page.CRF_LIST_SERVLET);
       }
   }

}
