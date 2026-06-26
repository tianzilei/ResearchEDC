package org.researchedc.module.datacapture.repository;

import java.util.List;
import org.researchedc.module.datacapture.entity.ItemGroupMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemGroupMetadataRepository extends JpaRepository<ItemGroupMetadataEntity, Integer> {

    List<ItemGroupMetadataEntity> findByItemIdAndCrfVersionId(Integer itemId, Integer crfVersionId);

    List<ItemGroupMetadataEntity> findByItemGroupIdAndCrfVersionId(Integer itemGroupId, Integer crfVersionId);

    List<ItemGroupMetadataEntity> findByCrfVersionId(Integer crfVersionId);

    @Query(value = """
            SELECT igm.*
            FROM module_item_group_metadata igm
            JOIN module_item_form_metadata ifm
              ON ifm.item_id = igm.item_id
             AND ifm.crf_version_id = igm.crf_version_id
            WHERE igm.item_group_id = ?1
              AND igm.crf_version_id = ?2
              AND ifm.section_id = ?3
            ORDER BY igm.ordinal
            """, nativeQuery = true)
    List<ItemGroupMetadataEntity> findMetaByGroupAndSection(Integer itemGroupId, Integer crfVersionId, Integer sectionId);

    @Query(value = """
            SELECT igm.*
            FROM module_item_group_metadata igm
            JOIN module_item_form_metadata ifm
              ON ifm.item_id = igm.item_id
             AND ifm.crf_version_id = igm.crf_version_id
            WHERE igm.item_group_id = ?1
              AND igm.crf_version_id = ?2
              AND ifm.section_id = ?3
            ORDER BY ifm.ordinal
            """, nativeQuery = true)
    List<ItemGroupMetadataEntity> findMetaByGroupAndSectionForPrint(Integer itemGroupId, Integer crfVersionId, Integer sectionId);
}
