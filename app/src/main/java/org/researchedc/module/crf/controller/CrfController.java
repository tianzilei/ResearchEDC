package org.researchedc.module.crf.controller;

import java.util.List;
import java.util.Map;
import org.researchedc.module.crf.dto.CrfSummaryDTO;
import org.researchedc.app.dto.CrfVersionDTO;
import org.researchedc.module.crf.dto.ItemDTO;
import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.service.CrfService;
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

    @GetMapping("/sections/{sectionId}/scd-rules")
    public ResponseEntity<List<Map<String, Object>>> getScdRules(@PathVariable int sectionId) {
        return ResponseEntity.ok(crfService.getScdRulesBySection(sectionId));
    }

    // ── Phase G: Write endpoints ──────────────────────────────────

    @PostMapping
    public ResponseEntity<CrfEntity> createCrf(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.get("description");
        Integer ownerId = body.get("ownerId") != null ? Integer.parseInt(body.get("ownerId")) : 0;
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(crfService.createCrf(name, description, ownerId));
    }

    @PostMapping("/{crfId}/versions")
    public ResponseEntity<CrfVersionDTO> createVersion(
            @PathVariable Integer crfId,
            @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.get("description");
        String revisionNotes = body.get("revisionNotes");
        Integer ownerId = body.get("ownerId") != null ? Integer.parseInt(body.get("ownerId")) : 0;
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        CrfVersionEntity entity = crfService.createVersion(crfId, name, description, revisionNotes, ownerId);
        return ResponseEntity.ok(crfService.getVersion(entity.getCrfVersionId()));
    }

    @GetMapping("/{crfId}/versions")
    public ResponseEntity<List<CrfVersionEntity>> listVersions(@PathVariable Integer crfId) {
        return ResponseEntity.ok(crfService.listVersionEntities(crfId));
    }

    @PatchMapping("/versions/{crfVersionId}/status")
    public ResponseEntity<Void> updateVersionStatus(
            @PathVariable Integer crfVersionId,
            @RequestBody Map<String, Integer> body) {
        Integer statusId = body.get("statusId");
        if (statusId == null) return ResponseEntity.badRequest().build();
        crfService.updateVersionStatus(crfVersionId, statusId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/versions/{crfVersionId}")
    public ResponseEntity<Void> deleteVersion(@PathVariable Integer crfVersionId) {
        crfService.deleteVersion(crfVersionId);
        return ResponseEntity.noContent().build();
    }
}
