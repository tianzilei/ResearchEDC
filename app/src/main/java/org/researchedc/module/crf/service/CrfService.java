package org.researchedc.module.crf.service;

import java.util.List;
import org.researchedc.module.crf.dto.CrfSummaryDTO;
import org.researchedc.module.crf.dto.CrfVersionDTO;
import org.researchedc.module.crf.dto.ItemDTO;
import org.researchedc.module.crf.internal.adapter.LegacyCrfAdapter;
import org.springframework.stereotype.Service;

@Service
public class CrfService {

    private final LegacyCrfAdapter legacyCrfAdapter;

    public CrfService(LegacyCrfAdapter legacyCrfAdapter) {
        this.legacyCrfAdapter = legacyCrfAdapter;
    }

    public List<CrfSummaryDTO> listCrfs() {
        return legacyCrfAdapter.findAllCrfs();
    }

    public CrfVersionDTO getVersion(int crfVersionId) {
        return legacyCrfAdapter.findVersionById(crfVersionId);
    }

    public List<ItemDTO> getItemsBySection(int sectionId, int crfVersionId) {
        return legacyCrfAdapter.findItemsBySectionAndVersion(sectionId, crfVersionId);
    }
}
