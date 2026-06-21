package org.researchedc.module.dataimport.service;

import org.researchedc.module.dataimport.dto.ImportStudyEvent;

public interface ImportStudyEventPort {

    ImportStudyEvent findImportStudyEventBySubjectDefinitionOrdinal(
            int studySubjectId, int studyEventDefinitionId, int ordinal);
}
