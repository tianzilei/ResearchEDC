package org.researchedc.module.dataimport.service;

import java.util.List;

public interface ImportItemPort {

    List<ImportItem> findImportItemsByOid(String oid);

    record ImportItem(Integer id, String oid, Integer dataTypeId) {
    }
}
