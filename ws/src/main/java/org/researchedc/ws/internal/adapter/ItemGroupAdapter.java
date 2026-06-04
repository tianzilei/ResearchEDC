package org.researchedc.ws.internal.adapter;

import java.util.List;

import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.dao.spi.IItemGroupDAO;
import org.springframework.stereotype.Repository;

@Repository
public class ItemGroupAdapter {

    private final IItemGroupDAO delegate;

    public ItemGroupAdapter(IItemGroupDAO delegate) {
        this.delegate = delegate;
    }

    public ItemGroupBean findByOid(String oid) {
        return (ItemGroupBean) delegate.findByOid(oid);
    }

    @SuppressWarnings("unchecked")
    public List<ItemGroupBean> findAllByOid(String oid) {
        return (List<ItemGroupBean>) delegate.findAllByOid(oid);
    }

    public IItemGroupDAO getDelegate() {
        return delegate;
    }
}
