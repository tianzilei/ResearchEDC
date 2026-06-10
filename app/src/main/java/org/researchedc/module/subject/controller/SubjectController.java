package org.researchedc.module.subject.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.researchedc.module.subject.dto.CreateSubjectRequest;
import org.researchedc.module.subject.dto.EnrollSubjectRequest;
import org.researchedc.module.subject.dto.ReassignStudySubjectRequest;
import org.researchedc.module.subject.dto.SignSubjectRequest;
import org.researchedc.module.subject.dto.StudySubjectDTO;
import org.researchedc.module.subject.dto.SubjectDTO;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.subject.service.SubjectService;
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
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final CurrentUserUtils currentUserUtils;

    public SubjectController(SubjectService subjectService, CurrentUserUtils currentUserUtils) {
        this.subjectService = subjectService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/search")
    public ResponseEntity<List<SubjectDTO>> searchSubjects(@RequestParam String query) {
        return ResponseEntity.ok(subjectService.searchSubjects(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> getSubject(@PathVariable Integer id) {
        return ResponseEntity.ok(subjectService.getSubject(id));
    }

    @GetMapping("/by-study")
    public ResponseEntity<List<StudySubjectDTO>> listStudySubjects(@RequestParam Integer studyId) {
        return ResponseEntity.ok(subjectService.listStudySubjects(studyId));
    }

    @GetMapping("/enrollment/{id}")
    public ResponseEntity<StudySubjectDTO> getStudySubject(@PathVariable Integer id) {
        return ResponseEntity.ok(subjectService.getStudySubject(id));
    }

    @PostMapping
    public ResponseEntity<SubjectDTO> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        SubjectDTO dto = subjectService.createSubject(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/enroll")
    public ResponseEntity<StudySubjectDTO> enrollSubject(@Valid @RequestBody EnrollSubjectRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        StudySubjectDTO dto = subjectService.enrollSubject(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/{id}/sign")
    public ResponseEntity<Void> signStudySubject(@PathVariable Integer id,
            @Valid @RequestBody SignSubjectRequest request) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.signStudySubject(id, request.getReason(), userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reassign")
    public ResponseEntity<Void> reassignStudySubject(@PathVariable Integer id,
            @Valid @RequestBody ReassignStudySubjectRequest request) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.reassignStudySubject(id, request.getStudyId(), userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeSubject(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.removeSubject(id, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> restoreSubject(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.restoreSubject(id, userId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/enrollment/{id}")
    public ResponseEntity<Void> removeStudySubject(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.removeStudySubject(id, userId);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/enrollment/{id}")
    public ResponseEntity<Void> restoreStudySubject(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        subjectService.restoreStudySubject(id, userId);
        return ResponseEntity.ok().build();
    }
}