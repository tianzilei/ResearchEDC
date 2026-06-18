package org.researchedc.module.dataimport.service;

public interface ImportStudySubjectPort {

    Object[] findImportStudySubjectByOidAndStudy(String oid, int studyId);
}
