package org.researchedc.module.study.repository;

import java.util.List;
import java.util.Optional;
import org.researchedc.module.study.entity.StudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends JpaRepository<StudyEntity, Integer> {

    List<StudyEntity> findByParentStudyIdOrderByName(Integer parentStudyId);

    List<StudyEntity> findByParentStudyIdIsNullOrderByName();

    List<StudyEntity> findByParentStudyIdIsNotNullOrderByName();

    Optional<StudyEntity> findByOcOid(String ocOid);

    List<StudyEntity> findByNameContainingIgnoreCase(String name);

    List<StudyEntity> findByStatusIdOrderByName(Integer statusId);

    Optional<StudyEntity> findByUniqueIdentifier(String uniqueIdentifier);

    @Query(value = """
            SELECT s.*
            FROM module_study s
            JOIN module_study_user_role ur ON ur.study_id = s.study_id
            JOIN module_user_account ua ON ua.user_id = ur.user_id
            WHERE ua.user_name = ?1 AND s.status_id = 1
            ORDER BY s.name
            """, nativeQuery = true)
    List<StudyEntity> findByUserName(String userName);

    @Query(value = """
            SELECT s.*
            FROM module_study s
            JOIN module_study_user_role ur ON ur.study_id = s.study_id
            JOIN module_user_account ua ON ua.user_id = ur.user_id
            WHERE ua.user_name = ?1 AND s.status_id != 5 AND s.status_id != 6
            ORDER BY s.name
            """, nativeQuery = true)
    List<StudyEntity> findByUserNameNotRemoved(String userName);

    @Query(value = """
            SELECT DISTINCT s.study_id
            FROM module_study s
            WHERE s.parent_study_id = ?1
            """, nativeQuery = true)
    List<Integer> findSiteIdsByParentStudyId(Integer parentStudyId);

    @Query(value = """
            SELECT DISTINCT s.*
            FROM module_study s
            JOIN module_study_subject ss ON ss.study_id = s.study_id
            WHERE ss.study_subject_id = ?1
            """, nativeQuery = true)
    Optional<StudyEntity> findByStudySubjectId(Integer studySubjectId);

    @Query(value = """
            SELECT s.*
            FROM module_study s
            WHERE s.parent_study_id = ?1 AND s.unique_identifier = ?2
            """, nativeQuery = true)
    Optional<StudyEntity> findByParentStudyIdAndUniqueIdentifier(Integer parentStudyId, String uniqueIdentifier);

    @Query(value = """
            SELECT DISTINCT s.study_id
            FROM module_study s
            JOIN module_study_event_definition sed ON sed.study_id = s.study_id
            JOIN module_event_definition_crf edc ON edc.study_event_definition_id = sed.study_event_definition_id
            WHERE edc.crf_id = ?1
            """, nativeQuery = true)
    List<Integer> findStudyIdsByCrfId(Integer crfId);
}
