/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.control.core.SecureController;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.submit.SubmitDataServlet;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.service.StudyConfigService;
import org.researchedc.service.pmanage.ParticipantPortalRegistrar;
import org.researchedc.service.pmanage.RandomizationRegistrar;
import org.researchedc.service.pmanage.SeRandomizationDTO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;

/**
 * @author jxu
 *
 * Processes the reuqest of 'view study details'
 */
public class ViewStudyServlet extends SecureController {
    @Autowired
    private IUserAccountDAO userAccountDao;
    @Autowired
    private EventDefinitionCRFDAO eventDefinitionCrfDao;

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        IStudyDAO sdao = this.studyDao;
        FormProcessor fp = new FormProcessor(request);
        int studyId = fp.getInt("id");
        if (studyId == 0) {
            addPageMessage(respage.getString("please_choose_a_study_to_view"));
            forwardPage(Page.STUDY_LIST_SERVLET);
        } else {
            if (currentStudy.getId() != studyId && currentStudy.getParentStudyId() != studyId) {
                checkRoleByUserAndStudy(ub, studyId, 0);
            }

            String viewFullRecords = fp.getString("viewFull");
            StudyBean study = (StudyBean) sdao.findByPK(studyId);


            study = studyConfigService.setParametersForStudy(study);

            IStudyParameterValueDAO spvdao = this.studyParameterValueDao;
            String randomizationStatusInOC = spvdao.findByHandleAndStudy(study.getId(), "randomization").getValue();
            String participantStatusInOC = spvdao.findByHandleAndStudy(study.getId(), "participantPortal").getValue();
            if(participantStatusInOC=="") participantStatusInOC="disabled";
            if(randomizationStatusInOC=="") randomizationStatusInOC="disabled";

            RandomizationRegistrar randomizationRegistrar = new RandomizationRegistrar();
            SeRandomizationDTO seRandomizationDTO = randomizationRegistrar.getCachedRandomizationDTOObject(study.getOid(), false);

            if (seRandomizationDTO!=null && seRandomizationDTO.getStatus().equalsIgnoreCase("ACTIVE") && randomizationStatusInOC.equalsIgnoreCase("enabled")){
                study.getStudyParameterConfig().setRandomization("enabled");
            }else{
                study.getStudyParameterConfig().setRandomization("disabled");
             };


             ParticipantPortalRegistrar  participantPortalRegistrar = new ParticipantPortalRegistrar();
             String pStatus = participantPortalRegistrar.getCachedRegistrationStatus(study.getOid(), session);
             if (participantPortalRegistrar!=null && pStatus.equalsIgnoreCase("ACTIVE") && participantStatusInOC.equalsIgnoreCase("enabled")){
                 study.getStudyParameterConfig().setParticipantPortal("enabled");
             }else{
                 study.getStudyParameterConfig().setParticipantPortal("disabled");
              };


            request.setAttribute("studyToView", study);
            if ("yes".equalsIgnoreCase(viewFullRecords)) {
                IUserAccountDAO udao = this.userAccountDao;
                IStudySubjectDAO ssdao = this.studySubjectDao;
                ArrayList sites = new ArrayList();
                ArrayList userRoles = new ArrayList();
                ArrayList subjects = new ArrayList();
                if (this.currentStudy.getParentStudyId() > 0 && this.currentRole.getRole().getId() > 3) {
                    sites.add(this.currentStudy);
                    userRoles = udao.findAllUsersByStudy(currentStudy.getId());
                    subjects = ssdao.findAllByStudy(currentStudy);
                } else {
                    sites = (ArrayList) sdao.findAllByParent(studyId);
                    userRoles = udao.findAllUsersByStudy(studyId);
                    subjects = ssdao.findAllByStudy(study);
                }

                // find all subjects in the study, include ones in sites
                IStudyEventDefinitionDAO seddao = this.studyEventDefinitionDao;
                EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;
                // IStudyEventDAO sedao = this.studyEventDao;

//                ArrayList displayStudySubs = new ArrayList();
//                for (int i = 0; i < subjects.size(); i++) {
//                    StudySubjectBean studySub = (StudySubjectBean) subjects.get(i);
//                    // find all events
//                    ArrayList events = sedao.findAllByStudySubject(studySub);
//
//                    // find all eventcrfs for each event
//                    EventCRFDao ecdao = this.eventCrfDao;
//
//                    DisplayStudySubjectBean dssb = new DisplayStudySubjectBean();
//                    dssb.setStudyEvents(events);
//                    dssb.setStudySubject(studySub);
//                    displayStudySubs.add(dssb);
//                }

                // find all events in the study, include ones in sites
                ArrayList definitions = seddao.findAllByStudy(study);

                for (int i = 0; i < definitions.size(); i++) {
                    StudyEventDefinitionBean def = (StudyEventDefinitionBean) definitions.get(i);
                    ArrayList crfs = (ArrayList) edcdao.findAllActiveParentsByEventDefinitionId(def.getId());
                    def.setCrfNum(crfs.size());

                }
                String moduleManager = CoreResources.getField("moduleManager");
                request.setAttribute("moduleManager", moduleManager);

                String portalURL = CoreResources.getField("portalURL");
                request.setAttribute("portalURL", portalURL);

                request.setAttribute("config", study);

                request.setAttribute("sitesToView", sites);
                request.setAttribute("siteNum", sites.size() + "");

                request.setAttribute("userRolesToView", userRoles);
                request.setAttribute("userNum", userRoles.size() + "");

                // request.setAttribute("subjectsToView", displayStudySubs);
                // request.setAttribute("subjectNum", subjects.size() + "");

                request.setAttribute("definitionsToView", definitions);
                request.setAttribute("defNum", definitions.size() + "");
                forwardPage(Page.VIEW_FULL_STUDY);

            } else {
                forwardPage(Page.VIEW_STUDY);
            }
        }
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

}
