package org.researchedc.module.crf.repository;

import org.researchedc.module.crf.entity.SectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<SectionEntity, Integer> {

    List<SectionEntity> findByCrfVersionIdOrderByOrdinal(Integer crfVersionId);
}
