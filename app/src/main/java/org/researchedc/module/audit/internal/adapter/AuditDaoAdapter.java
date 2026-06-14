package org.researchedc.module.audit.internal.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.researchedc.bean.admin.AuditBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.dao.spi.AuditDao;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("auditDao")
@Primary
@Transactional(readOnly = true)
public class AuditDaoAdapter implements AuditDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public EntityBean findByPK(int id) {
        List<?> rows = em.createNativeQuery(
                "SELECT ale.audit_id, ale.audit_date, ale.audit_table, ale.user_id, ale.entity_id, " +
                "ale.entity_name, ale.reason_for_change, ale.audit_log_event_type_id, ale.old_value, " +
                "ale.new_value, ale.event_crf_id, ua.user_name, alet.name " +
                "FROM audit_log_event ale JOIN user_account ua ON ale.user_id=ua.user_id " +
                "JOIN audit_log_event_type alet ON ale.audit_log_event_type_id=alet.audit_log_event_type_id " +
                "WHERE ale.audit_id = ?")
                .setParameter(1, id)
                .getResultList();
        if (rows.isEmpty()) return new AuditBean();
        return toBean((Object[]) rows.get(0));
    }

    @Override
    public EntityBean create(EntityBean eb) {
        return eb;
    }

    @Override
    public EntityBean update(EntityBean eb) {
        return eb;
    }

    @Override
    public Collection findStudySubjectAuditEvents(int studySubjectId) {
        List<?> rows = em.createNativeQuery(
                "SELECT ale.audit_id, ale.audit_date, ale.audit_table, ale.user_id, ale.entity_id, " +
                "ale.entity_name, ale.reason_for_change, ale.audit_log_event_type_id, ale.old_value, " +
                "ale.new_value, ale.event_crf_id, ua.user_name, alet.name " +
                "FROM audit_log_event ale JOIN user_account ua ON ale.user_id=ua.user_id " +
                "JOIN audit_log_event_type alet ON ale.audit_log_event_type_id=alet.audit_log_event_type_id " +
                "WHERE ale.audit_table='study_subject' AND ale.audit_log_event_type_id IN (2,3,4,27) " +
                "AND ale.entity_id=? ORDER BY ale.audit_date DESC")
                .setParameter(1, studySubjectId)
                .getResultList();
        return toBeans(rows);
    }

    @Override
    public Collection findSubjectAuditEvents(int subjectId) {
        List<?> rows = em.createNativeQuery(
                "SELECT ale.audit_id, ale.audit_date, ale.audit_table, ale.user_id, ale.entity_id, " +
                "ale.entity_name, ale.reason_for_change, ale.audit_log_event_type_id, ale.old_value, " +
                "ale.new_value, ale.event_crf_id, ua.user_name, alet.name " +
                "FROM audit_log_event ale JOIN user_account ua ON ale.user_id=ua.user_id " +
                "JOIN audit_log_event_type alet ON ale.audit_log_event_type_id=alet.audit_log_event_type_id " +
                "WHERE ale.audit_table='subject' AND ale.audit_log_event_type_id IN (5,6,7) " +
                "AND ale.entity_id=? ORDER BY ale.audit_date DESC")
                .setParameter(1, subjectId)
                .getResultList();
        return toBeans(rows);
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        return new AuditBean();
    }

    private ArrayList toBeans(List<?> rows) {
        ArrayList beans = new ArrayList();
        for (Object row : rows) {
            beans.add(toBean((Object[]) row));
        }
        return beans;
    }

    private AuditBean toBean(Object[] row) {
        AuditBean bean = new AuditBean();
        bean.setId(((Number) row[0]).intValue());
        if (row[1] instanceof java.sql.Timestamp ts) {
            bean.setAuditDate(new java.util.Date(ts.getTime()));
        }
        bean.setAuditTable(row[2] != null ? row[2].toString() : "");
        bean.setUserId(row[3] != null ? ((Number) row[3]).intValue() : 0);
        bean.setEntityId(row[4] != null ? ((Number) row[4]).intValue() : 0);
        bean.setEntityName(row[5] != null ? row[5].toString() : "");
        bean.setReasonForChange(row[6] != null ? row[6].toString() : "");
        bean.setAuditEventTypeId(row[7] != null ? ((Number) row[7]).intValue() : 0);
        bean.setOldValue(row[8] != null ? row[8].toString() : "");
        bean.setNewValue(row[9] != null ? row[9].toString() : "");
        bean.setUserName(row[11] != null ? row[11].toString() : "");
        bean.setAuditEventTypeName(row[12] != null ? row[12].toString() : "");
        return bean;
    }
}
