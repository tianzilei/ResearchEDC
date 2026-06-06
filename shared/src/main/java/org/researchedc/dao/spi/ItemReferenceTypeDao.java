package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.ItemReferenceType;

public interface ItemReferenceTypeDao {

    ItemReferenceType findByItemReferenceTypeId(int item_reference_type_id);

}
