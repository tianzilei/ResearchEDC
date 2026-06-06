package org.researchedc.dao.spi;

import org.researchedc.domain.managestudy.StudyModuleStatus;

public interface StudyModuleStatusDao {

    StudyModuleStatus findByStudyId(int studyId);

    default StudyModuleStatus saveOrUpdate(StudyModuleStatus studyModuleStatus) {
        throw new UnsupportedOperationException();
    }

}
