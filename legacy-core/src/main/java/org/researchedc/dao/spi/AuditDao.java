package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.admin.AuditBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public interface AuditDao {
    EntityBean findByPK(int id);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    Collection findStudySubjectAuditEvents(int studySubjectId);
    Collection findSubjectAuditEvents(int subjectId);
    Collection findEventCRFAuditEvents(int eventCRFId);
    Collection findEventCRFAuditEventsWithItemDataType(int eventCRFId);
    Collection findAllEventCRFAuditEvents(int studyEventId);
    Collection findAllEventCRFAuditEventsWithItemDataType(int studyEventId);
    Collection findEventCRFAudit(int eventCRFId);
    Collection findStudyEventAuditEvents(int studyEventId);
    Collection findStudySubjectGroupAssignmentAuditEvents(int studySubjectId);
    List findDeletedEventCRFsFromAuditEvent(int studyEventId);
    List findDeletedEventCRFsFromAuditEventByEventCRFStatus(int studyEventId);
    ArrayList findItemAuditEvents(int entityId, String auditTable);
    ArrayList checkItemAuditEventsExist(int itemId, String auditTable, int ecbId);
    String findLastStatus(String audit_table, int entity_id, String new_value);
    Object getEntityFromHashMap(HashMap hm);
}
