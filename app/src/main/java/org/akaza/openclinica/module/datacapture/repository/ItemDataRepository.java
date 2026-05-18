package org.akaza.openclinica.module.datacapture.repository;

import java.util.List;
import org.akaza.openclinica.module.datacapture.entity.ItemDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemDataRepository extends JpaRepository<ItemDataEntity, Integer> {

    List<ItemDataEntity> findByEventCrfIdOrderByItemId(Integer eventCrfId);

    List<ItemDataEntity> findByEventCrfIdAndItemId(Integer eventCrfId, Integer itemId);
}
