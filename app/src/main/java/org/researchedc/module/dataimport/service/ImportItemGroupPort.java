package org.researchedc.module.dataimport.service;

import java.util.List;

import org.researchedc.module.dataimport.dto.ImportItemGroup;

public interface ImportItemGroupPort {

    List<ImportItemGroup> findImportItemGroupsByOid(String oid);
}
