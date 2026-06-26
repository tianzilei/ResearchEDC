package org.researchedc.module.dataimport.service;

import org.researchedc.module.dataimport.dto.ImportStudySubject;

public interface ImportStudySubjectPort {

    ImportStudySubject findImportStudySubjectByOidAndStudy(String oid, int studyId);
}
