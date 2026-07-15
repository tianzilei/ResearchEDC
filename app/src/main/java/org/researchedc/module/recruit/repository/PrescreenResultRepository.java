package org.researchedc.module.recruit.repository;

import java.util.List;

import org.researchedc.module.recruit.entity.PrescreenResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescreenResultRepository extends JpaRepository<PrescreenResultEntity, Long> {
    List<PrescreenResultEntity> findByCandidateIdOrderByReviewedDateDesc(Long candidateId);
}
