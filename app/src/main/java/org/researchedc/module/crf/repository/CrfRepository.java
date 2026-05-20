package org.researchedc.module.crf.repository;

import org.researchedc.module.crf.entity.CrfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CrfRepository extends JpaRepository<CrfEntity, Integer> {

    Optional<CrfEntity> findByOcOid(String ocOid);
}
