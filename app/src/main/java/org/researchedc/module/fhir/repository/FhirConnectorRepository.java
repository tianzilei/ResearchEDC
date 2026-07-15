package org.researchedc.module.fhir.repository;

import java.util.List;
import org.researchedc.module.fhir.entity.FhirConnectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FhirConnectorRepository extends JpaRepository<FhirConnectorEntity, Long> {
    List<FhirConnectorEntity> findByStudyIdOrderByCreatedDateDesc(Integer studyId);
}
