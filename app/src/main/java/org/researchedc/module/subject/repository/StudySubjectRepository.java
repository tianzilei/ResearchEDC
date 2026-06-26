package org.researchedc.module.subject.repository;

import java.util.List;
import java.util.Optional;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudySubjectRepository extends JpaRepository<StudySubjectEntity, Integer> {

    List<StudySubjectEntity> findByStudyIdOrderByLabel(Integer studyId);

    List<StudySubjectEntity> findBySubjectId(Integer subjectId);

    List<StudySubjectEntity> findByLabelContainingIgnoreCase(String label);

    List<StudySubjectEntity> findByStudyId(Integer studyId);

    List<StudySubjectEntity> findByStudyIdAndStatusIdOrderByLabel(Integer studyId, Integer statusId);

    List<StudySubjectEntity> findByLabelAndStudyId(String label, Integer studyId);

    Optional<StudySubjectEntity> findByOcOid(String ocOid);

    Optional<StudySubjectEntity> findByOcOidAndStudyId(String ocOid, Integer studyId);

    Optional<StudySubjectEntity> findBySubjectIdAndStudyId(Integer subjectId, Integer studyId);

    Optional<StudySubjectEntity> findTopByOrderByStudySubjectIdDesc();

    long countByStudyId(Integer studyId);

    long countByStudyIdAndStatusId(Integer studyId, Integer statusId);
}
