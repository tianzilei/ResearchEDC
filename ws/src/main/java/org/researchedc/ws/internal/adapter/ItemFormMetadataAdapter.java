package org.researchedc.ws.internal.adapter;

import java.util.ArrayList;

import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.springframework.stereotype.Repository;

@Repository
public class ItemFormMetadataAdapter {

    private final IItemFormMetadataDAO delegate;

    public ItemFormMetadataAdapter(IItemFormMetadataDAO delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ItemFormMetadataBean> findAllByItemId(int itemId) {
        return (ArrayList<ItemFormMetadataBean>) delegate.findAllByItemId(itemId);
    }

    public IItemFormMetadataDAO getDelegate() {
        return delegate;
    }
}
