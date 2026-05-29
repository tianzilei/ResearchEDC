/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.core.SecureController;
import org.researchedc.core.EmailEngine;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.service.managestudy.EventDefinitionCrfTagService;
import org.researchedc.service.pmanage.Authorization;
import org.researchedc.service.pmanage.ParticipantPortalRegistrar;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;

/**
 * @author jxu
 *
 * Restores a removed study event definition and all its related data
 */
public class RestoreEventDefinitionServlet extends SecureController {
    
    @Autowired
    private CRFVersionDAO crfVersionDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;

EventDefinitionCrfTagService eventDefinitionCrfTagService = null;
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_DEFINITION_SERVLET, respage.getString("current_study_locked"));
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String idString = request.getParameter("id");

        int defId = Integer.valueOf(idString.trim()).intValue();
        IStudyEventDefinitionDAO sdao = this.studyEventDefinitionDao;
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) sdao.findByPK(defId);
        // find all CRFs
        EventDefinitionCRFDao edao = this.eventDefinitionCrfDao;
        ArrayList eventDefinitionCRFs = (ArrayList) edao.findAllByDefinition(defId);

        CRFVersionDAO cvdao = this.crfVersionDao;
        ICrfDAO cdao = this.crfDao;
        for (int i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
            ArrayList versions = (ArrayList) cvdao.findAllByCRF(edc.getCrfId());
            edc.setVersions(versions);
            CRFBean crf = (CRFBean) cdao.findByPK(edc.getCrfId());
            edc.setCrfName(crf.getName());
            CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edc.getDefaultVersionId());
            edc.setDefaultVersionName(defaultVersion.getName());
            CRFBean cBean = (CRFBean) cdao.findByPK(edc.getCrfId());                
            String crfPath=sed.getOid()+"."+cBean.getOid();
            edc.setOffline(getEventDefinitionCrfTagService().getEventDefnCrfOfflineStatus(2,crfPath,true));

        }

        // finds all events
        IStudyEventDAO sedao = this.studyEventDao;
        IStudyParameterValueDAO spvdao = this.studyParameterValueDao;    
        ArrayList events = (ArrayList) sedao.findAllByDefinition(sed.getId());

        String action = request.getParameter("action");
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_SED_to_restore"));
            forwardPage(Page.LIST_DEFINITION_SERVLET);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                if (!sed.getStatus().equals(Status.DELETED)) {
                    addPageMessage(respage.getString("this_SED_cannot_be_restored") + " " + respage.getString("please_contact_sysadmin_for_more_information"));
                    forwardPage(Page.LIST_DEFINITION_SERVLET);
                    return;
                }
                String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
                request.setAttribute("participateFormStatus",participateFormStatus );
                if (participateFormStatus.equals("enabled")) baseUrl();
                            
                request.setAttribute("definitionToRestore", sed);
                request.setAttribute("eventDefinitionCRFs", eventDefinitionCRFs);
                request.setAttribute("events", events);
                forwardPage(Page.RESTORE_DEFINITION);
            } else {
                logger.info("submit to restore the definition");
                // restore definition
                sed.setStatus(Status.AVAILABLE);
                sed.setUpdater(ub);
                sed.setUpdatedDate(new Date());
                sdao.update(sed);

                // restore all crfs
                for (int j = 0; j < eventDefinitionCRFs.size(); j++) {
                    EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(j);
                    if (edc.getStatus().equals(Status.AUTO_DELETED)) {
                        edc.setStatus(Status.AVAILABLE);
                        edc.setUpdater(ub);
                        edc.setUpdatedDate(new Date());
                        edao.update(edc);
                    }
                }
                // restore all events

                EventCRFDao ecdao = this.eventCrfDao;

                for (int j = 0; j < events.size(); j++) {
                    StudyEventBean event = (StudyEventBean) events.get(j);
                    if (event.getStatus().equals(Status.AUTO_DELETED)) {
                        event.setStatus(Status.AVAILABLE);
                        event.setUpdater(ub);
                        event.setUpdatedDate(new Date());
                        sedao.update(event);

                        ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);
                        // remove all the item data
                        IItemDataDAO iddao = this.itemDataDao;
                        for (int k = 0; k < eventCRFs.size(); k++) {
                            EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                            if (eventCRF.getStatus().equals(Status.AUTO_DELETED)) {
                                eventCRF.setStatus(Status.AVAILABLE);
                                eventCRF.setUpdater(ub);
                                eventCRF.setUpdatedDate(new Date());
                                ecdao.update(eventCRF);

                                ArrayList itemDatas = iddao.findAllByEventCRFId(eventCRF.getId());
                                for (int a = 0; a < itemDatas.size(); a++) {
                                    ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                                    if (item.getStatus().equals(Status.AUTO_DELETED)) {
                                        item.setStatus(Status.AVAILABLE);
                                        item.setUpdater(ub);
                                        item.setUpdatedDate(new Date());
                                        iddao.update(item);
                                    }
                                }
                            }
                        }
                    }
                }
                String emailBody =
                    respage.getString("the_SED") + " " + sed.getName() + "(" + respage.getString("and_all_associated_event_data_restored_to_study")
                        + currentStudy.getName() + ".";

                addPageMessage(emailBody);

//                sendEmail(emailBody);
                forwardPage(Page.LIST_DEFINITION_SERVLET);
            }

        }

    }

    /**
     * Send email to director and administrator
     *
     * @param request
     * @param response
     */
    private void sendEmail(String emailBody) throws Exception {

        logger.info("Sending email...");
        // to study director
        boolean emailSent = sendEmail(ub.getEmail().trim(), respage.getString("restore_SED"), emailBody, false);
        // to admin
        if (emailSent) {
            sendEmail(EmailEngine.getAdminEmail(), respage.getString("restore_SED"), emailBody, false);
        }
        logger.info("Sending email done..");
    }

    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
        eventDefinitionCrfTagService=
         this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

         return eventDefinitionCrfTagService;
     }

}
