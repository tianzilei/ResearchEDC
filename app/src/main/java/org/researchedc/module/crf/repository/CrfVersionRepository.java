package org.researchedc.module.crf.repository;

import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
