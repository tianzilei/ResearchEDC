package org.akaza.openclinica.module.randomization.repository;

import java.util.List;
import java.util.Optional;
import org.akaza.openclinica.module.randomization.entity.RandomizationAssignment;
import org.akaza.openclinica.module.randomization.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomizationAssignmentRepository extends JpaRepository<RandomizationAssignment, Long> {

    List<RandomizationAssignment> findBySchemeId(Long schemeId);

    Optional<RandomizationAssignment> findBySchemeIdAndStudySubjectId(Long schemeId, Integer studySubjectId);

    long countBySchemeIdAndStatus(Long schemeId, AssignmentStatus status);

    long countBySchemeIdAndArmIdAndStatus(Long schemeId, Long armId, AssignmentStatus status);

    List<RandomizationAssignment> findBySchemeIdAndStatusOrderByAssignedDateDesc(Long schemeId, AssignmentStatus status);
}
