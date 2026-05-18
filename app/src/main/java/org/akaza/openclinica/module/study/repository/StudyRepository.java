package org.akaza.openclinica.module.study.repository;

import java.util.List;
import java.util.Optional;
import org.akaza.openclinica.module.study.entity.StudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends JpaRepository<StudyEntity, Integer> {

    List<StudyEntity> findByParentStudyIdOrderByName(Integer parentStudyId);

    List<StudyEntity> findByParentStudyIdIsNullOrderByName();

    List<StudyEntity> findByParentStudyIdIsNotNullOrderByName();

    Optional<StudyEntity> findByOcOid(String ocOid);

    List<StudyEntity> findByNameContainingIgnoreCase(String name);
}
