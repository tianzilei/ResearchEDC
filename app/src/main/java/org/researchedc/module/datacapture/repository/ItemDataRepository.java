package org.researchedc.module.datacapture.repository;

import java.util.List;
import org.researchedc.module.datacapture.entity.ItemDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemDataRepository extends JpaRepository<ItemDataEntity, Integer> {

    List<ItemDataEntity> findByEventCrfIdOrderByItemId(Integer eventCrfId);

    List<ItemDataEntity> findByEventCrfIdAndItemId(Integer eventCrfId, Integer itemId);

    List<ItemDataEntity> findByEventCrfId(Integer eventCrfId);

    List<ItemDataEntity> findByEventCrfIdAndItemIdAndStatusIdNot(Integer eventCrfId, Integer itemId, Integer statusId);

    List<ItemDataEntity> findByItemIdAndEventCrfId(Integer itemId, Integer eventCrfId);

    List<ItemDataEntity> findByItemIdAndEventCrfIdAndOrdinal(Integer itemId, Integer eventCrfId, Integer ordinal);
}
