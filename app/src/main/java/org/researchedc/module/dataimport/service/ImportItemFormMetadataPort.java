package org.researchedc.module.dataimport.service;

import java.util.List;

public interface ImportItemFormMetadataPort {

    List<Object[]> findImportItemFormMetadataByItemId(int itemId);
}
