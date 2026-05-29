/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.service.StudyConfigService;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Prepares to process request of updating a study object
 *
 * @author jxu
 * @version CVS: $Id: InitUpdateStudyServlet.java 13689 2009-12-16 21:10:37Z kkrumlian $
 */
public class InitUpdateStudyServlet extends SecureController {

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

//        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
//        throw new InsufficientPermissionException(Page.STUDY_LIST_SERVLET, resexception.getString("not_admin"), "1");

    }

    /**
     * Processes the request
     */
    @Override
    public void processRequest() throws Exception {

        IStudyDAO sdao = this.studyDao;
        String idString = request.getParameter("id");
        logger.info("study id:" + idString);
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_study_to_edit"));
            forwardPage(Page.STUDY_LIST_SERVLET);
        } else {
            int studyId = Integer.valueOf(idString.trim()).intValue();
            StudyBean study = (StudyBean) sdao.findByPK(studyId);
            study = studyConfigService.setParametersForStudy(study);

            logger.info("date created:" + study.getCreatedDate());
            logger.info("protocol Type:" + study.getProtocolType());

            session.setAttribute("newStudy", study);
            request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
            request.setAttribute("statuses", Status.toActiveArrayList());

            resetPanel();
            panel.setStudyInfoShown(false);
            panel.setOrderedData(true);
            panel.setExtractData(false);
            panel.setSubmitDataModule(false);
            panel.setCreateDataset(false);
            panel.setIconInfoShown(true);
            panel.setManageSubject(false);

            forwardPage(Page.UPDATE_STUDY1);
        }

    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

}
