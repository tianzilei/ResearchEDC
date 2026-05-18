package org.akaza.openclinica.module.audit.repository;

import java.util.List;
import org.akaza.openclinica.module.audit.entity.AuditLog;
import org.akaza.openclinica.module.audit.enums.AuditEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByStudyIdOrderByPerformedDateDesc(Integer studyId);

    Page<AuditLog> findByStudyId(Integer studyId, Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByPerformedDateDesc(String entityType, Long entityId);

    List<AuditLog> findByEventTypeAndPerformedByOrderByPerformedDateDesc(AuditEventType eventType, Integer performedBy);

    List<AuditLog> findBySourceModuleOrderByPerformedDateDesc(String sourceModule);

    Page<AuditLog> findAllByOrderByPerformedDateDesc(Pageable pageable);
}
