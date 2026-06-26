package org.researchedc.module.subject.repository;

import java.util.List;
import java.util.Optional;
import org.researchedc.module.subject.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, Integer> {

    Optional<SubjectEntity> findByUniqueIdentifier(String uniqueIdentifier);

    List<SubjectEntity> findByUniqueIdentifierContainingIgnoreCase(String query);

    List<SubjectEntity> findByStatusId(int statusId);

    List<SubjectEntity> findByGender(String gender);

    List<SubjectEntity> findByGenderAndSubjectIdNot(String gender, int subjectId);

    @Query(value = "SELECT s.* FROM module_subject s "
            + "JOIN module_study_subject ss ON s.subject_id = ss.subject_id "
            + "JOIN module_study st ON ss.study_id = st.study_id "
            + "WHERE s.unique_identifier = :uniqueIdentifier "
            + "AND (st.parent_study_id = :studyId OR st.study_id = :studyId)",
            nativeQuery = true)
    Optional<SubjectEntity> findByUniqueIdentifierAndAnyStudyNative(
            @Param("uniqueIdentifier") String uniqueIdentifier,
            @Param("studyId") int studyId);

    @Query(value = "SELECT s.* FROM module_subject s "
            + "JOIN module_study_subject ss ON s.subject_id = ss.subject_id "
            + "WHERE s.unique_identifier = :uniqueIdentifier AND ss.study_id = :studyId",
            nativeQuery = true)
    Optional<SubjectEntity> findByUniqueIdentifierAndStudyNative(
            @Param("uniqueIdentifier") String uniqueIdentifier,
            @Param("studyId") int studyId);

    @Query(value = "SELECT s.* FROM module_subject s "
            + "JOIN module_study_subject ss ON s.subject_id = ss.subject_id "
            + "JOIN module_study st ON ss.study_id = st.study_id "
            + "WHERE s.unique_identifier = :uniqueIdentifier AND st.parent_study_id = :studyId",
            nativeQuery = true)
    Optional<SubjectEntity> findByUniqueIdentifierAndParentStudyNative(
            @Param("uniqueIdentifier") String uniqueIdentifier,
            @Param("studyId") int studyId);
}
