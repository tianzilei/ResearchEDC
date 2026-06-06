package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.ResponseType;

public interface ResponseTypeDao {

    ResponseType findByResponseTypeName(String name);

    ResponseType findByItemFormMetaDataId(Integer itemFormMetadataId);

}
