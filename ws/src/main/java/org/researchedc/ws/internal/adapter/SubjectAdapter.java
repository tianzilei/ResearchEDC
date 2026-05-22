package org.researchedc.ws.internal.adapter;

import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.dao.submit.SubjectDAO;
import org.springframework.stereotype.Repository;

@Repository
public class SubjectAdapter {

    private final SubjectDAO delegate;

    public SubjectAdapter(SubjectDAO delegate) {
        this.delegate = delegate;
    }

    public SubjectBean findByPK(int id) {
        return (SubjectBean) delegate.findByPK(id);
    }

    public SubjectBean findByUniqueIdentifierAndAnyStudy(String uniqueIdentifier, int studyId) {
        return delegate.findByUniqueIdentifierAndAnyStudy(uniqueIdentifier, studyId);
    }

    public SubjectDAO getDelegate() {
        return delegate;
    }
}
