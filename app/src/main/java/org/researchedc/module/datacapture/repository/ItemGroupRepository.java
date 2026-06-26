package org.researchedc.module.datacapture.repository;

import java.util.List;
import org.researchedc.module.datacapture.entity.ItemGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemGroupRepository extends JpaRepository<ItemGroupEntity, Integer> {

    List<ItemGroupEntity> findByCrfId(Integer crfId);

    List<ItemGroupEntity> findByName(String name);

    List<ItemGroupEntity> findByOcOid(String oid);

    List<ItemGroupEntity> findByOcOidAndCrfId(String oid, Integer crfId);

    List<ItemGroupEntity> findByStatusId(Integer statusId);

    @Query(value = "SELECT ig.* FROM module_item_group ig, module_item_group_metadata igm, module_item it"
            + " WHERE it.item_id = ?1 AND it.item_id = igm.item_id"
            + " AND igm.item_group_id = ig.item_group_id", nativeQuery = true)
    List<ItemGroupEntity> findGroupsByItemIdNative(Integer itemId);

    @Query(value = "SELECT DISTINCT ig.* FROM module_item_group ig, module_item_group_metadata igm"
            + " WHERE igm.crf_version_id = ?1"
            + " AND ig.item_group_id = igm.item_group_id", nativeQuery = true)
    List<ItemGroupEntity> findGroupByCRFVersionIdNative(Integer crfVersionId);

    @Query(value = "SELECT DISTINCT ig.* FROM module_item_group ig, module_item_group_metadata igm"
            + " WHERE igm.crf_version_id = ?1"
            + " AND ig.item_group_id = igm.item_group_id"
            + " AND ig.name != 'Ungrouped'", nativeQuery = true)
    List<ItemGroupEntity> findOnlyGroupsByCRFVersionIdNative(Integer crfVersionId);

    @Query(value = "SELECT DISTINCT ig.* FROM module_item_group ig, module_item_group_metadata igm"
            + " WHERE ig.item_group_id = igm.item_group_id"
            + " AND igm.crf_version_id = ?1"
            + " AND ig.name = ?2", nativeQuery = true)
    List<ItemGroupEntity> findGroupByGroupNameAndCrfVersionIdNative(Integer crfVersionId, String groupName);

    @Query(value = "SELECT DISTINCT ig.* FROM module_item_group ig, module_item_group_metadata igm"
            + " WHERE ig.item_group_id = igm.item_group_id"
            + " AND igm.crf_version_id = ?1"
            + " AND igm.item_id = ?2", nativeQuery = true)
    List<ItemGroupEntity> findGroupByItemIdCrfVersionIdNative(Integer crfVersionId, Integer itemId);

    @Query(value = "SELECT DISTINCT ig.* FROM module_item_group ig, module_item_group_metadata igm,"
            + " module_section s, module_item_form_metadata ifm"
            + " WHERE ifm.section_id = ?1"
            + " AND igm.crf_version_id = s.crf_version_id"
            + " AND ig.item_group_id = igm.item_group_id"
            + " AND s.section_id = ifm.section_id"
            + " AND ifm.item_id = igm.item_id", nativeQuery = true)
    List<ItemGroupEntity> findGroupBySectionIdNative(Integer sectionId);

    @Query(value = "SELECT DISTINCT ig.* FROM module_item_group ig, module_item_group_metadata igm,"
            + " module_section s, module_item_form_metadata ifm"
            + " WHERE ifm.section_id = ?1"
            + " AND igm.crf_version_id = s.crf_version_id"
            + " AND ig.item_group_id = igm.item_group_id"
            + " AND s.section_id = ifm.section_id"
            + " AND ifm.item_id = igm.item_id"
            + " AND ig.name != 'Ungrouped'"
            + " AND igm.repeating_group = true", nativeQuery = true)
    List<ItemGroupEntity> findLegitGroupBySectionIdNative(Integer sectionId);

    @Query(value = "SELECT DISTINCT ig.* FROM module_item_group ig, module_item_group_metadata igm,"
            + " module_section s, module_item_form_metadata ifm"
            + " WHERE ifm.section_id = ?1"
            + " AND igm.crf_version_id = s.crf_version_id"
            + " AND ig.item_group_id = igm.item_group_id"
            + " AND s.section_id = ifm.section_id"
            + " AND ifm.item_id = igm.item_id", nativeQuery = true)
    List<ItemGroupEntity> findLegitGroupAllBySectionIdNative(Integer sectionId);

    @Query(value = "SELECT ig.* FROM module_item_group ig, module_item_group_metadata igm,"
            + " module_section s, module_item_form_metadata ifm"
            + " WHERE ifm.section_id = ?1"
            + " AND igm.crf_version_id = s.crf_version_id"
            + " AND ig.item_group_id = igm.item_group_id"
            + " AND s.section_id = ifm.section_id"
            + " AND ifm.item_id = igm.item_id"
            + " AND ig.name != 'Ungrouped'"
            + " AND igm.repeating_group = true"
            + " LIMIT 1", nativeQuery = true)
    List<ItemGroupEntity> findTopOneGroupBySectionIdNative(Integer sectionId);
}
