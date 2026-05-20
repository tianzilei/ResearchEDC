package org.researchedc.module.crf.repository;

import org.researchedc.module.crf.entity.ItemFormMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemFormMetadataRepository extends JpaRepository<ItemFormMetadataEntity, Integer> {

    List<ItemFormMetadataEntity> findByCrfVersionId(Integer crfVersionId);
}
