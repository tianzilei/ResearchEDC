package org.researchedc.module.dataimport.service;

import org.researchedc.module.dataimport.dto.ImportStudy;

public interface ImportStudyLookupPort {

    ImportStudy findImportStudyByOid(String oid);
}
