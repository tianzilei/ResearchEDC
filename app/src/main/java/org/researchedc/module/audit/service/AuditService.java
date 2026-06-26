package org.researchedc.module.audit.service;

import org.researchedc.module.audit.dto.AuditLogDTO;
import org.researchedc.module.audit.entity.AuditLog;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.event.AuditRecordedEvent;
import org.researchedc.module.audit.repository.AuditLogRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AuditService(AuditLogRepository auditLogRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.auditLogRepository = auditLogRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuditLogDTO recordAudit(Integer studyId, AuditEventType eventType,
                                    String entityType, Long entityId,
                                    String entityLabel, String oldValue,
                                    String newValue, Integer performedBy,
                                    String details, String sourceModule) {
        AuditLog log = new AuditLog();
        log.setStudyId(studyId);
        log.setEventType(eventType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setEntityLabel(entityLabel);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedBy(performedBy);
        log.setDetails(details);
        log.setSourceModule(sourceModule);
        AuditLog saved = auditLogRepository.save(log);

        eventPublisher.publishEvent(new AuditRecordedEvent(
            this, saved.getId(), saved.getStudyId(), saved.getEventType(),
            saved.getEntityType(), saved.getEntityId(), saved.getPerformedBy()));

        return toDto(saved);
    }

    public Page<AuditLogDTO> listAuditLogs(Integer studyId, Pageable pageable) {
        Page<AuditLog> page;
        if (studyId != null) {
            page = auditLogRepository.findByStudyId(studyId, pageable);
        } else {
            page = auditLogRepository.findAllByOrderByPerformedDateDesc(pageable);
        }
        return page.map(this::toDto);
    }

    public AuditLogDTO getAuditLog(Long id) {
        AuditLog log = auditLogRepository.findById(id)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "AuditLog not found: " + id));
        return toDto(log);
    }

    public void onAuditRecorded(AuditRecordedEvent event) {
    }

    private AuditLogDTO toDto(AuditLog log) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(log.getId());
        dto.setStudyId(log.getStudyId());
        dto.setEventType(log.getEventType());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setEntityLabel(log.getEntityLabel());
        dto.setOldValue(log.getOldValue());
        dto.setNewValue(log.getNewValue());
        dto.setPerformedBy(log.getPerformedBy());
        dto.setPerformedDate(log.getPerformedDate());
        dto.setDetails(log.getDetails());
        dto.setSourceModule(log.getSourceModule());
        return dto;
    }
}
