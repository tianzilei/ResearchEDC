package org.akaza.openclinica.module.randomization.repository;

import java.util.List;
import java.util.Optional;
import org.akaza.openclinica.module.randomization.entity.RandomizationScheme;
import org.akaza.openclinica.module.randomization.enums.SchemeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomizationSchemeRepository extends JpaRepository<RandomizationScheme, Long> {

    List<RandomizationScheme> findByStudyId(Integer studyId);

    Optional<RandomizationScheme> findByStudyIdAndStatus(Integer studyId, SchemeStatus status);

    List<RandomizationScheme> findByStudyIdOrderByCreatedDateDesc(Integer studyId);

    long countByStudyIdAndStatus(Integer studyId, SchemeStatus status);
}
