package org.researchedc.module.subject.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.module.subject.dto.CreateSubjectRequest;
import org.researchedc.module.subject.dto.EnrollSubjectRequest;
import org.researchedc.module.subject.dto.ReassignStudySubjectRequest;
import org.researchedc.module.subject.dto.SignSubjectRequest;
import org.researchedc.app.dto.StudySubjectDTO;
import org.researchedc.module.subject.dto.SubjectDTO;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.subject.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final CurrentUserUtils currentUserUtils;

    public SubjectController(SubjectService subjectService, CurrentUserUtils currentUserUtils) {
        this.subjectService = subjectService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/search")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<SubjectDTO>> searchSubjects(@RequestParam String query) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(subjectService.searchSubjects(query, currentUserId));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<SubjectDTO> getSubject(@PathVariable Integer id) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(subjectService.getSubject(id, currentUserId));
    }

    @GetMapping("/by-study")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<StudySubjectDTO>> listStudySubjects(@RequestParam Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(subjectService.listStudySubjects(studyId, currentUserId));
    }

    @GetMapping("/enrollment/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<StudySubjectDTO> getStudySubject(@PathVariable Integer id) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(subjectService.getStudySubject(id, currentUserId));
    }

    @PostMapping
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<SubjectDTO> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        SubjectDTO dto = subjectService.createSubject(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/enroll")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<StudySubjectDTO> enrollSubject(@Valid @RequestBody EnrollSubjectRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        StudySubjectDTO dto = subjectService.enrollSubject(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/{id}/sign")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> signStudySubject(@PathVariable Integer id,
            @Valid @RequestBody SignSubjectRequest request) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.signStudySubject(id, request.getReason(), userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reassign")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> reassignStudySubject(@PathVariable Integer id,
            @Valid @RequestBody ReassignStudySubjectRequest request) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.reassignStudySubject(id, request.getStudyId(), userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> removeSubject(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.removeSubject(id, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> restoreSubject(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.restoreSubject(id, userId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/enrollment/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> removeStudySubject(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.removeStudySubject(id, userId);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/enrollment/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> restoreStudySubject(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.restoreStudySubject(id, userId);
        return ResponseEntity.ok().build();
    }
}
