package org.researchedc.ws.internal.adapter;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.managestudy.StudyDAO;
import org.springframework.stereotype.Repository;

@Repository
public class StudyAdapter {

    private final StudyDAO delegate;

    public StudyAdapter(StudyDAO delegate) {
        this.delegate = delegate;
    }

    public StudyBean findByPK(int id) {
        return (StudyBean) delegate.findByPK(id);
    }
}
