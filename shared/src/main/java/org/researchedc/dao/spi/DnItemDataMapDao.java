package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.DnItemDataMap;

import java.util.List;

public interface DnItemDataMapDao {

    List<DnItemDataMap> findByItemData(Integer itemDataId);

    default DnItemDataMap saveOrUpdate(DnItemDataMap dnItemDataMap) {
        throw new UnsupportedOperationException();
    }

}
