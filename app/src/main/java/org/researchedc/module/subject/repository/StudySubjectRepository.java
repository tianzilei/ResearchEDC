package org.researchedc.module.subject.repository;

import java.util.List;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudySubjectRepository extends JpaRepository<StudySubjectEntity, Integer> {

    List<StudySubjectEntity> findByStudyIdOrderByLabel(Integer studyId);

    List<StudySubjectEntity> findBySubjectId(Integer subjectId);

    List<StudySubjectEntity> findByLabelContainingIgnoreCase(String label);
}
