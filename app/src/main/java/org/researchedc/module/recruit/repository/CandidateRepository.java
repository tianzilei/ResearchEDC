package org.researchedc.module.recruit.repository;

import java.util.Collection;
import java.util.List;

import org.researchedc.module.recruit.entity.CandidateEntity;
import org.researchedc.module.recruit.enums.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository<CandidateEntity, Long> {
    List<CandidateEntity> findByStudyIdOrderByCreatedDateDesc(Integer studyId);
    List<CandidateEntity> findByStudyIdAndStatusOrderByCreatedDateDesc(Integer studyId, CandidateStatus status);
    List<CandidateEntity> findByStudyIdInOrderByStudyIdAscCreatedDateDesc(Collection<Integer> studyIds);
    boolean existsByStudyIdAndCandidateCodeIgnoreCase(Integer studyId, String candidateCode);
}
