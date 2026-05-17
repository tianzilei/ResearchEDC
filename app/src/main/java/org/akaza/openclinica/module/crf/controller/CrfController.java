package org.akaza.openclinica.module.crf.controller;

import java.util.List;
import org.akaza.openclinica.module.crf.dto.CrfSummaryDTO;
import org.akaza.openclinica.module.crf.dto.CrfVersionDTO;
import org.akaza.openclinica.module.crf.dto.ItemDTO;
import org.akaza.openclinica.module.crf.service.CrfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crfs")
public class CrfController {

    private final CrfService crfService;

    public CrfController(CrfService crfService) {
        this.crfService = crfService;
    }

    @GetMapping
    public ResponseEntity<List<CrfSummaryDTO>> listCrfs() {
        return ResponseEntity.ok(crfService.listCrfs());
    }

    @GetMapping("/versions/{crfVersionId}")
    public ResponseEntity<CrfVersionDTO> getVersion(@PathVariable int crfVersionId) {
        CrfVersionDTO dto = crfService.getVersion(crfVersionId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/versions/{crfVersionId}/sections/{sectionId}/items")
    public ResponseEntity<List<ItemDTO>> getItemsBySection(
            @PathVariable int crfVersionId,
            @PathVariable int sectionId) {
        return ResponseEntity.ok(crfService.getItemsBySection(sectionId, crfVersionId));
    }
}
