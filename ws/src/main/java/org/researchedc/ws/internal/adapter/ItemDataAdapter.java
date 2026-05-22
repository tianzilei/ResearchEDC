package org.researchedc.ws.internal.adapter;

import java.util.ArrayList;

import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.dao.submit.ItemDataDAO;
import org.springframework.stereotype.Repository;

@Repository
public class ItemDataAdapter {

    private final ItemDataDAO delegate;

    public ItemDataAdapter(ItemDataDAO delegate) {
        this.delegate = delegate;
    }

    public ItemDataBean findByItemIdAndEventCRFIdAndOrdinal(int itemId, int eventCRFId, int ordinal) {
        return (ItemDataBean) delegate.findByItemIdAndEventCRFIdAndOrdinal(itemId, eventCRFId, ordinal);
    }

    public void setFormatDates(boolean formatDates) {
        delegate.setFormatDates(formatDates);
    }

    public int findAllRequiredByEventCRFId(EventCRFBean ecb) {
        return delegate.findAllRequiredByEventCRFId(ecb);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ItemDataBean> findAllBlankRequiredByEventCRFId(int eventCRFId, int crfVersionId) {
        return delegate.findAllBlankRequiredByEventCRFId(eventCRFId, crfVersionId);
    }

    public void updateStatusByEventCRF(EventCRFBean ecb, Status status) {
        delegate.updateStatusByEventCRF(ecb, status);
    }

    public ItemDataBean update(ItemDataBean bean) {
        return (ItemDataBean) delegate.update(bean);
    }

    public ItemDataBean create(ItemDataBean bean) {
        return (ItemDataBean) delegate.create(bean);
    }

    public ItemDataBean findByPK(int id) {
        return (ItemDataBean) delegate.findByPK(id);
    }

    public ItemDataDAO getDelegate() {
        return delegate;
    }
}
