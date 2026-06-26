package org.researchedc.module.crf.repository;

import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrfVersionRepository extends JpaRepository<CrfVersionEntity, Integer> {

    List<CrfVersionEntity> findByCrfIdOrderByCrfVersionId(Integer crfId);

    Optional<CrfVersionEntity> findByOcOid(String ocOid);

    List<CrfVersionEntity> findByCrfId(Integer crfId);

    List<CrfVersionEntity> findByCrfIdAndStatusId(Integer crfId, Integer statusId);

    List<CrfVersionEntity> findByCrfIdAndStatusIdNot(Integer crfId, Integer statusId);

    Optional<CrfVersionEntity> findByName(String name);

    List<CrfVersionEntity> findByOcOidContaining(String ocOid);

    Optional<CrfVersionEntity> findByNameAndCrfId(String name, Integer crfId);

    @Query(value = """
            SELECT DISTINCT cv.*
            FROM module_crf_version cv
            JOIN module_event_definition_crf edc ON edc.default_version_id = cv.crf_version_id
            JOIN module_study_event_definition sed ON sed.study_event_definition_id = edc.study_event_definition_id
            JOIN module_study s ON s.study_id = sed.study_id
            WHERE s.oc_oid = ?1
            """, nativeQuery = true)
    List<CrfVersionEntity> findByStudyOcOid(String studyOcOid);
}
