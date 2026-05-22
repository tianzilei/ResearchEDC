package org.researchedc.ws.internal.adapter;

import java.util.List;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.springframework.stereotype.Repository;

@Repository
public class StudyEventDefinitionAdapter {

    private final StudyEventDefinitionDAO delegate;

    public StudyEventDefinitionAdapter(StudyEventDefinitionDAO delegate) {
        this.delegate = delegate;
    }

    public StudyEventDefinitionBean findByPK(int id) {
        return (StudyEventDefinitionBean) delegate.findByPK(id);
    }

    public StudyEventDefinitionBean findByOidAndStudy(String oid, int studyId, int parentStudyId) {
        return delegate.findByOidAndStudy(oid, studyId, parentStudyId);
    }

    @SuppressWarnings("unchecked")
    public List<StudyEventDefinitionBean> findAllByStudy(StudyBean study) {
        return (List<StudyEventDefinitionBean>) delegate.findAllByStudy(study);
    }

    public StudyEventDefinitionDAO getDelegate() {
        return delegate;
    }
}
