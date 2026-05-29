package org.researchedc.ws.internal.adapter;

import java.util.ArrayList;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.dao.spi.ICrfDAO;
import org.springframework.stereotype.Repository;

@Repository
public class CRFAdapter {

    private final ICrfDAO delegate;

    public CRFAdapter(ICrfDAO delegate) {
        this.delegate = delegate;
    }

    public CRFBean findByPK(int id) {
        return (CRFBean) delegate.findByPK(id);
    }

    public CRFBean findByVersionId(int crfVersionId) {
        return (CRFBean) delegate.findByVersionId(crfVersionId);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<CRFBean> findAllActiveByDefinition(StudyEventDefinitionBean sed) {
        return (ArrayList<CRFBean>) delegate.findAllActiveByDefinition(sed);
    }

    public ICrfDAO getDelegate() {
        return delegate;
    }
}
