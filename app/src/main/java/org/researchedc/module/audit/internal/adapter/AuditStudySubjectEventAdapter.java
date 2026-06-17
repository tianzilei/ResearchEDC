package org.researchedc.module.audit.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.module.audit.dto.AuditEventCrfDTO;
import org.researchedc.module.audit.dto.AuditStudyDTO;
import org.researchedc.module.audit.dto.AuditStudyEventDTO;
import org.researchedc.module.audit.dto.AuditStudyEventDefinitionDTO;
import org.researchedc.module.audit.dto.AuditStudySubjectAuditDTO;
import org.researchedc.module.audit.dto.AuditStudySubjectDTO;
import org.researchedc.module.audit.dto.AuditStudySubjectEventsDTO;
import org.researchedc.module.audit.dto.AuditStudySubjectLogDTO;
import org.researchedc.module.audit.dto.AuditSubjectDTO;
import org.researchedc.module.audit.service.AuditStudySubjectEventPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
class AuditStudySubjectEventAdapter implements AuditStudySubjectEventPort {

    @PersistenceContext
    private EntityManager entityManager;

    AuditStudySubjectEventAdapter() {
    }

    @Override
    public AuditStudySubjectEventsDTO findStudySubjectEvents(int studyId) {
        Object[] study = findStudy(studyId);
        List<AuditStudySubjectLogDTO> subjects = findStudySubjects(studyId).stream()
                .map(this::toSubjectLog)
                .toList();
        return new AuditStudySubjectEventsDTO(toStudy(study), subjects);
    }

    private AuditStudySubjectLogDTO toSubjectLog(Object[] studySubject) {
        int studySubjectId = integer(studySubject[0]);
        int subjectId = integer(studySubject[4]);
        Object[] subject = findSubject(subjectId);
        List<AuditStudySubjectAuditDTO> audits = new ArrayList<>();
        audits.addAll(findStudySubjectAudits(studySubjectId).stream()
                .map(this::toAudit)
                .toList());
        audits.addAll(findSubjectAudits(subjectId).stream()
                .map(this::toAudit)
                .toList());
        List<AuditStudyEventDTO> events = findStudyEvents(studySubjectId).stream()
                .map(this::toStudyEvent)
                .toList();
        return new AuditStudySubjectLogDTO(
                toStudySubject(studySubject),
                toSubject(subject),
                audits,
                events);
    }

    private Object[] findStudy(int studyId) {
        List<?> rows = entityManager.createNativeQuery(
                "SELECT study_id, name, unique_identifier, secondary_identifier, oc_oid " +
                "FROM module_study WHERE study_id = ?")
                .setParameter(1, studyId)
                .getResultList();
        return rows.isEmpty() ? new Object[] {studyId, "", "", "", ""} : (Object[]) rows.getFirst();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> findStudySubjects(int studyId) {
        return entityManager.createNativeQuery(
                "SELECT ss.study_subject_id, ss.label, ss.secondary_label, ss.oc_oid, " +
                "ss.subject_id, ss.study_id, ss.date_created, ss.owner_id, ua.user_name, ss.status_id " +
                "FROM module_study_subject ss " +
                "LEFT JOIN user_account ua ON ua.user_id = ss.owner_id " +
                "WHERE ss.study_id = ? ORDER BY ss.label")
                .setParameter(1, studyId)
                .getResultList();
    }

    private Object[] findSubject(int subjectId) {
        List<?> rows = entityManager.createNativeQuery(
                "SELECT subject_id, unique_identifier, date_of_birth, gender, dob_collected, status_id " +
                "FROM module_subject WHERE subject_id = ?")
                .setParameter(1, subjectId)
                .getResultList();
        return rows.isEmpty() ? new Object[] {subjectId, "", null, "m", false, Status.INVALID.getId()}
                : (Object[]) rows.getFirst();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> findStudySubjectAudits(int studySubjectId) {
        return entityManager.createNativeQuery(
                auditSql("study_subject", "2,3,4,27"))
                .setParameter(1, studySubjectId)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> findSubjectAudits(int subjectId) {
        return entityManager.createNativeQuery(auditSql("subject", "5,6,7"))
                .setParameter(1, subjectId)
                .getResultList();
    }

    private String auditSql(String table, String typeIds) {
        return "SELECT ale.audit_id, ale.audit_date, ale.audit_table, ale.user_id, ale.entity_id, " +
                "ale.entity_name, alet.name, ale.audit_log_event_type_id, ale.old_value, " +
                "ale.new_value, ale.reason_for_change, ua.user_name " +
                "FROM audit_log_event ale JOIN user_account ua ON ale.user_id=ua.user_id " +
                "JOIN audit_log_event_type alet ON ale.audit_log_event_type_id=alet.audit_log_event_type_id " +
                "WHERE ale.audit_table='" + table + "' AND ale.audit_log_event_type_id IN (" + typeIds + ") " +
                "AND ale.entity_id=? ORDER BY ale.audit_date DESC";
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> findStudyEvents(int studySubjectId) {
        return entityManager.createNativeQuery(
                "SELECT se.study_event_id, se.study_event_definition_id, se.study_subject_id, " +
                "se.location, se.sample_ordinal, se.date_start, se.date_end, se.status_id, " +
                "se.subject_event_status_id " +
                "FROM module_study_event se WHERE se.study_subject_id = ? ORDER BY se.study_event_id")
                .setParameter(1, studySubjectId)
                .getResultList();
    }

    private Object[] findDefinition(int definitionId) {
        List<?> rows = entityManager.createNativeQuery(
                "SELECT study_event_definition_id, name, oc_oid, description, category, type, repeating " +
                "FROM module_study_event_definition WHERE study_event_definition_id = ?")
                .setParameter(1, definitionId)
                .getResultList();
        return rows.isEmpty() ? new Object[] {definitionId, "", "", "", "", "", false}
                : (Object[]) rows.getFirst();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> findEventCrfs(int studyEventId) {
        return entityManager.createNativeQuery(
                "SELECT event_crf_id, study_event_id, study_subject_id, crf_version_id, " +
                "date_interviewed, interviewer_name, date_completed, status_id, " +
                "electronic_signature_status, sdv_status " +
                "FROM module_event_crf WHERE study_event_id = ? ORDER BY event_crf_id")
                .setParameter(1, studyEventId)
                .getResultList();
    }

    private AuditStudyDTO toStudy(Object[] row) {
        return new AuditStudyDTO(
                integer(row[0]),
                string(row[1]),
                string(row[2]),
                string(row[3]),
                string(row[4]));
    }

    private AuditStudySubjectDTO toStudySubject(Object[] row) {
        return new AuditStudySubjectDTO(
                integer(row[0]),
                string(row[1]),
                string(row[2]),
                string(row[3]),
                integer(row[4]),
                integer(row[5]),
                toInstant(row[6]),
                nullableInteger(row[7]),
                string(row[8]),
                statusName(row[9]));
    }

    private AuditSubjectDTO toSubject(Object[] row) {
        String gender = string(row[3]);
        return new AuditSubjectDTO(
                integer(row[0]),
                string(row[1]),
                "",
                toInstant(row[2]),
                !gender.isEmpty() ? gender.substring(0, 1) : "m",
                bool(row[4]),
                statusName(row[5]));
    }

    private AuditStudySubjectAuditDTO toAudit(Object[] row) {
        return new AuditStudySubjectAuditDTO(
                integer(row[0]),
                toInstant(row[1]),
                string(row[2]),
                integer(row[3]),
                string(row[11]),
                integer(row[4]),
                string(row[5]),
                string(row[6]),
                integer(row[7]),
                string(row[8]),
                string(row[9]),
                string(row[10]));
    }

    private AuditStudyEventDTO toStudyEvent(Object[] row) {
        int eventId = integer(row[0]);
        int definitionId = integer(row[1]);
        List<AuditEventCrfDTO> eventCrfs = findEventCrfs(eventId).stream()
                .map(this::toEventCrf)
                .toList();
        return new AuditStudyEventDTO(
                eventId,
                definitionId,
                integer(row[2]),
                string(row[3]),
                integer(row[4]),
                toInstant(row[5]),
                toInstant(row[6]),
                statusName(row[7]),
                null,
                subjectEventStatusName(row[8]),
                toDefinition(findDefinition(definitionId)),
                eventCrfs);
    }

    private AuditStudyEventDefinitionDTO toDefinition(Object[] row) {
        return new AuditStudyEventDefinitionDTO(
                integer(row[0]),
                string(row[1]),
                string(row[2]),
                string(row[3]),
                string(row[4]),
                string(row[5]),
                bool(row[6]));
    }

    private AuditEventCrfDTO toEventCrf(Object[] row) {
        return new AuditEventCrfDTO(
                integer(row[0]),
                integer(row[1]),
                integer(row[2]),
                integer(row[3]),
                toInstant(row[4]),
                string(row[5]),
                toInstant(row[6]),
                statusName(row[7]),
                null,
                bool(row[8]),
                bool(row[9]));
    }

    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private int integer(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private Integer nullableInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private boolean bool(Object value) {
        return value instanceof Boolean bool && bool;
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
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.atZone(ZoneId.systemDefault()).toInstant().toString();
        }
        return null;
    }

    private String statusName(Object value) {
        return value instanceof Number number ? Status.getFromMap(number.intValue()).getName() : null;
    }

    private String subjectEventStatusName(Object value) {
        return value instanceof Number number ? SubjectEventStatus.getFromMap(number.intValue()).getName() : null;
    }
}
