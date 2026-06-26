package org.researchedc.module.dataimport.service;

import java.util.List;

public interface ImportResponseSetPort {

    List<ImportResponseSet> findAllByItemId(int itemId);

    record ImportResponseSet(Integer responseTypeId, String optionsText, String optionsValues) {
    }
}
