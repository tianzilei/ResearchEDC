package org.researchedc.module.crf.internal.adapter;

import org.researchedc.dao.spi.ResponseTypeDao;
import org.researchedc.domain.datamap.ResponseType;
import org.researchedc.module.crf.internal.adapter.repository.ResponseTypeRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("responseTypeDao")
@Primary
public class ResponseTypeDaoAdapter implements ResponseTypeDao {

    private final ResponseTypeRepository repository;

    public ResponseTypeDaoAdapter(ResponseTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public ResponseType findByResponseTypeName(String name) {
        return repository.findByName(name);
    }

    @Override
    public ResponseType findByItemFormMetaDataId(Integer itemFormMetadataId) {
        return repository.findByItemFormMetaDataId(itemFormMetadataId);
    }
}
