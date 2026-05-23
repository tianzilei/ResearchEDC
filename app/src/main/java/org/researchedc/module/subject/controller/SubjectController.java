package org.researchedc.module.subject.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.researchedc.module.subject.dto.CreateSubjectRequest;
import org.researchedc.module.subject.dto.EnrollSubjectRequest;
import org.researchedc.module.subject.dto.StudySubjectDTO;
import org.researchedc.module.subject.dto.SubjectDTO;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.subject.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final CurrentUserUtils currentUserUtils;

    public SubjectController(SubjectService subjectService, CurrentUserUtils currentUserUtils) {
        this.subjectService = subjectService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/search")
    public ResponseEntity<List<SubjectDTO>> searchSubjects(
            @RequestParam String query) {
        return ResponseEntity.ok(subjectService.searchSubjects(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> getSubject(@PathVariable Integer id) {
        return ResponseEntity.ok(subjectService.getSubject(id));
    }

    @GetMapping("/by-study")
    public ResponseEntity<List<StudySubjectDTO>> listStudySubjects(
            @RequestParam Integer studyId) {
        return ResponseEntity.ok(subjectService.listStudySubjects(studyId));
    }

    @GetMapping("/enrollment/{id}")
    public ResponseEntity<StudySubjectDTO> getStudySubject(
            @PathVariable Integer id) {
        return ResponseEntity.ok(subjectService.getStudySubject(id));
    }

    @PostMapping
    public ResponseEntity<SubjectDTO> createSubject(
            @Valid @RequestBody CreateSubjectRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        SubjectDTO dto = subjectService.createSubject(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/enroll")
    public ResponseEntity<StudySubjectDTO> enrollSubject(
            @Valid @RequestBody EnrollSubjectRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        StudySubjectDTO dto = subjectService.enrollSubject(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
