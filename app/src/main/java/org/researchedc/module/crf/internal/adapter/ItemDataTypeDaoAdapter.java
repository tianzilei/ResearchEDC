package org.researchedc.module.crf.internal.adapter;

import org.researchedc.dao.spi.ItemDataTypeDao;
import org.researchedc.domain.datamap.ItemDataType;
import org.researchedc.module.crf.internal.adapter.repository.ItemDataTypeRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("itemDataTypeDao")
@Primary
public class ItemDataTypeDaoAdapter implements ItemDataTypeDao {

    private final ItemDataTypeRepository repository;

    public ItemDataTypeDaoAdapter(ItemDataTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public ItemDataType findByItemDataTypeCode(String code) {
        return repository.findByCode(code);
    }

    @Override
    public ItemDataType findByItemDataTypeId(int itemDataTypeId) {
        return repository.findByItemDataTypeId(itemDataTypeId);
    }

    @Override
    public ItemDataType findByItemId(int itemId) {
        return repository.findByItemId(itemId);
    }
}
