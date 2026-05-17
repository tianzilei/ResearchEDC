package org.akaza.openclinica.module.randomization.repository;

import java.util.List;
import org.akaza.openclinica.module.randomization.entity.RandomizationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomizationAuditLogRepository extends JpaRepository<RandomizationAuditLog, Long> {

    List<RandomizationAuditLog> findBySchemeIdOrderByPerformedDateDesc(Long schemeId);

    List<RandomizationAuditLog> findByStudyIdOrderByPerformedDateDesc(Integer studyId);
}
