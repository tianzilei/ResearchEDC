package org.researchedc.module.econsent.repository;

import java.util.Collection;
import java.util.List;
import org.researchedc.module.econsent.entity.ConsentAssignment;
import org.researchedc.module.econsent.enums.ConsentAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentAssignmentRepository extends JpaRepository<ConsentAssignment, Long> {
    List<ConsentAssignment> findByStudyIdOrderByCreatedDateDesc(Integer studyId);

    List<ConsentAssignment> findByParticipantAccountIdAndStatusInOrderByCreatedDateDesc(
            Long participantAccountId,
            Collection<ConsentAssignmentStatus> statuses);

    List<ConsentAssignment> findByStudySubjectIdAndConsentVersionIdAndStatusIn(
            Integer studySubjectId,
            Long consentVersionId,
            Collection<ConsentAssignmentStatus> statuses);
}
