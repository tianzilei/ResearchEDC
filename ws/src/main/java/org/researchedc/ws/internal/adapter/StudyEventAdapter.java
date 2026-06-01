package org.researchedc.ws.internal.adapter;

import java.util.ArrayList;

import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.springframework.stereotype.Repository;

@Repository
public class StudyEventAdapter {

    private final IStudyEventDAO delegate;

    public StudyEventAdapter(IStudyEventDAO delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<StudyEventBean> findAllByStudySubject(StudySubjectBean studySubject) {
        return (ArrayList<StudyEventBean>) delegate.findAllByStudySubject(studySubject);
    }

    public StudyEventBean findByStudySubjectIdAndDefinitionIdAndOrdinal(int studySubjectId, int studyEventDefinitionId, int ordinal) {
        return (StudyEventBean) delegate.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectId, studyEventDefinitionId, ordinal);
    }

    public StudyEventBean findByPK(int id) {
        return (StudyEventBean) delegate.findByPK(id);
    }

    public StudyEventBean update(StudyEventBean bean, boolean inTransaction) {
        return (StudyEventBean) delegate.update(bean, inTransaction);
    }

    public IStudyEventDAO getDelegate() {
        return delegate;
    }
}
