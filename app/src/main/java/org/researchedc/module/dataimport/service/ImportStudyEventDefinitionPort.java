package org.researchedc.module.dataimport.service;

public interface ImportStudyEventDefinitionPort {

    Object[] findImportStudyEventDefinitionByOidAndStudy(
            String oid, int studyId, int parentStudyId);
}
