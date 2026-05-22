package org.researchedc.ws.internal.adapter;

import java.util.ArrayList;

import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.springframework.stereotype.Repository;

@Repository
public class CRFVersionAdapter {

    private final CRFVersionDAO delegate;

    public CRFVersionAdapter(CRFVersionDAO delegate) {
        this.delegate = delegate;
    }

    public CRFVersionBean findByPK(int id) {
        return (CRFVersionBean) delegate.findByPK(id);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<CRFVersionBean> findAllByOid(String oid) {
        return (ArrayList<CRFVersionBean>) delegate.findAllByOid(oid);
    }

    public CRFVersionDAO getDelegate() {
        return delegate;
    }
}
