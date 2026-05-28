package org.researchedc.ws.internal.adapter;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.springframework.stereotype.Repository;

@Repository
public class StudySubjectAdapter {

    private final IStudySubjectDAO delegate;

    public StudySubjectAdapter(IStudySubjectDAO delegate) {
        this.delegate = delegate;
    }

    public StudySubjectBean findByPK(int id) {
        return (StudySubjectBean) delegate.findByPK(id);
    }

    public StudySubjectBean findByLabelAndStudy(String label, StudyBean study) {
        return delegate.findByLabelAndStudy(label, study);
    }
}
