package org.researchedc.module.legacy.controller;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.researchedc.module.discrepancynote.entity.DiscrepancyNoteEntity;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.discrepancynote.service.DiscrepancyNoteService;
import org.researchedc.module.legacy.dto.CreateDiscrepancyNoteRequest;
import org.researchedc.module.legacy.dto.DiscrepancyNoteDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/discrepancy-notes")
public class LegacyDiscrepancyNoteController {

    private final DiscrepancyNoteService discrepancyNoteService;
    private final CurrentUserUtils currentUserUtils;

    public LegacyDiscrepancyNoteController(DiscrepancyNoteService discrepancyNoteService, CurrentUserUtils currentUserUtils) {
        this.discrepancyNoteService = discrepancyNoteService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping
    public ResponseEntity<List<DiscrepancyNoteDTO>> listNotes(
            @RequestParam(required = false) Integer eventCrfId,
            @RequestParam(required = false) Integer studyId) {
        if (eventCrfId != null) {
            return listNotesByEventCrf(eventCrfId);
        }
        if (studyId != null) {
            return listNotesByStudy(studyId);
        }
        return ResponseEntity.ok(List.of());
    }

    private ResponseEntity<List<DiscrepancyNoteDTO>> listNotesByEventCrf(int eventCrfId) {
        List<DiscrepancyNoteDTO> result = new ArrayList<>();
        for (DiscrepancyNoteEntity entity : discrepancyNoteService.listByStudy(eventCrfId)) {
            result.add(toDto(entity));
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<List<DiscrepancyNoteDTO>> listNotesByStudy(int studyId) {
        List<DiscrepancyNoteDTO> result = new ArrayList<>();
        for (DiscrepancyNoteEntity entity : discrepancyNoteService.listByStudy(studyId)) {
            result.add(toDto(entity));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscrepancyNoteDTO> getNote(@PathVariable int id) {
        try {
            DiscrepancyNoteEntity entity = discrepancyNoteService.getById(id);
            return ResponseEntity.ok(toDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<DiscrepancyNoteDTO> createNote(
            @RequestBody CreateDiscrepancyNoteRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        DiscrepancyNoteEntity entity = discrepancyNoteService.create(
                request.getDescription(), 1, 1,
                request.getDetailedNotes(), ownerId, null,
                request.getEntityType(), request.getEntityId(),
                request.getStudyId(), null);
        return ResponseEntity.ok(toDto(entity));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<DiscrepancyNoteDTO> resolveNote(@PathVariable int id) {
        try {
            DiscrepancyNoteEntity entity = discrepancyNoteService.resolveNote(id);
            return ResponseEntity.ok(toDto(entity));
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
        dto.setColumn("value");
        dto.setEntityId(entity.getDiscrepancyNoteId());
        dto.setStudyId(entity.getStudyId());
        dto.setOwnerId(entity.getOwnerId());
        if (entity.getDateCreated() != null) {
            dto.setDateCreated(Date.from(
                    entity.getDateCreated().atZone(ZoneId.systemDefault()).toInstant()));
        }
        dto.setParentDnId(entity.getParentDnId() != null ? entity.getParentDnId() : 0);
        return dto;
    }
}
