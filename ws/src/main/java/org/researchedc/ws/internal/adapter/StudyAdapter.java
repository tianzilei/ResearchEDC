package org.researchedc.ws.internal.adapter;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.spi.IStudyDAO;
import org.springframework.stereotype.Repository;

@Repository
public class StudyAdapter {

    private final IStudyDAO delegate;

    public StudyAdapter(IStudyDAO delegate) {
        this.delegate = delegate;
    }

    public StudyBean findByPK(int id) {
        return (StudyBean) delegate.findByPK(id);
    }
}
