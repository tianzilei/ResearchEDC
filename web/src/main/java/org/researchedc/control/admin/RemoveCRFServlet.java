/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.control.core.SecureController;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;

/**
 * Removes a crf
 *
 * @author jxu
 */
public class RemoveCRFServlet extends SecureController {
    @Autowired
    IStudyEventDAO studyEventDao;
    @Autowired
    IStudyEventDefinitionDAO studyEventDefinitionDao;
    @Autowired
    ICrfDAO crfDao;
    @Autowired
    EventCRFDao eventCrfDao;
    @Autowired
    ItemDataDAO itemDataDao;

    @Autowired
    private ICrfVersionDAO crfVersionDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;
    @Autowired
    private SectionDAO sectionDao;
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
        throw new InsufficientPermissionException(Page.CRF_LIST_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        ICrfDAO cdao = this.crfDao;
        ICrfVersionDAO cvdao = this.crfVersionDao;
        FormProcessor fp = new FormProcessor(request);

        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int crfId = fp.getInt("id", true);

        String action = request.getParameter("action");
        if (crfId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_remove"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            CRFBean crf = (CRFBean) cdao.findByPK(crfId);
            ArrayList versions = cvdao.findAllByCRFId(crfId);
            crf.setVersions(versions);
            EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;
            ArrayList edcs = (ArrayList) edcdao.findAllByCRF(crfId);

            SectionDAO secdao = this.sectionDao;

            EventCRFDao evdao = this.eventCrfDao;
            ArrayList eventCRFs = evdao.findAllByCRF(crfId);
            IStudyEventDAO seDao = this.studyEventDao;
            IStudyEventDefinitionDAO sedDao = this.studyEventDefinitionDao;
            for (Object ecBean: eventCRFs) {
                StudyEventBean seBean = (StudyEventBean) seDao.findByPK(((EventCRFBean)ecBean).getStudyEventId());
                StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean)sedDao.findByPK(seBean.getStudyEventDefinitionId());
                ((EventCRFBean)ecBean).setEventName(sedBean.getName());
            }
            if ("confirm".equalsIgnoreCase(action)) {
                request.setAttribute("crfToRemove", crf);
                request.setAttribute("eventCRFs", eventCRFs);
                forwardPage(Page.REMOVE_CRF);
            } else {
                logger.info("submit to remove the crf");
                crf.setStatus(Status.DELETED);
                crf.setUpdater(ub);
                crf.setUpdatedDate(new Date());
                cdao.update(crf);

                for (int i = 0; i < versions.size(); i++) {
                    CRFVersionBean version = (CRFVersionBean) versions.get(i);
                    if (!version.getStatus().equals(Status.DELETED)) {
                        version.setStatus(Status.AUTO_DELETED);
                        version.setUpdater(ub);
                        version.setUpdatedDate(new Date());
                        cvdao.update(version);

                        ArrayList sections = secdao.findAllByCRFVersionId(version.getId());
                        for (int j = 0; j < sections.size(); j++) {
                            SectionBean section = (SectionBean) sections.get(j);
                            if (!section.getStatus().equals(Status.DELETED)) {
                                section.setStatus(Status.AUTO_DELETED);
                                section.setUpdater(ub);
                                section.setUpdatedDate(new Date());
                                secdao.update(section);
                            }
                        }
                    }
                }

                for (int i = 0; i < edcs.size(); i++) {
                    EventDefinitionCRFBean edc = (EventDefinitionCRFBean) edcs.get(i);
                    if (!edc.getStatus().equals(Status.DELETED)) {
                        edc.setStatus(Status.AUTO_DELETED);
                        edc.setUpdater(ub);
                        edc.setUpdatedDate(new Date());
                        edcdao.update(edc);
                    }
                }

                IItemDataDAO idao = this.itemDataDao;
                for (int i = 0; i < eventCRFs.size(); i++) {
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(i);
                    if (!eventCRF.getStatus().equals(Status.DELETED)) {
                        eventCRF.setStatus(Status.AUTO_DELETED);
                        eventCRF.setUpdater(ub);
                        eventCRF.setUpdatedDate(new Date());
                        evdao.update(eventCRF);

                        ArrayList items = idao.findAllByEventCRFId(eventCRF.getId());
                        for (int j = 0; j < items.size(); j++) {
                            ItemDataBean item = (ItemDataBean) items.get(j);
                            if (!item.getStatus().equals(Status.DELETED)) {
                                item.setStatus(Status.AUTO_DELETED);
                                item.setUpdater(ub);
                                item.setUpdatedDate(new Date());
                                idao.update(item);
                            }
                        }
                    }
                }

                addPageMessage(respage.getString("the_CRF") + crf.getName() + " " + respage.getString("has_been_removed_succesfully"));
                forwardPage(Page.CRF_LIST_SERVLET);

            }
        }

    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

}
