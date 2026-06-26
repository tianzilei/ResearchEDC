package org.researchedc.module.dataimport.service;

public interface ImportItemDataPort {

    void upsertImportItemData(
            int itemId,
            int eventCrfId,
            int ordinal,
            int ownerId,
            int statusId,
            String value);
}
