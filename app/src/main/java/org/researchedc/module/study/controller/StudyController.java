package org.researchedc.module.study.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.researchedc.module.study.dto.CreateStudyRequest;
import org.researchedc.module.study.dto.FeatureFlagsDTO;
import org.researchedc.module.study.dto.StudyDetailDTO;
import org.researchedc.module.study.dto.StudySummaryDTO;
import org.researchedc.module.study.dto.UpdateStudyRequest;
import org.researchedc.module.study.service.StudyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/studies")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @GetMapping
    public ResponseEntity<List<StudySummaryDTO>> listStudies() {
        return ResponseEntity.ok(studyService.listStudies());
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudySummaryDTO>> searchStudies(
            @RequestParam String name) {
        return ResponseEntity.ok(studyService.searchByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyDetailDTO> getStudy(@PathVariable Integer id) {
        return ResponseEntity.ok(studyService.getStudy(id));
    }

    @GetMapping("/{id}/sites")
    public ResponseEntity<List<StudySummaryDTO>> listSites(@PathVariable Integer id) {
        return ResponseEntity.ok(studyService.listSites(id));
    }

    @PostMapping
    public ResponseEntity<StudyDetailDTO> createStudy(
            @Valid @RequestBody CreateStudyRequest request) {
        // TODO: extract ownerId from security context / JWT token
        Integer ownerId = 1;
        StudyDetailDTO dto = studyService.createStudy(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyDetailDTO> updateStudy(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateStudyRequest request) {
        // TODO: extract userId from security context / JWT token
        Integer userId = 1;
        StudyDetailDTO dto = studyService.updateStudy(id, request, userId);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudy(@PathVariable Integer id) {
        // TODO: extract userId from security context / JWT token
        Integer userId = 1;
        studyService.deleteStudy(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStudyStatus(
            @PathVariable Integer id,
            @RequestParam Integer statusId) {
        // TODO: extract userId from security context / JWT token
        Integer userId = 1;
        studyService.updateStudyStatus(id, statusId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/feature-flags")
    public ResponseEntity<FeatureFlagsDTO> getFeatureFlags(@PathVariable Integer id) {
        String json = studyService.getFeatureFlags(id);
        com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Boolean>> typeRef =
            new com.fasterxml.jackson.core.type.TypeReference<>() {};
        try {
            java.util.Map<String, Boolean> flags =
                new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, typeRef);
            return ResponseEntity.ok(new FeatureFlagsDTO(flags));
        } catch (Exception e) {
            return ResponseEntity.ok(new FeatureFlagsDTO(java.util.Collections.emptyMap()));
        }
    }

    @PutMapping("/{id}/feature-flags")
    public ResponseEntity<Void> updateFeatureFlags(
            @PathVariable Integer id,
            @RequestBody FeatureFlagsDTO featureFlags) {
        Integer userId = 1;
        String flagsJson = "{}";
        if (featureFlags.getFlags() != null) {
            try {
                flagsJson = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(featureFlags.getFlags());
            } catch (Exception e) {
                flagsJson = "{}";
            }
        }
        studyService.updateFeatureFlags(id, flagsJson, userId);
        return ResponseEntity.ok().build();
    }
}
