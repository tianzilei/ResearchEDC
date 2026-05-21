package org.researchedc.module.subjectgroup.repository;

import org.researchedc.module.subjectgroup.entity.StudyGroupClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyGroupClassRepository extends JpaRepository<StudyGroupClassEntity, Integer> {

    List<StudyGroupClassEntity> findByStudyId(Integer studyId);
}
