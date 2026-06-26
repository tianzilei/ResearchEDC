package org.researchedc.module.subjectgroup.repository;

import org.researchedc.module.subjectgroup.entity.StudyGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroupEntity, Integer> {

    List<StudyGroupEntity> findByStudyGroupClassId(Integer studyGroupClassId);

    Optional<StudyGroupEntity> findFirstByStudyGroupClassId(Integer studyGroupClassId);

    @Query(value = """
            SELECT sg.*
            FROM module_study_group sg
            JOIN module_study_group_class sgc ON sg.study_group_class_id = sgc.study_group_class_id
            WHERE sgc.study_id = ?1
            ORDER BY sg.study_group_id
            LIMIT 1
            """, nativeQuery = true)
    Optional<StudyGroupEntity> findFirstByStudyId(Integer studyId);

    Optional<StudyGroupEntity> findByNameAndStudyGroupClassId(String name, Integer studyGroupClassId);

    @Query(value = """
            SELECT sg.*
            FROM module_study_group sg
            JOIN module_study_group_class sgc ON sg.study_group_class_id = sgc.study_group_class_id
            JOIN module_study st ON sgc.study_id = st.study_id
            WHERE st.study_id = ?1 OR st.parent_study_id = ?1
            """, nativeQuery = true)
    List<StudyGroupEntity> findByStudyOrChildStudy(Integer studyId);

    @Query(value = """
            SELECT sg.*
            FROM module_study_group sg
            JOIN subject_group_map sgm ON sgm.study_group_id = sg.study_group_id
            JOIN module_study_subject ss ON sgm.study_subject_id = ss.study_subject_id
            WHERE ss.study_subject_id = ?1
              AND (ss.study_id = ?2 OR ss.study_id IN (
                  SELECT study_id FROM module_study WHERE parent_study_id = ?3
              ))
            """, nativeQuery = true)
    List<StudyGroupEntity> findByStudySubjectInStudyOrChildStudy(Integer studySubjectId, Integer studyId,
                                                                  Integer parentStudyId);

    @Query(value = """
            SELECT sg.*
            FROM module_study_group sg
            JOIN subject_group_map sgm ON sg.study_group_id = sgm.study_group_id
            WHERE sgm.study_subject_id = ?1
            """, nativeQuery = true)
    List<StudyGroupEntity> findByStudySubject(Integer studySubjectId);

    @Query(value = """
            SELECT sg.*
            FROM module_study_group sg
            JOIN subject_group_map sgm ON sgm.study_group_id = sg.study_group_id
            JOIN module_study_group_class sgc ON sgm.study_group_class_id = sgc.study_group_class_id
            WHERE sgm.study_subject_id = ?1
              AND sgc.name = ?2
            """, nativeQuery = true)
    List<StudyGroupEntity> findSubjectStudyGroup(Integer studySubjectId, String groupClassName);

    @Query(value = """
            SELECT sg.study_group_id, sg.name, sg.description, sg.study_group_class_id, sgmap.study_subject_id
            FROM module_study_group sg
            JOIN module_study_group_class sgc ON sg.study_group_class_id = sgc.study_group_class_id
            JOIN subject_group_map sgmap ON sg.study_group_id = sgmap.study_group_id
            WHERE sgc.study_id = ?1
            """, nativeQuery = true)
    List<Object[]> findSubjectGroupMapRows(Integer studyId);
}
