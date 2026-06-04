package org.researchedc.module.crf.repository;

import org.researchedc.module.crf.entity.ItemFormMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemFormMetadataRepository extends JpaRepository<ItemFormMetadataEntity, Integer> {

    List<ItemFormMetadataEntity> findByCrfVersionId(Integer crfVersionId);

    List<ItemFormMetadataEntity> findByCrfVersionIdOrderByOrdinal(Integer crfVersionId);

    List<ItemFormMetadataEntity> findBySectionIdOrderByOrdinal(Integer sectionId);

    List<ItemFormMetadataEntity> findByCrfVersionIdAndSectionIdOrderByOrdinal(Integer crfVersionId, Integer sectionId);

    List<ItemFormMetadataEntity> findByItemIdAndCrfVersionId(Integer itemId, Integer crfVersionId);
}
