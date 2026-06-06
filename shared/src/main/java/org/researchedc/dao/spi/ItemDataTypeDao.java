package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.ItemDataType;

public interface ItemDataTypeDao {

    ItemDataType findByItemDataTypeCode(String item_data_type_code);

    ItemDataType findByItemDataTypeId(int item_data_type_id);

    ItemDataType findByItemId(int item_id);

}
