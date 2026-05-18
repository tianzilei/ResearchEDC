package org.akaza.openclinica.module.study.controller;

import java.util.List;
import org.akaza.openclinica.module.study.dto.StudyDetailDTO;
import org.akaza.openclinica.module.study.dto.StudySummaryDTO;
import org.akaza.openclinica.module.study.service.StudyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
