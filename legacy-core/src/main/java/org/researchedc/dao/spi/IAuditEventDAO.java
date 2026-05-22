package org.researchedc.dao.spi;

import org.researchedc.bean.admin.AuditEventBean;
import org.researchedc.bean.admin.TriggerBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.login.UserAccountBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface IAuditEventDAO {
    EntityBean findByPK(int id);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    ArrayList findAllByAuditTable(String tableName);
    ArrayList findAllByStudyId(int studyId);
    ArrayList findAllByStudyIdAndLimit(int studyId);
    ArrayList findAllByUserId(int userId);
    ArrayList findAllByEntityName(int entityId, String digesterName);
    ArrayList findEventStatusLogByStudySubject(int studySubjectId);
    Collection findAggregatesByTableName(String tableName);
    AuditEventBean setStudyAndSubjectInfo(AuditEventBean aeb);
    void createRowForJobConclusion(TriggerBean trigger, int eventTypeId);
    void createRowForUserAccount(UserAccountBean uab, String reasonForChange, String actionMessage);
    void createRowForFailedLogin(UserAccountBean uab);
    void createRowForLogin(UserAccountBean uab);
    void createRowForPasswordRequest(UserAccountBean uab);
    void createRowForJobExecution(TriggerBean triggerBean, String reasonForChange, String actionMessage);
    void createRowForExtractDataJobSuccess(TriggerBean triggerBean);
    void createRowForExtractDataJobSuccess(TriggerBean triggerBean, String message);
    void createRowForExtractDataJobFailure(TriggerBean triggerBean);
    void createRowForExtractDataJobFailure(TriggerBean triggerBean, String message);
    Object getEntityFromHashMap(HashMap hm);
}
