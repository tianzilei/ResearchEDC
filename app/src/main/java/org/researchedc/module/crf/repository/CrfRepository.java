package org.researchedc.module.crf.repository;

import org.researchedc.module.crf.entity.CrfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrfRepository extends JpaRepository<CrfEntity, Integer> {

    Optional<CrfEntity> findByOcOid(String ocOid);

    Optional<CrfEntity> findByName(String name);

    @Query("SELECT c FROM ModuleCrf c WHERE c.name = :name AND c.crfId <> :crfId")
    Optional<CrfEntity> findAnotherByName(@Param("name") String name, @Param("crfId") Integer crfId);

    List<CrfEntity> findBySourceStudyId(Integer sourceStudyId);

    List<CrfEntity> findByStatusId(Integer statusId);

    List<CrfEntity> findBySourceStudyIdAndStatusId(Integer sourceStudyId, Integer statusId);

    @Query("SELECT DISTINCT c FROM ModuleCrf c, ModuleCrfVersion cv, ModuleItemFormMetadata m, ModuleItem i WHERE c.crfId = cv.crfId AND cv.crfVersionId = m.crfVersionId AND m.itemId = i.itemId AND i.ocOid = :itemOid")
    Optional<CrfEntity> findByItemOid(@Param("itemOid") String itemOid);
}
