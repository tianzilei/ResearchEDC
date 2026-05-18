package org.akaza.openclinica.module.datacapture.repository;

import java.util.List;
import org.akaza.openclinica.module.datacapture.entity.ItemGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemGroupRepository extends JpaRepository<ItemGroupEntity, Integer> {

    List<ItemGroupEntity> findByCrfId(Integer crfId);
}
