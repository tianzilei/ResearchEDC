package org.researchedc.module.legacy.controller;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.researchedc.module.legacy.dto.SubjectDTO;
import org.researchedc.module.subject.dto.StudySubjectDTO;
import org.researchedc.module.subject.service.SubjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/subjects")
public class LegacySubjectController {

    private final SubjectService subjectService;

    public LegacySubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<List<SubjectDTO>> listSubjects(@RequestParam int studyId) {
        List<SubjectDTO> result = new ArrayList<>();
        for (StudySubjectDTO ss : subjectService.listStudySubjects(studyId)) {
            result.add(toDto(ss));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> getSubject(@PathVariable int id) {
        try {
            StudySubjectDTO ss = subjectService.getStudySubject(id);
            return ResponseEntity.ok(toDto(ss));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-label")
    public ResponseEntity<List<SubjectDTO>> searchByLabel(
            @RequestParam int studyId, @RequestParam String label) {
        List<SubjectDTO> result = new ArrayList<>();
        String lowerQuery = label.toLowerCase();
        for (StudySubjectDTO ss : subjectService.listStudySubjects(studyId)) {
            if (ss.getLabel() != null
                    && ss.getLabel().toLowerCase().contains(lowerQuery)) {
                result.add(toDto(ss));
            }
        }
        return ResponseEntity.ok(result);
    }

    private static SubjectDTO toDto(StudySubjectDTO src) {
        SubjectDTO dto = new SubjectDTO();
        dto.setStudySubjectId(src.getStudySubjectId());
        dto.setStudyId(src.getStudyId());
        dto.setLabel(src.getLabel());
        dto.setSecondaryLabel(src.getSecondaryLabel());
        dto.setUniqueIdentifier(src.getSubjectUniqueIdentifier());
        if (src.getEnrollmentDate() != null) {
            dto.setEnrollmentDate(Date.from(
                    src.getEnrollmentDate().atZone(ZoneId.systemDefault()).toInstant()));
        }
        dto.setStatus(null);
        return dto;
    }
}
