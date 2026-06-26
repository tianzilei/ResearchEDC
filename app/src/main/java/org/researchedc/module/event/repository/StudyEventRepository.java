package org.researchedc.module.event.repository;

import java.util.List;
import java.util.Optional;

import org.researchedc.module.event.entity.StudyEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyEventRepository extends JpaRepository<StudyEventEntity, Integer> {

    List<StudyEventEntity> findByStudySubjectIdOrderByDateStart(Integer studySubjectId);

    List<StudyEventEntity> findByStudyEventDefinitionId(Integer studyEventDefinitionId);

    List<StudyEventEntity> findByStudySubjectId(Integer studySubjectId);

    List<StudyEventEntity> findByStudyEventDefinitionIdAndStudySubjectId(Integer studyEventDefinitionId, Integer studySubjectId);

    List<StudyEventEntity> findByStudyEventDefinitionIdAndStudySubjectIdOrderBySampleOrdinal(Integer studyEventDefinitionId, Integer studySubjectId);

    Optional<StudyEventEntity> findTopByStudyEventDefinitionIdAndStudySubjectIdOrderBySampleOrdinalDesc(Integer studyEventDefinitionId, Integer studySubjectId);

    List<StudyEventEntity> findByStudySubjectIdAndStudyEventDefinitionIdAndSampleOrdinal(Integer studySubjectId, Integer studyEventDefinitionId, Integer sampleOrdinal);
}
