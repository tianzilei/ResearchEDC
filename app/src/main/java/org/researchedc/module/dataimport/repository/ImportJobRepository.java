package org.researchedc.module.dataimport.repository;

import java.util.List;
import org.researchedc.module.dataimport.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    List<ImportJob> findByStudyIdOrderByRequestedDateDesc(Integer studyId);

    List<ImportJob> findByRequestedByOrderByRequestedDateDesc(Integer requestedBy);
}
