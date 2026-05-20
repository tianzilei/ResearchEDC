/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.core.EmailEngine;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;

/**
 * Locks a study event definition
 *
 * @author jxu
 *
 */
public class LockEventDefinitionServlet extends SecureController {
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
        throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String idString = request.getParameter("id");

        int defId = Integer.valueOf(idString.trim()).intValue();
        StudyEventDefinitionDAO sdao = new StudyEventDefinitionDAO(sm.getDataSource());
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) sdao.findByPK(defId);
        // find all CRFs
        EventDefinitionCRFDAO edao = new EventDefinitionCRFDAO(sm.getDataSource());
        ArrayList eventDefinitionCRFs = (ArrayList) edao.findAllByDefinition(defId);

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        for (int i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
            ArrayList versions = (ArrayList) cvdao.findAllByCRF(edc.getCrfId());
            edc.setVersions(versions);
            CRFBean crf = (CRFBean) cdao.findByPK(edc.getCrfId());
            edc.setCrfName(crf.getName());
        }

        // finds all events
        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        ArrayList events = (ArrayList) sedao.findAllByDefinition(sed.getId());

        String action = request.getParameter("action");
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_SED_to_lock"));
            forwardPage(Page.LIST_DEFINITION_SERVLET);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                if (!sed.getStatus().equals(Status.AVAILABLE)) {
                    addPageMessage(respage.getString("this_SED_is_not_available_for_this_study")
                        + respage.getString("please_contact_sysadmin_for_more_information"));
                    forwardPage(Page.LIST_DEFINITION_SERVLET);
                    return;
                }

                request.setAttribute("definitionToLock", sed);
                request.setAttribute("eventDefinitionCRFs", eventDefinitionCRFs);
                request.setAttribute("events", events);
                forwardPage(Page.LOCK_DEFINITION);
            } else {
                logger.info("submit to lock the definition");
                // lock definition
                sed.setStatus(Status.LOCKED);
                sed.setUpdater(ub);
                sed.setUpdatedDate(new Date());
                sdao.update(sed);

                // lock all crfs
                for (int j = 0; j < eventDefinitionCRFs.size(); j++) {
                    EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(j);
                    edc.setStatus(Status.LOCKED);
                    edc.setUpdater(ub);
                    edc.setUpdatedDate(new Date());
                    edao.update(edc);
                }
                // lock all events

                EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

                for (int j = 0; j < events.size(); j++) {
                    StudyEventBean event = (StudyEventBean) events.get(j);
                    event.setStatus(Status.LOCKED);
                    event.setUpdater(ub);
                    event.setUpdatedDate(new Date());
                    sedao.update(event);

                    ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);
                    // remove all the item data
                    ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                    for (int k = 0; k < eventCRFs.size(); k++) {
                        EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                        eventCRF.setStatus(Status.LOCKED);
                        eventCRF.setUpdater(ub);
                        eventCRF.setUpdatedDate(new Date());
                        ecdao.update(eventCRF);

                        ArrayList itemDatas = iddao.findAllByEventCRFId(eventCRF.getId());
                        for (int a = 0; a < itemDatas.size(); a++) {
                            ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                            item.setStatus(Status.LOCKED);
                            item.setUpdater(ub);
                            item.setUpdatedDate(new Date());
                            iddao.update(item);
                        }
                    }
                }

                String emailBody =
                    respage.getString("the_SED") + sed.getName() + respage.getString("has_been_locked_for_the_study") + currentStudy.getName()
                        + respage.getString("no_new_data_may_entered_for_this_SED");

                addPageMessage(emailBody);
                sendEmail(emailBody);
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
        sendEmail(ub.getEmail().trim(), respage.getString("lock_SED"), emailBody, false);
        // to admin
        sendEmail(EmailEngine.getAdminEmail(), respage.getString("lock_SED"), emailBody, false);
        logger.info("Sending email done..");
    }

}
