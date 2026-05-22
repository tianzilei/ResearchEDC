package org.researchedc.ws.internal.adapter;

import java.util.ArrayList;

import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.springframework.stereotype.Repository;

@Repository
public class ItemFormMetadataAdapter {

    private final ItemFormMetadataDAO delegate;

    public ItemFormMetadataAdapter(ItemFormMetadataDAO delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ItemFormMetadataBean> findAllByItemId(int itemId) {
        return (ArrayList<ItemFormMetadataBean>) delegate.findAllByItemId(itemId);
    }

    public ItemFormMetadataDAO getDelegate() {
        return delegate;
    }
}
