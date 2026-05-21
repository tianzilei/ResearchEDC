package org.researchedc.module.subjectgroup.repository;

import org.researchedc.module.subjectgroup.entity.StudyGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroupEntity, Integer> {

    List<StudyGroupEntity> findByStudyGroupClassId(Integer studyGroupClassId);
}
