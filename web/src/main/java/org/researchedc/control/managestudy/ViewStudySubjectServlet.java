/*
 * Minimal stub for ViewStudySubjectServlet — retains only static methods still referenced by ListStudySubjectServlet.
 * Full implementation deleted in Phase 1 study/subject/event slice.
 */
package org.researchedc.control.managestudy;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyEventDAO;

public class ViewStudySubjectServlet {

    private ViewStudySubjectServlet() {
    }

    public static ArrayList getDisplayEventCRFs(DataSource ds, ArrayList eventCRFs, ArrayList eventDefinitionCRFs, UserAccountBean ub,
            StudyUserRoleBean currentRole, SubjectEventStatus status, StudyBean study, IStudyEventDAO sedao, ICrfDAO cdao, ICrfVersionDAO cvdao,
            IItemDataDAO iddao, EventDefinitionCRFDao edcdao) {
        ArrayList answer = new ArrayList();

        int i;
        for (i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecb = (EventCRFBean) eventCRFs.get(i);

            int crfVersionId = ecb.getCRFVersionId();
            CRFBean cb = cdao.findByVersionId(crfVersionId);
            ecb.setCrf(cb);

            boolean isCompleted = ecb.getStage().equals(SubjectEventStatus.COMPLETED)
                    || ecb.getStage().equals(SubjectEventStatus.SIGNED);

            if (!isCompleted) {
                answer.add(ecb);
            }
        }

        return answer;
    }

    public static ArrayList getUncompletedCRFs(DataSource ds, ArrayList eventDefinitionCRFs, ArrayList eventCRFs, SubjectEventStatus status,
            ICrfVersionDAO cvdao, IItemDataDAO iddao) {
        HashMap completed = new HashMap();
        ArrayList answer = new ArrayList();

        for (int i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edcrf = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
            completed.put(Integer.valueOf(edcrf.getCrfId()), Boolean.FALSE);
        }

        for (int i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecrf = (EventCRFBean) eventCRFs.get(i);
            if (ecrf.getStage().equals(SubjectEventStatus.COMPLETED)
                    || ecrf.getStage().equals(SubjectEventStatus.SIGNED)) {
                completed.put(Integer.valueOf(ecrf.getCRFVersionId()), Boolean.TRUE);
            }
        }

        for (int i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edcrf = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
            Boolean isCompleted = (Boolean) completed.get(Integer.valueOf(edcrf.getCrfId()));
            if (isCompleted != null && !isCompleted.booleanValue()) {
                answer.add(edcrf);
            }
        }

        return answer;
    }
}
