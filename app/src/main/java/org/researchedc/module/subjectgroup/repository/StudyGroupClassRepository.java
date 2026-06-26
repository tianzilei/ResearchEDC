package org.researchedc.module.subjectgroup.repository;

import org.researchedc.module.subjectgroup.entity.StudyGroupClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyGroupClassRepository extends JpaRepository<StudyGroupClassEntity, Integer> {

    List<StudyGroupClassEntity> findByStudyId(Integer studyId);

    List<StudyGroupClassEntity> findByStudyIdAndStatusId(Integer studyId, Integer statusId);

    @Query(value = """
            SELECT sg.*
            FROM module_study_group_class sg
            JOIN module_study st ON sg.study_id = st.study_id
            WHERE st.study_id = :studyId OR st.parent_study_id = :studyId
            ORDER BY sg.name ASC
            """, nativeQuery = true)
    List<StudyGroupClassEntity> findByStudyOrChildStudy(Integer studyId);

    @Query(value = """
            SELECT sg.*
            FROM module_study_group_class sg
            JOIN module_study st ON sg.study_id = st.study_id
            WHERE (st.study_id = :studyId OR st.parent_study_id = :studyId)
              AND sg.status_id = :statusId
            ORDER BY sg.name ASC
            """, nativeQuery = true)
    List<StudyGroupClassEntity> findByStudyOrChildStudyAndStatus(Integer studyId, Integer statusId);
}
