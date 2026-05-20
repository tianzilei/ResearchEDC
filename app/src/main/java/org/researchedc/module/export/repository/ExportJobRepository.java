package org.researchedc.module.export.repository;

import java.util.List;
import org.researchedc.module.export.entity.ExportJob;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    List<ExportJob> findByStudyIdOrderByRequestedDateDesc(Integer studyId);

    List<ExportJob> findByStatusOrderByRequestedDateAsc(ExportJobStatus status);

    List<ExportJob> findByRequestedByOrderByRequestedDateDesc(Integer requestedBy);

    long countByStudyIdAndStatus(Integer studyId, ExportJobStatus status);
}
