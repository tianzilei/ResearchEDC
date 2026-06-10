package org.researchedc.module.subjectgroup.controller;

import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.subjectgroup.entity.StudyGroupClassEntity;
import org.researchedc.module.subjectgroup.service.SubjectGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subject-groups")
public class SubjectGroupController {

    private final SubjectGroupService subjectGroupService;
    private final CurrentUserUtils currentUserUtils;

    public SubjectGroupController(SubjectGroupService subjectGroupService,
                                   CurrentUserUtils currentUserUtils) {
        this.subjectGroupService = subjectGroupService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/classes/{id}")
    public ResponseEntity<StudyGroupClassEntity> getClass(@PathVariable Integer id) {
        return ResponseEntity.ok(subjectGroupService.getClassById(id));
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<StudyGroupClassEntity> removeClass(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(subjectGroupService.removeClass(id, userId));
    }

    @PatchMapping("/classes/{id}")
    public ResponseEntity<StudyGroupClassEntity> restoreClass(@PathVariable Integer id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(subjectGroupService.restoreClass(id, userId));
    }
}
