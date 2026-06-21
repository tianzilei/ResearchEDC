package org.researchedc.module.dataimport.service;

import org.researchedc.module.dataimport.dto.ImportStudyEventDefinition;

public interface ImportStudyEventDefinitionPort {

    ImportStudyEventDefinition findImportStudyEventDefinitionByOidAndStudy(
            String oid, int studyId, int parentStudyId);
}
