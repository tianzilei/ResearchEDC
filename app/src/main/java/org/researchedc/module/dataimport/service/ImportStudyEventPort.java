package org.researchedc.module.dataimport.service;

public interface ImportStudyEventPort {

    Object[] findImportStudyEventBySubjectDefinitionOrdinal(
            int studySubjectId, int studyEventDefinitionId, int ordinal);
}
