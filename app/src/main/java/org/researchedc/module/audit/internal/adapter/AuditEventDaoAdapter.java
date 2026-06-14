package org.researchedc.module.audit.internal.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.researchedc.bean.admin.AuditEventBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.dao.spi.IAuditEventDAO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("auditEventDAO")
@Primary
@Transactional(readOnly = true)
public class AuditEventDaoAdapter implements IAuditEventDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public EntityBean findByPK(int id) {
        return new AuditEventBean();
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
    public ArrayList findAllByUserId(int userId) {
        List<?> rows = em.createNativeQuery(
                "SELECT ae.audit_id, ae.audit_date, ae.audit_table, ae.user_id, ae.entity_id, " +
                "ae.reason_for_change, ae.action_message, " +
                "aev.old_value, aev.new_value, aev.column_name, " +
                "aec.study_id, aec.subject_id " +
                "FROM audit_event ae " +
                "JOIN audit_event_values aev ON ae.audit_id=aev.audit_id " +
                "JOIN audit_event_context aec ON ae.audit_id=aec.audit_id " +
                "WHERE ae.user_id=? ORDER BY ae.audit_date DESC")
                .setParameter(1, userId)
                .getResultList();
        ArrayList beans = new ArrayList();
        for (Object row : rows) {
            beans.add(toAuditEventBean((Object[]) row));
        }
        return beans;
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        return new AuditEventBean();
    }

    private AuditEventBean toAuditEventBean(Object[] row) {
        AuditEventBean bean = new AuditEventBean();
        bean.setId(((Number) row[0]).intValue());
        if (row[1] instanceof java.sql.Timestamp ts) {
            bean.setAuditDate(new java.util.Date(ts.getTime()));
        }
        bean.setAuditTable(row[2] != null ? row[2].toString() : "");
        bean.setUserId(row[3] != null ? ((Number) row[3]).intValue() : 0);
        bean.setEntityId(row[4] != null ? ((Number) row[4]).intValue() : 0);
        bean.setReasonForChange(row[5] != null ? row[5].toString() : "");
        bean.setActionMessage(row[6] != null ? row[6].toString() : "");
        bean.setOldValue(row[7] != null ? row[7].toString() : "");
        bean.setNewValue(row[8] != null ? row[8].toString() : "");
        bean.setColumnName(row[9] != null ? row[9].toString() : "");
        bean.setStudyId(row[10] != null ? ((Number) row[10]).intValue() : 0);
        bean.setSubjectId(row[11] != null ? ((Number) row[11]).intValue() : 0);
        return bean;
    }
}
