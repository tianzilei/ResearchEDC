package org.researchedc.module.crf.internal.adapter;

import org.researchedc.dao.spi.ItemReferenceTypeDao;
import org.researchedc.domain.datamap.ItemReferenceType;
import org.researchedc.module.crf.internal.adapter.repository.ItemReferenceTypeRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("itemReferenceTypeDao")
@Primary
public class ItemReferenceTypeDaoAdapter implements ItemReferenceTypeDao {

    private final ItemReferenceTypeRepository repository;

    public ItemReferenceTypeDaoAdapter(ItemReferenceTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public ItemReferenceType findByItemReferenceTypeId(int itemReferenceTypeId) {
        return repository.findByItemReferenceTypeId(itemReferenceTypeId);
    }
}
