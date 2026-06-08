/*
 * Minimal stub for ListStudySubjectServlet — retains constants and getDisplayStudyEventsForStudySubject() still referenced by 8+ active servlets.
 * Full implementation deleted in Phase 1 study/subject/event slice.
 * Extended by ListStudySubjectsManageServlet and ListStudySubjectsSubmitServlet.
 */
package org.researchedc.control.managestudy;

import java.util.ArrayList;

import javax.sql.DataSource;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.managestudy.DisplayStudyEventBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.view.Page;

public abstract class ListStudySubjectServlet extends SecureController {

    public static String SUBJECT_PAGE_NUMBER = "ebl_page";
    public static String PAGINATING_QUERY = "paginatingQuery";
    public static String FILTER_KEYWORD = "ebl_filterKeyword";
    public static String SEARCH_SUBMITTED = "submitted";

    protected abstract String getBaseURL();

    protected abstract Page getJSP();

    @Override
    public void processRequest() throws Exception {
    }

    public static DisplayStudyEventBean getDisplayStudyEventsForStudySubject(StudySubjectBean studySub, StudyEventBean event, DataSource ds,
            UserAccountBean ub, StudyUserRoleBean currentRole, StudyBean study, IStudyEventDefinitionDAO seddao, IStudyEventDAO sedao, EventCRFDao ecdao,
            EventDefinitionCRFDao edcdao, ICrfDAO cdao, ICrfVersionDAO cvdao, IItemDataDAO iddao) {

        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
        event.setStudyEventDefinition(sed);

        ArrayList eventDefinitionCRFs = edcdao.findAllActiveByEventDefinitionId(sed.getId());
        ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

        DisplayStudyEventBean de = new DisplayStudyEventBean();
        de.setStudyEvent(event);
        de.setDisplayEventCRFs(ViewStudySubjectServlet.getDisplayEventCRFs(ds, eventCRFs, eventDefinitionCRFs, ub, currentRole, event.getSubjectEventStatus(),
                study, sedao, cdao, cvdao, iddao, edcdao));
        ArrayList al = ViewStudySubjectServlet.getUncompletedCRFs(ds, eventDefinitionCRFs, eventCRFs, event.getSubjectEventStatus(), cvdao, iddao);
        de.setUncompletedCRFs(al);

        return de;
    }
}
