/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.service.StudyParameterValueBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.EventDefinitionCrfTagDao;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.domain.datamap.CrfBean;
import org.researchedc.service.managestudy.EventDefinitionCrfTagService;
import org.researchedc.service.pmanage.Authorization;
import org.researchedc.service.pmanage.ParticipantPortalRegistrar;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * View the details of a study event definition
 *
 * @author jxu
 *
 */
public class ViewEventDefinitionServlet extends SecureController {
   
    @Autowired
    protected ICrfVersionDAO crfVersionDao;
    @Autowired
    protected EventDefinitionCRFDao eventDefinitionCrfDao;

EventDefinitionCrfTagService eventDefinitionCrfTagService = null;
   
    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        IStudyEventDefinitionDAO sdao = this.studyEventDefinitionDao;
        IStudyDAO studyDao = this.studyDao;
        FormProcessor fp = new FormProcessor(request);
        int defId = fp.getInt("id", true);

        if (defId == 0) {
            addPageMessage(respage.getString("please_choose_a_definition_to_view"));
            forwardPage(Page.LIST_DEFINITION_SERVLET);
        } else {
            // definition id
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) sdao.findByPK(defId);

            if (currentStudy.getId() != sed.getStudyId()) {
                addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                        + " " + respage.getString("change_active_study_or_contact"));
                forwardPage(Page.MENU_SERVLET);
                return;
            }
            
            checkRoleByUserAndStudy(ub, sed.getStudyId(), 0);

            EventDefinitionCRFDao edao = this.eventDefinitionCrfDao;
            ArrayList eventDefinitionCRFs = (ArrayList) edao.findAllByDefinition(this.currentStudy, defId);

            ICrfVersionDAO cvdao = this.crfVersionDao;
            ICrfDAO cdao = this.crfDao;

            for (int i = 0; i < eventDefinitionCRFs.size(); i++) {
                EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
                ArrayList versions = (ArrayList) cvdao.findAllByCRF(edc.getCrfId());
                edc.setVersions(versions);
                CRFBean crf = (CRFBean) cdao.findByPK(edc.getCrfId());
                // edc.setCrfLabel(crf.getLabel());
                edc.setCrfName(crf.getName());
                // to show/hide edit action on jsp page
                if (crf.getStatus().equals(Status.AVAILABLE)) {
                    edc.setOwner(crf.getOwner());
                }

                CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edc.getDefaultVersionId());
                edc.setDefaultVersionName(defaultVersion.getName());
  
                CRFBean cBean = (CRFBean) cdao.findByPK(edc.getCrfId());                
                String crfPath=sed.getOid()+"."+cBean.getOid();
                edc.setOffline(getEventDefinitionCrfTagService().getEventDefnCrfOfflineStatus(2,crfPath,true));
            }
            
            IStudyParameterValueDAO spvdao = this.studyParameterValueDao;    
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();       
            request.setAttribute("participateFormStatus",participateFormStatus );       
            if (participateFormStatus.equals("enabled")) baseUrl();

            request.setAttribute("participateFormStatus",participateFormStatus );

            
            
            request.setAttribute("definition", sed);
            request.setAttribute("eventDefinitionCRFs", eventDefinitionCRFs);
            request.setAttribute("defSize", Integer.valueOf(eventDefinitionCRFs.size()));
            // request.setAttribute("eventDefinitionCRFs", new
            // ArrayList(tm.values()));
            forwardPage(Page.VIEW_EVENT_DEFINITION);
        }

    }

    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
           eventDefinitionCrfTagService=
            this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

            return eventDefinitionCrfTagService;
        }

}
