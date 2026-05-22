/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.extract;

import org.researchedc.dao.managestudy.StudyGroupClassDAO;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.extract.DatasetBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author jxu
 *
 * Views selected items for creating dataset, aslo allow user to de-select or
 * select all items in a study
 */
public class ViewSelectedServlet extends SecureController {

    Locale locale;

    // < ResourceBundlerestext,resexception,respage;

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("org.researchedc.i18n.notes",locale);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.researchedc.i18n.exceptions",locale);

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

    /*
     * setup study groups, tbh, added july 2007 FIXME in general a repeated set
     * of code -- need to create a superclass which will contain this class, tbh
     */
    public void setUpStudyGroups() {
        ArrayList sgclasses = (ArrayList) session.getAttribute("allSelectedGroups");
        if (sgclasses == null || sgclasses.size() == 0) {
            IStudyDAO studydao = new StudyDAO(sm.getDataSource());
            StudyGroupClassDAO sgclassdao = new StudyGroupClassDAO(sm.getDataSource());
            StudyBean theStudy = (StudyBean) studydao.findByPK(sm.getUserBean().getActiveStudyId());
            sgclasses = sgclassdao.findAllActiveByStudy(theStudy);
        }
        session.setAttribute("allSelectedGroups", sgclasses);
        session.setAttribute("numberOfStudyGroups", sgclasses.size());
        request.setAttribute("allSelectedGroups", sgclasses);
    }

    @Override
    public void processRequest() throws Exception {

        DatasetBean db = (DatasetBean) session.getAttribute("newDataset");
        HashMap events = (HashMap) session.getAttribute(CreateDatasetServlet.EVENTS_FOR_CREATE_DATASET);
        if (events == null) {
            events = new HashMap();
        }
        request.setAttribute("eventlist", events);

        ICrfDAO crfdao = new CRFDAO(sm.getDataSource());
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemFormMetadataDAO imfdao = new ItemFormMetadataDAO(sm.getDataSource());
        ArrayList ids = CreateDatasetServlet.allSedItemIdsInStudy(events, crfdao, idao);// new
                                                                                        // ArrayList();
        // ArrayList allItemsInStudy = EditSelectedServlet.selectAll(events,
        // crfdao, idao);
        // for (int j = 0; j < allItemsInStudy.size(); j++) {
        // ItemBean item = (ItemBean) allItemsInStudy.get(j);
        // Integer itemId = new Integer(item.getId());
        // if (!ids.contains(itemId)) {
        // ids.add(itemId);
        // }
        // }
        session.setAttribute("numberOfStudyItems", new Integer(ids.size()).toString());

        ArrayList items = new ArrayList();
        if (db == null || db.getItemIds().size() == 0) {
            session.setAttribute("allSelectedItems", items);
            setUpStudyGroups();// FIXME can it be that we have no selected
            // items and
            // some selected groups? tbh
            forwardPage(Page.CREATE_DATASET_VIEW_SELECTED);
            return;
        }

        items = getAllSelected(db, idao, imfdao);

        session.setAttribute("allSelectedItems", items);

        FormProcessor fp = new FormProcessor(request);
        String status = fp.getString("status");
        if (!StringUtil.isBlank(status) && "html".equalsIgnoreCase(status)) {
            forwardPage(Page.CREATE_DATASET_VIEW_SELECTED_HTML);
        } else {
            setUpStudyGroups();
            forwardPage(Page.CREATE_DATASET_VIEW_SELECTED);
        }

    }

    public static ArrayList getAllSelected(DatasetBean db, ItemDAO idao, ItemFormMetadataDAO imfdao) throws Exception {
        ArrayList items = new ArrayList();
        // ArrayList itemIds = db.getItemIds();
        ArrayList itemDefCrfs = db.getItemDefCrf();

        for (int i = 0; i < itemDefCrfs.size(); i++) {
            ItemBean item = (ItemBean) itemDefCrfs.get(i);
            item.setSelected(true);
            ArrayList metas = imfdao.findAllByItemId(item.getId());
            for (int h = 0; h < metas.size(); h++) {
                ItemFormMetadataBean ifmb = (ItemFormMetadataBean) metas.get(h);
                // logger.info("group name found:
                // "+ifmb.getGroupLabel());
            }
            item.setItemMetas(metas);
            items.add(item);
        }

        return items;

    }

}
