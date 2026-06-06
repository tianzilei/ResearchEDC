package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.ItemDataFlag;

import java.util.List;

public interface ItemDataFlagDao {

    List<ItemDataFlag> findAllByEventCrfPath(int tag_id, String eventCrfPath);

    ItemDataFlag findByItemDataPath(int tag_id, String itemDataPath);

    default ItemDataFlag findById(Integer id) {
        throw new UnsupportedOperationException();
    }

    default ItemDataFlag saveOrUpdate(ItemDataFlag itemDataFlag) {
        throw new UnsupportedOperationException();
    }

}
