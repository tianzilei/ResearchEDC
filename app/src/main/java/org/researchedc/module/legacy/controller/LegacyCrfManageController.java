package org.researchedc.module.legacy.controller;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.service.CrfService;
import org.researchedc.module.legacy.dto.CreateCrfRequest;
import org.researchedc.module.legacy.dto.CrfManageDTO;
import org.researchedc.module.legacy.dto.CrfVersionManageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/crfs")
public class LegacyCrfManageController {

    private final CrfService crfService;

    public LegacyCrfManageController(CrfService crfService) {
        this.crfService = crfService;
    }

    @GetMapping
    public ResponseEntity<List<CrfManageDTO>> listCrfs() {
        List<CrfManageDTO> result = new ArrayList<>();
        for (CrfEntity entity : crfService.getAllCrfEntities()) {
            result.add(toCrfDto(entity));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrfManageDTO> getCrf(@PathVariable int id) {
        try {
            CrfEntity entity = crfService.getCrfEntity(id);
            return ResponseEntity.ok(toCrfDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CrfManageDTO> createCrf(@RequestBody CreateCrfRequest request) {
        Integer ownerId = 1;
        CrfEntity entity = crfService.createCrf(request.getName(), request.getDescription(), ownerId);
        return ResponseEntity.ok(toCrfDto(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrfManageDTO> updateCrf(@PathVariable int id,
                                                   @RequestBody CreateCrfRequest request) {
        try {
            CrfEntity entity = crfService.updateCrf(id, request.getName(), request.getDescription());
            return ResponseEntity.ok(toCrfDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<CrfVersionManageDTO>> listVersions(@PathVariable int id) {
        List<CrfVersionManageDTO> result = new ArrayList<>();
        for (CrfVersionEntity entity : crfService.listVersionEntities(id)) {
            result.add(toVersionDto(entity));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/versions/{versionId}")
    public ResponseEntity<CrfVersionManageDTO> getVersion(@PathVariable int versionId) {
        try {
            CrfVersionEntity entity = crfService.getCrfVersionEntity(versionId);
            return ResponseEntity.ok(toVersionDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{crfId}/versions")
    public ResponseEntity<CrfVersionManageDTO> createVersion(@PathVariable int crfId,
            @RequestBody CrfVersionManageDTO request) {
        Integer ownerId = 1;
        CrfVersionEntity entity = crfService.createVersion(
                crfId, request.getName(), request.getDescription(),
                request.getRevisionNotes(), ownerId);
        return ResponseEntity.ok(toVersionDto(entity));
    }

    @DeleteMapping("/versions/{versionId}")
    public ResponseEntity<Void> deleteVersion(@PathVariable int versionId) {
        try {
            crfService.deleteVersion(versionId);
            return ResponseEntity.noContent().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private CrfManageDTO toCrfDto(CrfEntity entity) {
        CrfManageDTO dto = new CrfManageDTO();
        dto.setCrfId(entity.getCrfId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setOcOid(entity.getOcOid());
        dto.setStatus(String.valueOf(entity.getStatusId()));
        if (entity.getDateCreated() != null) {
            dto.setDateCreated(Date.from(
                    entity.getDateCreated().atZone(ZoneId.systemDefault()).toInstant()));
        }
        return dto;
    }

    private CrfVersionManageDTO toVersionDto(CrfVersionEntity entity) {
        CrfVersionManageDTO dto = new CrfVersionManageDTO();
        dto.setCrfVersionId(entity.getCrfVersionId());
        dto.setCrfId(entity.getCrfId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setRevisionNotes(entity.getRevisionNotes());
        dto.setStatus(String.valueOf(entity.getStatusId()));
        if (entity.getDateCreated() != null) {
            dto.setDateCreated(Date.from(
                    entity.getDateCreated().atZone(ZoneId.systemDefault()).toInstant()));
        }
        return dto;
    }
}
