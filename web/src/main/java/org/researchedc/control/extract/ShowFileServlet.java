/*
 * Created on Jun 9, 2005
 *
 *
 */
package org.researchedc.control.extract;

import org.researchedc.dao.spi.DatasetDao;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.extract.ArchivedDatasetFileBean;
import org.researchedc.bean.extract.DatasetBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.extract.ArchivedDatasetFileDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.researchedc.web.bean.ArchivedDatasetFileRow;
import org.researchedc.web.bean.EntityBeanTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <P>
 * purpose of this servlet is to respond with a file listing after we've
 * outlasted the 'please wait' message.
 *
 * @author thickerson
 *
 */
public class ShowFileServlet extends SecureController {

    @Autowired
    protected DatasetDao datasetDao;

    Locale locale;

    // < ResourceBundlerestext,resword,respage,resexception;

    public static String getLink(int fId, int dId) {
        return "ShowFile?fileId=" + fId + "&datasetId=" + dId;
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int fileId = fp.getInt("fileId");
        int dsId = fp.getInt("datasetId");
        DatasetDao dsdao = this.datasetDao;
        DatasetBean db = (DatasetBean) dsdao.findByPK(dsId);

        ArchivedDatasetFileDAO asdfdao = this.archivedDatasetFileDao;
        ArchivedDatasetFileBean asdfBean = (ArchivedDatasetFileBean) asdfdao.findByPK(fileId);

        ArrayList newFileList = new ArrayList();
        newFileList.add(asdfBean);
        // request.setAttribute("filelist",newFileList);

        ArrayList filterRows = ArchivedDatasetFileRow.generateRowsFromBeans(newFileList);
        EntityBeanTable table = fp.getEntityBeanTable();
        String[] columns =
            { resword.getString("file_name"), resword.getString("run_time"), resword.getString("file_size"), resword.getString("created_date"),
                resword.getString("created_by") };

        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(0);
        table.hideColumnLink(1);
        table.hideColumnLink(2);
        table.hideColumnLink(3);
        table.hideColumnLink(4);

        // table.setQuery("ExportDataset?datasetId=" +db.getId(), new
        // HashMap());
        // trying to continue...
        // session.setAttribute("newDataset",db);
        request.setAttribute("dataset", db);
        request.setAttribute("file", asdfBean);
        table.setRows(filterRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        Page finalTarget = Page.EXPORT_DATA_CUSTOM;

        finalTarget.setFileName("/WEB-INF/jsp/extract/generateMetadataFile.jsp");

        forwardPage(finalTarget);
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("org.researchedc.i18n.notes",locale);
        // < resword =
        // ResourceBundle.getBundle("org.researchedc.i18n.words",locale);
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

}
