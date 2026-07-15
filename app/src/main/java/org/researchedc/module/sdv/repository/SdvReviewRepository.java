package org.researchedc.module.sdv.repository;

import java.util.List;
import java.util.Optional;

import org.researchedc.module.sdv.entity.SdvReviewEntity;
import org.researchedc.module.sdv.enums.SdvStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SdvReviewRepository extends JpaRepository<SdvReviewEntity, Long> {
    List<SdvReviewEntity> findByStudyIdOrderByCreatedDateDesc(Integer studyId);
    List<SdvReviewEntity> findByStudyIdAndStatusOrderByCreatedDateDesc(Integer studyId, SdvStatus status);
    Optional<SdvReviewEntity> findByEventCrfId(Integer eventCrfId);
}
