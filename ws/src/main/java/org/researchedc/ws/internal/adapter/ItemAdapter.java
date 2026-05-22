package org.researchedc.ws.internal.adapter;

import java.util.List;

import org.researchedc.bean.submit.ItemBean;
import org.researchedc.dao.submit.ItemDAO;
import org.springframework.stereotype.Repository;

@Repository
public class ItemAdapter {

    private final ItemDAO delegate;

    public ItemAdapter(ItemDAO delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    public List<ItemBean> findByOid(String oid) {
        return (List<ItemBean>) delegate.findByOid(oid);
    }

    public ItemBean findByPK(int id) {
        return (ItemBean) delegate.findByPK(id);
    }

    public int findAllRequiredByCRFVersionId(int crfVersionId) {
        return delegate.findAllRequiredByCRFVersionId(crfVersionId);
    }

    public ItemDAO getDelegate() {
        return delegate;
    }
}
