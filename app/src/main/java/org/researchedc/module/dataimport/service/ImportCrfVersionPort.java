package org.researchedc.module.dataimport.service;

import java.util.List;

public interface ImportCrfVersionPort {

    List<ImportCrfVersion> findAllImportCrfVersionsByOid(String oid);

    record ImportCrfVersion(Integer id) {
    }
}
