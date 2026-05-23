package org.researchedc.module.audit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.researchedc.module.audit.dto.AuditLogDTO;
import org.researchedc.module.audit.entity.AuditLog;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.event.AuditRecordedEvent;
import org.researchedc.module.audit.repository.AuditLogRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private AuditService service;

    @BeforeEach
    void setUp() {
        service = new AuditService(auditLogRepository, eventPublisher);
    }

    // --- recordAudit ---

    @Test
    void recordAudit_savesAndPublishesEvent() {
        AuditLog saved = createAuditLog(1L, 10, AuditEventType.CREATE, "Study", 100L, 42);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(saved);

        AuditLogDTO result = service.recordAudit(
                10, AuditEventType.CREATE, "Study", 100L,
                "Study #100", null, "New study", 42, "Created study", "study-module");

        assertEquals(1L, result.getId());
        assertEquals(10, result.getStudyId());
        assertEquals(AuditEventType.CREATE, result.getEventType());
        assertEquals("Study", result.getEntityType());
        assertEquals(100L, result.getEntityId());
        assertEquals("Study #100", result.getEntityLabel());
        assertEquals(42, result.getPerformedBy());
        assertNotNull(result.getPerformedDate());

        verify(auditLogRepository).save(any(AuditLog.class));

        ArgumentCaptor<AuditRecordedEvent> eventCaptor =
                ArgumentCaptor.forClass(AuditRecordedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        AuditRecordedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.getAuditLogId());
        assertEquals(10, event.getStudyId());
        assertEquals(AuditEventType.CREATE, event.getEventType());
        assertEquals("Study", event.getEntityType());
        assertEquals(100L, event.getEntityId());
        assertEquals(42, event.getPerformedBy());
    }

    @Test
    void recordAudit_withNullStudyId_savesAndPublishes() {
        AuditLog saved = createAuditLog(2L, null, AuditEventType.LOGIN, "User", 1L, 1);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(saved);

        AuditLogDTO result = service.recordAudit(
                null, AuditEventType.LOGIN, "User", 1L,
                "user@example.com", null, null, 1, "Login", "identity-module");

        assertNull(result.getStudyId());
        assertEquals(AuditEventType.LOGIN, result.getEventType());

        ArgumentCaptor<AuditRecordedEvent> captor =
                ArgumentCaptor.forClass(AuditRecordedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertNull(captor.getValue().getStudyId());
    }

    // --- listAuditLogs (with studyId) ---

    @Test
    void listAuditLogs_withStudyId_filtersByStudy() {
        AuditLog log = createAuditLog(1L, 10, AuditEventType.UPDATE, "Subject", 50L, 42);
        Page<AuditLog> page = new PageImpl<>(List.of(log));
        when(auditLogRepository.findByStudyId(eq(10), any(Pageable.class))).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 20);
        Page<AuditLogDTO> result = service.listAuditLogs(10, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(AuditEventType.UPDATE, result.getContent().getFirst().getEventType());
        verify(auditLogRepository).findByStudyId(10, pageable);
    }

    // --- listAuditLogs (without studyId) ---

    @Test
    void listAuditLogs_withoutStudyId_returnsAll() {
        AuditLog log = createAuditLog(1L, null, AuditEventType.SYSTEM, "System", null, null);
        Page<AuditLog> page = new PageImpl<>(List.of(log));
        when(auditLogRepository.findAllByOrderByPerformedDateDesc(any(Pageable.class)))
                .thenReturn(page);

        Pageable pageable = PageRequest.of(0, 20);
        Page<AuditLogDTO> result = service.listAuditLogs(null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(AuditEventType.SYSTEM, result.getContent().getFirst().getEventType());
        verify(auditLogRepository).findAllByOrderByPerformedDateDesc(pageable);
    }

    // --- getAuditLog ---

    @Test
    void getAuditLog_whenFound_returnsDto() {
        AuditLog log = createAuditLog(1L, 10, AuditEventType.CREATE, "Study", 100L, 42);
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(log));

        AuditLogDTO result = service.getAuditLog(1L);

        assertEquals(1L, result.getId());
        assertEquals(10, result.getStudyId());
        assertEquals(AuditEventType.CREATE, result.getEventType());
        assertEquals("Study", result.getEntityType());
        assertEquals(100L, result.getEntityId());
        assertEquals(42, result.getPerformedBy());
    }

    @Test
    void getAuditLog_whenNotFound_throwsException() {
        when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getAuditLog(99L));
    }

    // --- factory method ---

    private static AuditLog createAuditLog(Long id, Integer studyId, AuditEventType eventType,
                                            String entityType, Long entityId, Integer performedBy) {
        AuditLog log = new AuditLog();
        ReflectionTestUtils.setField(log, "id", id);
        log.setStudyId(studyId);
        log.setEventType(eventType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setEntityLabel(entityType + " #" + entityId);
        log.setOldValue(null);
        log.setNewValue("test value");
        log.setPerformedBy(performedBy);
        ReflectionTestUtils.setField(log, "performedDate", LocalDateTime.now());
        log.setDetails("test details");
        log.setSourceModule("test-module");
        return log;
    }
}
