package org.researchedc.module.legacy.controller;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.researchedc.module.legacy.dto.StudySummaryDTO;
import org.researchedc.module.study.dto.StudyDetailDTO;
import org.researchedc.module.study.service.StudyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/studies")
public class LegacyStudyController {

    private final StudyService studyService;

    public LegacyStudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @GetMapping
    public ResponseEntity<List<StudySummaryDTO>> listStudies() {
        List<StudySummaryDTO> result = new ArrayList<>();
        for (org.researchedc.module.study.dto.StudySummaryDTO s : studyService.listStudies()) {
            result.add(toDto(s));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudySummaryDTO> getStudy(@PathVariable int id) {
        try {
            StudyDetailDTO detail = studyService.getStudy(id);
            return ResponseEntity.ok(toDetailDto(detail));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudySummaryDTO>> searchStudies(
            @RequestParam String query) {
        List<StudySummaryDTO> result = new ArrayList<>();
        for (org.researchedc.module.study.dto.StudySummaryDTO s : studyService.searchByName(query)) {
            result.add(toDto(s));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/sites")
    public ResponseEntity<List<StudySummaryDTO>> listSites(@PathVariable int id) {
        List<StudySummaryDTO> result = new ArrayList<>();
        for (org.researchedc.module.study.dto.StudySummaryDTO s : studyService.listSites(id)) {
            result.add(toDto(s));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-oid")
    public ResponseEntity<StudySummaryDTO> findByOid(@RequestParam String oid) {
        for (org.researchedc.module.study.dto.StudySummaryDTO s : studyService.listStudies()) {
            if (oid.equals(s.getOcOid())) {
                return ResponseEntity.ok(toDto(s));
            }
        }
        return ResponseEntity.notFound().build();
    }

    private static StudySummaryDTO toDto(org.researchedc.module.study.dto.StudySummaryDTO src) {
        StudySummaryDTO dto = new StudySummaryDTO();
        dto.setStudyId(src.getStudyId());
        dto.setName(src.getName());
        dto.setIdentifier(src.getUniqueIdentifier());
        dto.setOid(src.getOcOid());
        dto.setType(null);
        dto.setStatus(src.getStatus());
        dto.setPrincipalInvestigator(src.getPrincipalInvestigator());
        if (src.getDateCreated() != null) {
            dto.setDateCreated(Date.from(
                    src.getDateCreated().atZone(ZoneId.systemDefault()).toInstant()));
        }
        return dto;
    }

    private static StudySummaryDTO toDetailDto(StudyDetailDTO src) {
        StudySummaryDTO dto = new StudySummaryDTO();
        dto.setStudyId(src.getStudyId());
        dto.setName(src.getName());
        dto.setIdentifier(src.getUniqueIdentifier());
        dto.setOid(src.getOcOid());
        dto.setType(null);
        dto.setStatus(src.getStatus());
        dto.setPrincipalInvestigator(src.getPrincipalInvestigator());
        if (src.getDateCreated() != null) {
            dto.setDateCreated(Date.from(
                    src.getDateCreated().atZone(ZoneId.systemDefault()).toInstant()));
        }
        return dto;
    }
}
