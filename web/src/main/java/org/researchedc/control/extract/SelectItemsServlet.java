/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.extract;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.extract.DatasetBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.dao.spi.StudyGroupDao;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jxu
 *
 *
 */
public class SelectItemsServlet extends SecureController {

    @Autowired
    protected ItemFormMetadataDAO itemFormMetadataDao;

    Locale locale;
    // < ResourceBundlerestext,resexception,respage;

    public static String CURRENT_DEF_ID = "currentDefId";

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("org.researchedc.i18n.notes",locale);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",
        // locale);
        // <
        // resexception=ResourceBundle.getBundle(
        // "org.researchedc.i18n.exceptions",locale);

        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");

    }

    public void setUpStudyGroupPage() {
        ArrayList sgclasses = (ArrayList) session.getAttribute("allSelectedGroups");
        if (sgclasses == null || sgclasses.size() == 0) {
            IStudyDAO studydao = this.studyDao;
            StudyGroupClassDao sgclassdao = this.studyGroupClassDao;
            StudyBean theStudy = (StudyBean) studydao.findByPK(sm.getUserBean().getActiveStudyId());
            sgclasses = sgclassdao.findAllActiveByStudy(theStudy);

            StudyGroupDao sgdao = this.studyGroupDao;

            for (int i = 0; i < sgclasses.size(); i++) {
                StudyGroupClassBean sgclass = (StudyGroupClassBean) sgclasses.get(i);
                ArrayList studyGroups = sgdao.findAllByGroupClass(sgclass);
                sgclass.setStudyGroups(studyGroups);
                // hmm, set it back into the array list? tbh
            }
        }
        session.setAttribute("allSelectedGroups", sgclasses);
        request.setAttribute("allSelectedGroups", sgclasses);
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int crfId = fp.getInt("crfId");
        int defId = fp.getInt("defId");
        int eventAttr = fp.getInt("eventAttr");
        int subAttr = fp.getInt("subAttr");
        int CRFAttr = fp.getInt("CRFAttr");
        int groupAttr = fp.getInt("groupAttr");
        int discAttr = fp.getInt("discAttr");
        ICrfDAO crfdao = this.crfDao;
        ItemDAO idao = this.itemDao;
        ItemFormMetadataDAO imfdao = this.itemFormMetadataDao;
        IStudyEventDefinitionDAO seddao = this.studyEventDefinitionDao;

        HashMap events = (HashMap) session.getAttribute(CreateDatasetServlet.EVENTS_FOR_CREATE_DATASET);
        if (events == null) {
            events = new HashMap();
        }
        request.setAttribute("eventlist", events);
        logger.info("found dob setting: " + currentStudy.getStudyParameterConfig().getCollectDob());
        // System.out.println("found dob setting: " +
        // currentStudy.getStudyParameterConfig().getCollectDob());

        if (crfId == 0) {// no crf selected
            if (eventAttr == 0 && subAttr == 0 && CRFAttr == 0 && groupAttr == 0 && discAttr == 0) {

                forwardPage(Page.CREATE_DATASET_2);
            } else if (eventAttr > 0) {
                request.setAttribute("subjectAgeAtEvent", "1");
                if (currentStudy.getStudyParameterConfig().getCollectDob().equals("3")) {
                    request.setAttribute("subjectAgeAtEvent", "0");
                    logger.info("dob not collected, setting age at event to 0");
                }
                forwardPage(Page.CREATE_DATASET_EVENT_ATTR);
            } else if (subAttr > 0) {
                if (currentStudy.getStudyParameterConfig().getCollectDob().equals("3")) {
                    logger.info("dob not collected, setting age at event to 0");
                }
                forwardPage(Page.CREATE_DATASET_SUB_ATTR);
            } else if (CRFAttr > 0) {
                forwardPage(Page.CREATE_DATASET_CRF_ATTR);
            } else if (groupAttr > 0) {
                // TODO set up subject group classes here?
                setUpStudyGroupPage();
                forwardPage(Page.CREATE_DATASET_GROUP_ATTR);
            } // else if (discAttr > 0) {
            // forwardPage(Page.CREATE_DATASET_DISC_ATTR);
            // }
            else {
                forwardPage(Page.CREATE_DATASET_2);
            }
            return;
        }

        CRFBean crf = (CRFBean) crfdao.findByPK(crfId);
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(defId);

        session.setAttribute("crf", crf);
        session.setAttribute("definition", sed);

        DatasetBean db = (DatasetBean) session.getAttribute("newDataset");
        if (db == null) {
            db = new DatasetBean();
        }

        session.setAttribute("newDataset", db);
        // save current def id in the seesion to avoid duplicated def id in
        // dataset
        // bean
        // session.setAttribute(CURRENT_DEF_ID, new Integer(defId));

        ArrayList items = idao.findAllActiveByCRF(crf);
        for (int i = 0; i < items.size(); i++) {
            ItemBean item = (ItemBean) items.get(i);
            /*
             * logger.info("testing on item id "+ item.getId()+ " crf version id "+
             * item.getItemMeta().getCrfVersionId());
             */
            ItemFormMetadataBean meta = imfdao.findByItemIdAndCRFVersionId(item.getId(), item.getItemMeta().getCrfVersionId());
            // TODO change the above data access function, tbh
            // ArrayList metas = imfdao.findAllByItemId(item.getId());
            meta.setCrfVersionName(item.getItemMeta().getCrfVersionName());
            // logger.info("crf versionname" + meta.getCrfVersionName());
            item.getItemMetas().add(meta);
            // item.setItemMetas(metas);
        }
        HashMap itemMap = new HashMap();
        for (int i = 0; i < items.size(); i++) {
            ItemBean item = (ItemBean) items.get(i);

            if (!itemMap.containsKey(defId + "_" + item.getId())) {
                if (db.getItemMap().containsKey(defId + "_" + item.getId())) {
                    item.setSelected(true);
                    item.setDatasetItemMapKey(defId + "_" + item.getId());
                    // logger.info("Item got selected already11");
                }
                // itemMap.put(new Integer(item.getId()), item);
                itemMap.put(defId + "_" + item.getId(), item);
            } else {
                // same item,combine the metadata
                ItemBean uniqueItem = (ItemBean) itemMap.get(defId + "_" + item.getId());
                uniqueItem.getItemMetas().add(item.getItemMetas().get(0));
                if (db.getItemMap().containsKey(defId + "_" + uniqueItem.getId())) {
                    uniqueItem.setSelected(true);
                    // logger.info("Item got selected already22");
                }
            }

        }
        ArrayList itemArray = new ArrayList(itemMap.values());
        // now sort them by ordinal/name
        Collections.sort(itemArray);
        session.setAttribute("allItems", itemArray);

        forwardPage(Page.CREATE_DATASET_2);
    }

}
