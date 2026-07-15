package org.researchedc.module.participantaccess.repository;

import java.util.List;
import java.util.Optional;
import org.researchedc.module.participantaccess.entity.ParticipantAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantAccountRepository extends JpaRepository<ParticipantAccount, Long> {
    List<ParticipantAccount> findByStudyIdOrderByDisplayLabelAsc(Integer studyId);

    Optional<ParticipantAccount> findByStudySubjectId(Integer studySubjectId);
}
