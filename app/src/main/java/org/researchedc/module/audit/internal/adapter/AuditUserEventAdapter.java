package org.researchedc.module.audit.internal.adapter;

import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.researchedc.module.audit.dto.AuditUserEventDTO;
import org.researchedc.module.audit.dto.AuditUserEventsDTO;
import org.researchedc.module.audit.dto.AuditUserSummaryDTO;
import org.researchedc.module.audit.service.AuditUserEventPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
class AuditUserEventAdapter implements AuditUserEventPort {

    @PersistenceContext
    private EntityManager entityManager;

    AuditUserEventAdapter() {
    }

    @Override
    public AuditUserEventsDTO findUserEvents(int userId) {
        Object[] user = findUserRow(userId);
        List<AuditUserEventDTO> events = findEventRows(userId).stream()
                .map(this::toDto)
                .toList();
        return new AuditUserEventsDTO(toUserSummary(user), events);
    }

    private Object[] findUserRow(int userId) {
        List<?> rows = entityManager.createNativeQuery(
                "SELECT user_id, user_name, first_name, last_name " +
                "FROM user_account WHERE user_id = ?")
                .setParameter(1, userId)
                .getResultList();
        return rows.isEmpty() ? new Object[] {userId, "", "", ""} : (Object[]) rows.getFirst();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> findEventRows(int userId) {
        return entityManager.createNativeQuery(
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
    }

    private AuditUserSummaryDTO toUserSummary(Object[] row) {
        String userName = string(row[1]);
        return new AuditUserSummaryDTO(
                integer(row[0]),
                userName,
                userName,
                string(row[2]),
                string(row[3]));
    }

    private AuditUserEventDTO toDto(Object[] row) {
        return new AuditUserEventDTO(
                integer(row[0]),
                toInstant(row[1]),
                string(row[2]),
                integer(row[3]),
                integer(row[4]),
                string(row[5]),
                string(row[5]),
                string(row[6]),
                string(row[6]),
                string(row[9]),
                string(row[7]),
                string(row[8]),
                integer(row[10]),
                "NULL",
                integer(row[11]),
                "NULL",
                Map.of(),
                Map.of());
    }

    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Integer integer(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private String string(Object value) {
        return value != null ? value.toString() : "";
    }

    private String toInstant(Object value) {
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant().toString();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant().toString();
        }
        return null;
    }
}
