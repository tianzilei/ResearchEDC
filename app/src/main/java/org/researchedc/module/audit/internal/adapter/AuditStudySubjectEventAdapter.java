package org.researchedc.module.audit.internal.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.researchedc.bean.admin.AuditBean;
import org.researchedc.bean.core.DataEntryStage;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.dao.spi.AuditDao;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;
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

    private final IStudyDAO studyDao;
    private final IStudySubjectDAO studySubjectDao;
    private final ISubjectDAO subjectDao;
    private final AuditDao auditDao;
    private final IStudyEventDAO studyEventDao;
    private final IStudyEventDefinitionDAO studyEventDefinitionDao;
    private final EventCRFDao eventCrfDao;

    AuditStudySubjectEventAdapter(IStudyDAO studyDao,
                                  IStudySubjectDAO studySubjectDao,
                                  ISubjectDAO subjectDao,
                                  AuditDao auditDao,
                                  IStudyEventDAO studyEventDao,
                                  IStudyEventDefinitionDAO studyEventDefinitionDao,
                                  EventCRFDao eventCrfDao) {
        this.studyDao = studyDao;
        this.studySubjectDao = studySubjectDao;
        this.subjectDao = subjectDao;
        this.auditDao = auditDao;
        this.studyEventDao = studyEventDao;
        this.studyEventDefinitionDao = studyEventDefinitionDao;
        this.eventCrfDao = eventCrfDao;
    }

    @Override
    public AuditStudySubjectEventsDTO findStudySubjectEvents(int studyId) {
        StudyBean study = (StudyBean) studyDao.findByPK(studyId);
        List<AuditStudySubjectLogDTO> subjects = studySubjectDao.findAllByStudyOrderByLabel(study)
                .stream()
                .map(bean -> toSubjectLog((StudySubjectBean) bean))
                .toList();
        return new AuditStudySubjectEventsDTO(toStudy(study), subjects);
    }

    private AuditStudySubjectLogDTO toSubjectLog(StudySubjectBean studySubject) {
        SubjectBean subject = (SubjectBean) subjectDao.findByPK(studySubject.getSubjectId());
        List<AuditStudySubjectAuditDTO> audits = new ArrayList<>();
        audits.addAll(auditDao.findStudySubjectAuditEvents(studySubject.getId())
                .stream()
                .map(bean -> toAudit((AuditBean) bean))
                .toList());
        audits.addAll(auditDao.findSubjectAuditEvents(subject.getId())
                .stream()
                .map(bean -> toAudit((AuditBean) bean))
                .toList());
        List<AuditStudyEventDTO> events = studyEventDao.findAllByStudySubject(studySubject)
                .stream()
                .map(bean -> toStudyEvent((StudyEventBean) bean))
                .toList();
        return new AuditStudySubjectLogDTO(
                toStudySubject(studySubject),
                toSubject(subject),
                audits,
                events);
    }

    private AuditStudyDTO toStudy(StudyBean bean) {
        return new AuditStudyDTO(
                bean.getId(),
                bean.getName(),
                bean.getIdentifier(),
                bean.getSecondaryIdentifier(),
                bean.getOid());
    }

    private AuditStudySubjectDTO toStudySubject(StudySubjectBean bean) {
        UserAccountBean owner = bean.getOwner();
        return new AuditStudySubjectDTO(
                bean.getId(),
                bean.getLabel(),
                bean.getSecondaryLabel(),
                bean.getOid(),
                bean.getSubjectId(),
                bean.getStudyId(),
                toInstant(bean.getCreatedDate()),
                owner != null ? owner.getId() : null,
                owner != null ? owner.getName() : null,
                statusName(bean.getStatus()));
    }

    private AuditSubjectDTO toSubject(SubjectBean bean) {
        return new AuditSubjectDTO(
                bean.getId(),
                bean.getUniqueIdentifier(),
                bean.getLabel(),
                toInstant(bean.getDateOfBirth()),
                String.valueOf(bean.getGender()),
                bean.isDobCollected(),
                statusName(bean.getStatus()));
    }

    private AuditStudySubjectAuditDTO toAudit(AuditBean bean) {
        return new AuditStudySubjectAuditDTO(
                bean.getId(),
                toInstant(bean.getAuditDate()),
                bean.getAuditTable(),
                bean.getUserId(),
                bean.getUserName(),
                bean.getEntityId(),
                bean.getEntityName(),
                bean.getAuditEventTypeName(),
                bean.getAuditEventTypeId(),
                bean.getOldValue(),
                bean.getNewValue(),
                bean.getReasonForChange());
    }

    private AuditStudyEventDTO toStudyEvent(StudyEventBean bean) {
        StudyEventDefinitionBean definition =
                (StudyEventDefinitionBean) studyEventDefinitionDao.findByPK(bean.getStudyEventDefinitionId());
        List<AuditEventCrfDTO> eventCrfs = eventCrfDao.findAllByStudyEvent(bean)
                .stream()
                .map(eventCrf -> toEventCrf((EventCRFBean) eventCrf))
                .toList();
        return new AuditStudyEventDTO(
                bean.getId(),
                bean.getStudyEventDefinitionId(),
                bean.getStudySubjectId(),
                bean.getLocation(),
                bean.getSampleOrdinal(),
                toInstant(bean.getDateStarted()),
                toInstant(bean.getDateEnded()),
                statusName(bean.getStatus()),
                stageName(bean.getStage()),
                subjectEventStatusName(bean.getSubjectEventStatus()),
                toDefinition(definition),
                eventCrfs);
    }

    private AuditStudyEventDefinitionDTO toDefinition(StudyEventDefinitionBean bean) {
        return new AuditStudyEventDefinitionDTO(
                bean.getId(),
                bean.getName(),
                bean.getOid(),
                bean.getDescription(),
                bean.getCategory(),
                bean.getType(),
                bean.isRepeating());
    }

    private AuditEventCrfDTO toEventCrf(EventCRFBean bean) {
        return new AuditEventCrfDTO(
                bean.getId(),
                bean.getStudyEventId(),
                bean.getStudySubjectId(),
                bean.getCRFVersionId(),
                toInstant(bean.getDateInterviewed()),
                bean.getInterviewerName(),
                toInstant(bean.getDateCompleted()),
                statusName(bean.getStatus()),
                stageName(bean.getStage()),
                bean.isElectronicSignatureStatus(),
                bean.isSdvStatus());
    }

    private String toInstant(Date value) {
        return value != null ? value.toInstant().toString() : null;
    }

    private String statusName(Status status) {
        return status != null ? status.getName() : null;
    }

    private String stageName(DataEntryStage stage) {
        return stage != null ? stage.getName() : null;
    }

    private String subjectEventStatusName(SubjectEventStatus status) {
        return status != null ? status.getName() : null;
    }
}
