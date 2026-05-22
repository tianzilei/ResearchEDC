package org.researchedc.ws.internal.adapter;

import java.util.ArrayList;

import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.springframework.stereotype.Repository;

@Repository
public class StudyEventAdapter {

    private final StudyEventDAO delegate;

    public StudyEventAdapter(StudyEventDAO delegate) {
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

    public StudyEventDAO getDelegate() {
        return delegate;
    }
}
