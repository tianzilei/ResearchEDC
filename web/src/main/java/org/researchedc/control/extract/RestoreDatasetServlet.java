/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.extract;

import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.extract.DatasetBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.spi.DatasetDao;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.researchedc.web.bean.DatasetRow;
import org.researchedc.web.bean.EntityBeanTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author thickerson
 *
 */
public class RestoreDatasetServlet extends SecureController {

    @Autowired
    protected DatasetDao datasetDao;

    Locale locale;

    // < ResourceBundlerespage,resexception,resword;

    public static String getLink(int dsId) {
        return "RestoreDataset?dsId=" + dsId;
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int dsId = fp.getInt("dsId");
        DatasetDao dsDAO = this.datasetDao;
        DatasetBean dataset = (DatasetBean) dsDAO.findByPK(dsId);

        String action = request.getParameter("action");
        if (resword.getString("restore_this_dataset").equalsIgnoreCase(action)) {
            dataset.setStatus(Status.AVAILABLE);
            dsDAO.update(dataset);
            addPageMessage(respage.getString("dataset_has_been_succesfully_reinstated"));
            request.setAttribute("table", getDatasetTable());
            forwardPage(Page.VIEW_DATASETS);
        } else if (resword.getString("cancel").equalsIgnoreCase(action)) {

            request.setAttribute("table", getDatasetTable());
            forwardPage(Page.VIEW_DATASETS);
        } else {
            request.setAttribute("dataset", dataset);
            forwardPage(Page.RESTORE_DATASET);
        }
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.researchedc.i18n.exceptions",locale);
        // < resword =
        // ResourceBundle.getBundle("org.researchedc.i18n.words",locale);

        if (ub.isSysAdmin()) {
            return;// TODO limit to owner only?
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_restore_dataset"), "1");

    }

    private EntityBeanTable getDatasetTable() {
        FormProcessor fp = new FormProcessor(request);

        EntityBeanTable table = fp.getEntityBeanTable();
        DatasetDao dsdao = this.datasetDao;
        ArrayList datasets = new ArrayList();
        // if (ub.isSysAdmin()) {
        // datasets =
        // (ArrayList)dsdao.findAllByStudyIdAdmin(currentStudy.getId());
        // } else {
        datasets = dsdao.findAllByStudyId(currentStudy.getId());
        // }

        ArrayList datasetRows = DatasetRow.generateRowsFromBeans(datasets);

        String[] columns =
            { resword.getString("dataset_name"), resword.getString("description"), resword.getString("created_by"), resword.getString("created_date"),
                resword.getString("status"), resword.getString("actions") };
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(5);
        table.addLink(resword.getString("show_only_my_datasets"), "ViewDatasets?action=owner&ownerId=" + ub.getId());
        table.addLink(resword.getString("create_dataset"), "CreateDataset");
        table.setQuery("ViewDatasets", new HashMap());
        table.setRows(datasetRows);
        table.computeDisplay();
        return table;
    }

}
