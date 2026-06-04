package org.researchedc.module.event.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.researchedc.module.event.entity.StudyEventDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyEventDefinitionRepository extends JpaRepository<StudyEventDefinitionEntity, Integer> {

    List<StudyEventDefinitionEntity> findByStudyIdOrderByName(Integer studyId);

    Optional<StudyEventDefinitionEntity> findByOcOid(String ocOid);

    Optional<StudyEventDefinitionEntity> findByOcOidAndStudyId(String ocOid, Integer studyId);

    List<StudyEventDefinitionEntity> findByOcOidAndStudyIdIn(String ocOid, Collection<Integer> studyIds);

    List<StudyEventDefinitionEntity> findByStatusIdAndStudyId(Integer statusId, Integer studyId);

    List<StudyEventDefinitionEntity> findByName(String name);

    @Query(value = """
            SELECT sed.* FROM module_study_event_definition sed
            JOIN module_event_definition_crf edc ON edc.study_event_definition_id = sed.study_event_definition_id
            WHERE edc.event_definition_crf_id = ?1
            """, nativeQuery = true)
    Optional<StudyEventDefinitionEntity> findByEventDefinitionCRFId(Integer eventDefinitionCRFId);
}
