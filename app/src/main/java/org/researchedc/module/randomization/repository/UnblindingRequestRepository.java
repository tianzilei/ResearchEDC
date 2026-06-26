package org.researchedc.module.randomization.repository;

import java.util.List;
import org.researchedc.module.randomization.entity.UnblindingRequest;
import org.researchedc.module.randomization.enums.UnblindingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnblindingRequestRepository extends JpaRepository<UnblindingRequest, Long> {

    List<UnblindingRequest> findByAssignmentSchemeIdOrderByRequestedDateDesc(Long schemeId);

    List<UnblindingRequest> findByStatusOrderByRequestedDateAsc(UnblindingStatus status);

    List<UnblindingRequest> findByAssignmentId(Long assignmentId);
}
