package org.researchedc.module.ecoa.repository;

import java.util.List;
import org.researchedc.module.ecoa.entity.EcoaSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcoaScheduleRepository extends JpaRepository<EcoaSchedule, Long> {
    List<EcoaSchedule> findByStudyIdOrderByDueAtAscCreatedDateAsc(Integer studyId);
}
