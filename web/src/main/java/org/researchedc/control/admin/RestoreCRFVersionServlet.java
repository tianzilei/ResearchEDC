/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RestoreCRFVersionServlet extends SecureController {

    @Autowired
    private CRFVersionDAO crfVersionDao;
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

        CRFVersionDAO cvdao = this.crfVersionDao;
        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int versionId = fp.getInt("id", true);

        String action = fp.getString("action");
        if (versionId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_version_to_restore"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            if (StringUtil.isBlank(action)) {
                addPageMessage(respage.getString("no_action_specified"));
                forwardPage(Page.CRF_LIST_SERVLET);
                return;
            }
            CRFVersionBean version = (CRFVersionBean) cvdao.findByPK(versionId);

            SectionDAO secdao = this.sectionDao;

            EventCRFDao evdao = this.eventCrfDao;
            // find all event crfs by version id
            ArrayList eventCRFs = evdao.findAllByCRFVersion(versionId);
            if ("confirm".equalsIgnoreCase(action)) {
                request.setAttribute("versionToRestore", version);
                request.setAttribute("eventCRFs", eventCRFs);
                forwardPage(Page.RESTORE_CRF_VERSION);
            } else {
                logger.info("submit to restore the crf version");
                // version
                version.setStatus(Status.AVAILABLE);
                version.setUpdater(ub);
                version.setUpdatedDate(new Date());
                cvdao.update(version);
                // all sections
                ArrayList sections = secdao.findAllByCRFVersionId(version.getId());
                for (int j = 0; j < sections.size(); j++) {
                    SectionBean section = (SectionBean) sections.get(j);
                    if (section.getStatus().equals(Status.AUTO_DELETED)) {
                        section.setStatus(Status.AVAILABLE);
                        section.setUpdater(ub);
                        section.setUpdatedDate(new Date());
                        secdao.update(section);
                    }
                }

                // all item data related to event crfs
                ItemDataDAO idao = this.itemDataDao;
                for (int i = 0; i < eventCRFs.size(); i++) {
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(i);
                    if (eventCRF.getStatus().equals(Status.AUTO_DELETED)) {
                        eventCRF.setStatus(Status.AVAILABLE);
                        eventCRF.setUpdater(ub);
                        eventCRF.setUpdatedDate(new Date());
                        evdao.update(eventCRF);

                        ArrayList items = idao.findAllByEventCRFId(eventCRF.getId());
                        for (int j = 0; j < items.size(); j++) {
                            ItemDataBean item = (ItemDataBean) items.get(j);
                            if (item.getStatus().equals(Status.AUTO_DELETED)) {
                                item.setStatus(Status.AVAILABLE);
                                item.setUpdater(ub);
                                item.setUpdatedDate(new Date());
                                idao.update(item);
                            }
                        }

                    }
                }
                addPageMessage(respage.getString("the_CRF_version") + version.getName() + " " + respage.getString("has_been_restored_succesfully"));
                forwardPage(Page.CRF_LIST_SERVLET);

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
