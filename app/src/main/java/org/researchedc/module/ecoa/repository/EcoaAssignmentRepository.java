package org.researchedc.module.ecoa.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.researchedc.module.ecoa.entity.EcoaAssignment;
import org.researchedc.module.ecoa.enums.EcoaAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcoaAssignmentRepository extends JpaRepository<EcoaAssignment, Long> {
    List<EcoaAssignment> findByStudyIdOrderByDueAtAscCreatedDateAsc(Integer studyId);

    List<EcoaAssignment> findByStudyIdInOrderByDueAtAscCreatedDateAsc(Collection<Integer> studyIds);

    List<EcoaAssignment> findByParticipantAccountIdAndStatusInOrderByDueAtAscCreatedDateAsc(
            Long participantAccountId,
            Collection<EcoaAssignmentStatus> statuses);

    List<EcoaAssignment> findByStatusInAndDueAtBefore(Collection<EcoaAssignmentStatus> statuses,
                                                       LocalDateTime dueAt);
}
