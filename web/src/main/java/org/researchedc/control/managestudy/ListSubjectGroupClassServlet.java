/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.dao.managestudy.StudyGroupClassDAO;
import org.researchedc.dao.managestudy.StudyGroupDAO;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.researchedc.web.bean.EntityBeanTable;
import org.researchedc.web.bean.StudyGroupClassRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Lists all the subject group classes in a study
 *
 * @author jxu
 *
 */
public class ListSubjectGroupClassServlet extends SecureController {

    Locale locale;

    // < ResourceBundleresexception,respage,resword;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // <
        // resexception=ResourceBundle.getBundle(
        // "org.researchedc.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",
        // locale);
        // < resword =
        // ResourceBundle.getBundle("org.researchedc.i18n.words",locale);

        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MANAGE_STUDY_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        StudyGroupClassDAO sgcdao = this.studyGroupClassDao;
        // YW <<
        IStudyDAO stdao = this.studyDao;
        int parentStudyId = currentStudy.getParentStudyId();
        ArrayList groups = new ArrayList();
        if (parentStudyId > 0) {
            StudyBean parentStudy = (StudyBean) stdao.findByPK(parentStudyId);
            groups = sgcdao.findAllByStudy(parentStudy);
        } else {
            groups = sgcdao.findAllByStudy(currentStudy);
        }
        // YW >>
        String isReadOnly = request.getParameter("read");

        StudyGroupDAO sgdao = this.studyGroupDao;
        for (int i = 0; i < groups.size(); i++) {
            StudyGroupClassBean group = (StudyGroupClassBean) groups.get(i);
            ArrayList studyGroups = sgdao.findAllByGroupClass(group);
            group.setStudyGroups(studyGroups);

        }
        EntityBeanTable table = fp.getEntityBeanTable();
        ArrayList allGroupRows = StudyGroupClassRow.generateRowsFromBeans(groups);
        boolean isParentStudy = currentStudy.getParentStudyId() > 0 ? false : true;
        request.setAttribute("isParentStudy", isParentStudy);

        String[] columns =
            { resword.getString("subject_group_class"), resword.getString("type"), resword.getString("subject_assignment"), resword.getString("study_name"),
                resword.getString("subject_groups"), resword.getString("status"), resword.getString("actions") };
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(4);
        table.hideColumnLink(6);
        table.setQuery("ListSubjectGroupClass", new HashMap());
        // if (isParentStudy && (!currentStudy.getStatus().isLocked())) {
        // table.addLink(resword.getString("create_a_subject_group_class"),
        // "CreateSubjectGroupClass");
        // }
        table.setRows(allGroupRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        if (request.getParameter("read") != null && request.getParameter("read").equals("true")) {
            request.setAttribute("readOnly", true);
        }
        forwardPage(Page.SUBJECT_GROUP_CLASS_LIST);

    }

}
