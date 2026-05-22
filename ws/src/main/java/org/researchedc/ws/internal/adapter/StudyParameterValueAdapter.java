package org.researchedc.ws.internal.adapter;

import java.util.ArrayList;

import org.researchedc.bean.service.StudyParameterValueBean;
import org.researchedc.dao.spi.IStudyParameterValueDAO;

public class StudyParameterValueAdapter {

    private final IStudyParameterValueDAO delegate;

    public StudyParameterValueAdapter(IStudyParameterValueDAO delegate) {
        this.delegate = delegate;
    }

    public StudyParameterValueBean findByHandleAndStudy(int studyId, String handle) {
        return delegate.findByHandleAndStudy(studyId, handle);
    }

    @SuppressWarnings("unchecked")
    public ArrayList findParamConfigByStudy(org.researchedc.bean.managestudy.StudyBean study) {
        return delegate.findParamConfigByStudy(study);
    }

    public IStudyParameterValueDAO getDelegate() {
        return delegate;
    }
}
