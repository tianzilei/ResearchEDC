package org.researchedc.module.crf.controller;

import java.util.List;

import org.researchedc.module.crf.dto.CreateCrfRequest;
import org.researchedc.module.crf.dto.CreateCrfVersionRequest;
import org.researchedc.module.crf.dto.CrfManageDTO;
import org.researchedc.module.crf.dto.CrfVersionManageDTO;
import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.service.CrfService;
import org.researchedc.config.CurrentUserUtils;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/crfs/manage")
public class CrfManageController {

    private final CrfService crfService;
    private final CurrentUserUtils currentUserUtils;

    public CrfManageController(CrfService crfService, CurrentUserUtils currentUserUtils) {
        this.crfService = crfService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping
    public ResponseEntity<List<CrfManageDTO>> listCrfs() {
        return ResponseEntity.ok(crfService.getAllCrfEntities()
                .stream().map(this::toCrfDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrfManageDTO> getCrf(@PathVariable int id) {
        try {
            return ResponseEntity.ok(toCrfDto(crfService.getCrfEntity(id)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CrfManageDTO> createCrf(@RequestBody CreateCrfRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        CrfEntity entity = crfService.createCrf(request.getName(), request.getDescription(), ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCrfDto(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrfManageDTO> updateCrf(
            @PathVariable int id, @RequestBody CreateCrfRequest request) {
        try {
            return ResponseEntity.ok(toCrfDto(crfService.updateCrf(id, request.getName(), request.getDescription())));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<CrfVersionManageDTO>> listVersions(@PathVariable int id) {
        return ResponseEntity.ok(crfService.listVersionEntities(id)
                .stream().map(this::toVersionDto).toList());
    }

    @GetMapping("/versions/{versionId}")
    public ResponseEntity<CrfVersionManageDTO> getVersion(@PathVariable int versionId) {
        try {
            return ResponseEntity.ok(toVersionDto(crfService.getCrfVersionEntity(versionId)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{crfId}/versions")
    public ResponseEntity<CrfVersionManageDTO> createVersion(
            @PathVariable int crfId, @RequestBody CreateCrfVersionRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        CrfVersionEntity entity = crfService.createVersion(
                crfId, request.getName(), request.getDescription(),
                request.getRevisionNotes(), ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toVersionDto(entity));
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
        dto.setStatusId(entity.getStatusId());
        dto.setDateCreated(entity.getDateCreated());
        return dto;
    }

    private CrfVersionManageDTO toVersionDto(CrfVersionEntity entity) {
        CrfVersionManageDTO dto = new CrfVersionManageDTO();
        dto.setCrfVersionId(entity.getCrfVersionId());
        dto.setCrfId(entity.getCrfId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setRevisionNotes(entity.getRevisionNotes());
        dto.setStatusId(entity.getStatusId());
        dto.setDateCreated(entity.getDateCreated());
        return dto;
    }
}
