package org.researchedc.ws.internal.adapter;

import java.util.List;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.springframework.stereotype.Repository;

@Repository
public class StudyEventDefinitionAdapter {

    private final IStudyEventDefinitionDAO delegate;

    public StudyEventDefinitionAdapter(IStudyEventDefinitionDAO delegate) {
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

    public IStudyEventDefinitionDAO getDelegate() {
        return delegate;
    }
}
