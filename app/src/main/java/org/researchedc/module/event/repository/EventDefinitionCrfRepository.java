package org.researchedc.module.event.repository;

import java.util.List;
import java.util.Optional;
import org.researchedc.module.event.entity.EventDefinitionCrfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventDefinitionCrfRepository extends JpaRepository<EventDefinitionCrfEntity, Integer> {

    List<EventDefinitionCrfEntity> findByStudyEventDefinitionId(Integer studyEventDefinitionId);

    List<EventDefinitionCrfEntity> findByStudyEventDefinitionIdAndStudyId(Integer studyEventDefinitionId, Integer studyId);

    List<EventDefinitionCrfEntity> findByCrfId(Integer crfId);

    List<EventDefinitionCrfEntity> findByDefaultVersionId(Integer defaultVersionId);

    Optional<EventDefinitionCrfEntity> findByStudyEventDefinitionIdAndCrfId(Integer studyEventDefinitionId, Integer crfId);

    Optional<EventDefinitionCrfEntity> findByStudyEventDefinitionIdAndCrfIdAndStudyId(
            Integer studyEventDefinitionId, Integer crfId, Integer studyId);

    List<EventDefinitionCrfEntity> findByStudyEventDefinitionIdAndOrdinal(Integer studyEventDefinitionId, Integer ordinal);

    List<EventDefinitionCrfEntity> findByStudyEventDefinitionIdAndStatusId(Integer studyEventDefinitionId, Integer statusId);

    List<EventDefinitionCrfEntity> findByStudyEventDefinitionIdAndStatusIdAndStudyId(
            Integer studyEventDefinitionId, Integer statusId, Integer studyId);

    @Query(value = """
            SELECT edc.*
            FROM module_event_definition_crf edc
            WHERE edc.study_event_definition_id = ?1
              AND edc.parent_id IS NULL
              AND edc.status_id = 1
            """, nativeQuery = true)
    List<EventDefinitionCrfEntity> findActiveParentsByEventDefinitionId(Integer studyEventDefinitionId);

    @Query(value = """
            SELECT edc.*
            FROM module_event_definition_crf edc
            WHERE edc.study_event_definition_id = ?1
              AND edc.study_id = ?2
              AND edc.status_id = 1
              AND edc.hide_crf = false
            """, nativeQuery = true)
    List<EventDefinitionCrfEntity> findActiveNonHiddenByEventDefinitionIdAndStudy(
            Integer studyEventDefinitionId, Integer studyId);

    @Query(value = """
            SELECT edc.*
            FROM module_event_definition_crf edc
            WHERE edc.study_event_definition_id = ?1
              AND edc.study_id = ?2
              AND edc.parent_id IS NULL
            """, nativeQuery = true)
    List<EventDefinitionCrfEntity> findByDefinitionAndSiteIdAndParentStudyId(
            Integer studyEventDefinitionId, Integer siteId, Integer parentStudyId);

    @Query(value = """
            SELECT edc.*
            FROM module_event_definition_crf edc
            WHERE edc.parent_id = ?1
              AND edc.study_id = ?2
            """, nativeQuery = true)
    List<EventDefinitionCrfEntity> findByCrfDefinitionInSiteOnly(Integer definitionId, Integer crfId);

    List<EventDefinitionCrfEntity> findByParentStudyIdAndStatusId(Integer parentStudyId, Integer statusId);

    @Query(value = """
            SELECT edc.*
            FROM module_event_definition_crf edc
            WHERE edc.submission_url = ?1 AND edc.study_id = ?2
            """, nativeQuery = true)
    List<EventDefinitionCrfEntity> findBySubmissionUrlAndStudyId(String submissionUrl, Integer studyId);
}
