package org.researchedc.module.discrepancynote.controller;

import java.util.List;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.module.discrepancynote.dto.CreateDiscrepancyNoteRequest;
import org.researchedc.module.discrepancynote.dto.DiscrepancyNoteDTO;
import org.researchedc.module.discrepancynote.entity.DiscrepancyNoteEntity;
import org.researchedc.module.discrepancynote.service.DiscrepancyNoteService;
import org.researchedc.config.CurrentUserUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/discrepancy-notes")
public class DiscrepancyNoteController {

    private final DiscrepancyNoteService discrepancyNoteService;
    private final CurrentUserUtils currentUserUtils;

    public DiscrepancyNoteController(DiscrepancyNoteService discrepancyNoteService, CurrentUserUtils currentUserUtils) {
        this.discrepancyNoteService = discrepancyNoteService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<DiscrepancyNoteDTO>> listNotes(
            @RequestParam(required = false) Integer eventCrfId,
            @RequestParam(required = false) Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        List<DiscrepancyNoteEntity> entities;
        if (eventCrfId != null) {
            entities = discrepancyNoteService.listByEventCrf(eventCrfId, currentUserId);
        } else if (studyId != null) {
            entities = discrepancyNoteService.listByStudy(studyId, currentUserId);
        } else {
            entities = List.of();
        }
        return ResponseEntity.ok(entities.stream().map(this::toDto).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<DiscrepancyNoteDTO> getNote(@PathVariable int id) {
        try {
            Integer currentUserId = currentUserUtils.getCurrentUserId();
            return ResponseEntity.ok(toDto(discrepancyNoteService.getById(id, currentUserId)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<DiscrepancyNoteDTO> createNote(@RequestBody CreateDiscrepancyNoteRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        DiscrepancyNoteEntity entity = discrepancyNoteService.create(
                request.getDescription(), 1, 1,
                request.getDetailedNotes(), ownerId, null,
                request.getEntityType(), request.getEntityId(),
                request.getStudyId(), null, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(entity));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<DiscrepancyNoteDTO> resolveNote(@PathVariable int id) {
        try {
            Integer currentUserId = currentUserUtils.getCurrentUserId();
            return ResponseEntity.ok(toDto(discrepancyNoteService.resolveNote(id, currentUserId)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private DiscrepancyNoteDTO toDto(DiscrepancyNoteEntity entity) {
        DiscrepancyNoteDTO dto = new DiscrepancyNoteDTO();
        dto.setDiscrepancyNoteId(entity.getDiscrepancyNoteId());
        dto.setDescription(entity.getDescription());
        dto.setDetailedNotes(entity.getDetailedNotes());
        dto.setEntityType(entity.getEntityType());
        dto.setEntityId(entity.getEntityId());
        dto.setStudyId(entity.getStudyId());
        dto.setOwnerId(entity.getOwnerId());
        dto.setParentDnId(entity.getParentDnId());
        dto.setDateCreated(entity.getDateCreated());
        dto.setResolutionStatusId(entity.getResolutionStatusId());
        dto.setDiscrepancyNoteTypeId(entity.getDiscrepancyNoteTypeId());
        return dto;
    }
}
