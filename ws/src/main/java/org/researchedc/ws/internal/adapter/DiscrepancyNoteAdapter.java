package org.researchedc.ws.internal.adapter;

import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;

public class DiscrepancyNoteAdapter {

    private final IDiscrepancyNoteDAO delegate;

    public DiscrepancyNoteAdapter(IDiscrepancyNoteDAO delegate) {
        this.delegate = delegate;
    }

    public DiscrepancyNoteBean create(DiscrepancyNoteBean bean) {
        return (DiscrepancyNoteBean) delegate.create(bean);
    }

    public void createMapping(DiscrepancyNoteBean bean) {
        delegate.createMapping(bean);
    }

    public IDiscrepancyNoteDAO getDelegate() {
        return delegate;
    }
}
