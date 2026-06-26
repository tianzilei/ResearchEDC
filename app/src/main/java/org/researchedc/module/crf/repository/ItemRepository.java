package org.researchedc.module.crf.repository;

import org.researchedc.module.crf.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Integer> {

    List<ItemEntity> findByOcOid(String ocOid);

    List<ItemEntity> findByName(String name);

    List<ItemEntity> findByStatusId(Integer statusId);

    @Query("SELECT i FROM ModuleItem i, ModuleItemFormMetadata m WHERE i.itemId = m.itemId AND m.sectionId = :sectionId")
    List<ItemEntity> findBySectionId(@Param("sectionId") Integer sectionId);

    @Query("SELECT i FROM ModuleItem i, ModuleItemFormMetadata m WHERE i.itemId = m.itemId AND m.sectionId = :sectionId ORDER BY m.ordinal")
    List<ItemEntity> findBySectionIdOrderedByOrdinal(@Param("sectionId") Integer sectionId);

    @Query("SELECT i FROM ModuleItem i, ModuleItemFormMetadata m, ModuleSection s WHERE i.itemId = m.itemId AND m.sectionId = s.sectionId AND s.parentId = :parentId AND s.crfVersionId = :crfVersionId")
    List<ItemEntity> findByParentIdAndCrfVersionId(@Param("parentId") Integer parentId, @Param("crfVersionId") Integer crfVersionId);

    @Query("SELECT i FROM ModuleItem i, ModuleItemFormMetadata m, ModuleCrfVersion cv WHERE i.itemId = m.itemId AND m.crfVersionId = cv.crfVersionId AND i.name = :name AND cv.crfId = :crfId")
    List<ItemEntity> findByNameAndCrfId(@Param("name") String name, @Param("crfId") Integer crfId);

    @Query("SELECT DISTINCT i FROM ModuleItem i, ModuleItemFormMetadata m, ModuleCrfVersion cv WHERE i.itemId = m.itemId AND m.crfVersionId = cv.crfVersionId AND cv.crfId = :crfId")
    List<ItemEntity> findByCrfId(@Param("crfId") Integer crfId);
}
