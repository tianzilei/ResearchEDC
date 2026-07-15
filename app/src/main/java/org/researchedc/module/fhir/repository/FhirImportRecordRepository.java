package org.researchedc.module.fhir.repository;

import java.util.List;
import org.researchedc.module.fhir.entity.FhirImportRecordEntity;
import org.researchedc.module.fhir.enums.FhirImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FhirImportRecordRepository extends JpaRepository<FhirImportRecordEntity, Long> {
    List<FhirImportRecordEntity> findByStudyIdOrderByCreatedDateDesc(Integer studyId);
    List<FhirImportRecordEntity> findByStudyIdAndStatusOrderByCreatedDateDesc(Integer studyId, FhirImportStatus status);
}
