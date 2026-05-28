package org.researchedc.ws.internal.adapter;

import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.dao.spi.ISubjectDAO;
import org.springframework.stereotype.Repository;

@Repository
public class SubjectAdapter {

    private final ISubjectDAO delegate;

    public SubjectAdapter(ISubjectDAO delegate) {
        this.delegate = delegate;
    }

    public SubjectBean findByPK(int id) {
        return (SubjectBean) delegate.findByPK(id);
    }

    public SubjectBean findByUniqueIdentifierAndAnyStudy(String uniqueIdentifier, int studyId) {
        return delegate.findByUniqueIdentifierAndAnyStudy(uniqueIdentifier, studyId);
    }

    public ISubjectDAO getDelegate() {
        return delegate;
    }
}
