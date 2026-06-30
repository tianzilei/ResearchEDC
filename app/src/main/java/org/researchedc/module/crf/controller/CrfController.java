package org.researchedc.module.crf.controller;

import java.util.List;
import java.util.Map;
import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.crf.dto.CrfSummaryDTO;
import org.researchedc.app.dto.CrfVersionDTO;
import org.researchedc.module.crf.dto.ItemDTO;
import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.service.CrfService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crfs")
public class CrfController {

    private final CrfService crfService;
    private final CurrentUserUtils currentUserUtils;

    public CrfController(CrfService crfService, CurrentUserUtils currentUserUtils) {
        this.crfService = crfService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<CrfSummaryDTO>> listCrfs() {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(crfService.listCrfs(currentUserId));
    }

    @GetMapping("/versions/{crfVersionId}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<CrfVersionDTO> getVersion(@PathVariable int crfVersionId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        CrfVersionDTO dto = crfService.getVersion(crfVersionId, currentUserId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/versions/{crfVersionId}/sections/{sectionId}/items")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<ItemDTO>> getItemsBySection(
            @PathVariable int crfVersionId,
            @PathVariable int sectionId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(crfService.getItemsBySection(sectionId, crfVersionId, currentUserId));
    }

    @GetMapping("/sections/{sectionId}/scd-rules")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<Map<String, Object>>> getScdRules(@PathVariable int sectionId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(crfService.getScdRulesBySection(sectionId, currentUserId));
    }

    // ── Phase G: Write endpoints ──────────────────────────────────

    @PostMapping
    @PreAuthorize(CoreEdcAuthorityExpressions.ADMINISTER_STUDIES)
    public ResponseEntity<CrfEntity> createCrf(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.get("description");
        Integer ownerId = currentUserUtils.getCurrentUserId();
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(crfService.createCrf(name, description, ownerId));
    }

    @PostMapping("/{crfId}/versions")
    @PreAuthorize(CoreEdcAuthorityExpressions.ADMINISTER_STUDIES)
    public ResponseEntity<CrfVersionDTO> createVersion(
            @PathVariable Integer crfId,
            @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.get("description");
        String revisionNotes = body.get("revisionNotes");
        Integer ownerId = currentUserUtils.getCurrentUserId();
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        CrfVersionEntity entity = crfService.createVersion(crfId, name, description, revisionNotes, ownerId);
        return ResponseEntity.ok(crfService.getVersion(entity.getCrfVersionId(), ownerId));
    }

    @GetMapping("/{crfId}/versions")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<CrfVersionEntity>> listVersions(@PathVariable Integer crfId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(crfService.listVersionEntities(crfId, currentUserId));
    }

    @PatchMapping("/versions/{crfVersionId}/status")
    @PreAuthorize(CoreEdcAuthorityExpressions.ADMINISTER_STUDIES)
    public ResponseEntity<Void> updateVersionStatus(
            @PathVariable Integer crfVersionId,
            @RequestBody Map<String, Integer> body) {
        Integer statusId = body.get("statusId");
        if (statusId == null) return ResponseEntity.badRequest().build();
        crfService.updateVersionStatus(crfVersionId, statusId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/versions/{crfVersionId}")
    @PreAuthorize(CoreEdcAuthorityExpressions.ADMINISTER_STUDIES)
    public ResponseEntity<Void> deleteVersion(@PathVariable Integer crfVersionId) {
        crfService.deleteVersion(crfVersionId);
        return ResponseEntity.noContent().build();
    }
}
