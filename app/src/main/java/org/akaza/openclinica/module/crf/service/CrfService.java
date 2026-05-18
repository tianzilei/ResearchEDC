package org.akaza.openclinica.module.crf.service;

import java.util.List;
import org.akaza.openclinica.module.crf.dto.CrfSummaryDTO;
import org.akaza.openclinica.module.crf.dto.CrfVersionDTO;
import org.akaza.openclinica.module.crf.dto.ItemDTO;
import org.akaza.openclinica.module.crf.internal.adapter.LegacyCrfAdapter;
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
