package org.researchedc.module.dataimport.service;

import java.util.List;

import org.researchedc.module.dataimport.dto.ImportItemFormMetadata;

public interface ImportItemFormMetadataPort {

    List<ImportItemFormMetadata> findImportItemFormMetadataByItemId(int itemId);
}
