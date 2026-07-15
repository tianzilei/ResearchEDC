package org.researchedc.module.studybuild.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.studybuild.dto.ApplyStudyTemplateRequest;
import org.researchedc.module.studybuild.dto.CreateStudyTemplateRequest;
import org.researchedc.module.studybuild.dto.StudyTemplateApplicationDTO;
import org.researchedc.module.studybuild.dto.StudyTemplateDTO;
import org.researchedc.module.studybuild.service.StudyBuildService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/study-build")
public class StudyBuildController {

    private final StudyBuildService studyBuildService;
    private final CurrentUserUtils currentUserUtils;

    public StudyBuildController(StudyBuildService studyBuildService, CurrentUserUtils currentUserUtils) {
        this.studyBuildService = studyBuildService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/templates")
    public ResponseEntity<List<StudyTemplateDTO>> listTemplates() {
        return ResponseEntity.ok(studyBuildService.listTemplates());
    }

    @PostMapping("/templates")
    @PreAuthorize(CoreEdcAuthorityExpressions.ADMINISTER_STUDIES)
    public ResponseEntity<StudyTemplateDTO> createTemplate(@Valid @RequestBody CreateStudyTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyBuildService.createTemplate(request, currentUserUtils.getCurrentUserId()));
    }

    @PostMapping("/templates/{templateId}/apply")
    @PreAuthorize(CoreEdcAuthorityExpressions.ADMINISTER_STUDIES)
    public ResponseEntity<StudyTemplateApplicationDTO> applyTemplate(@PathVariable Long templateId,
            @Valid @RequestBody ApplyStudyTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyBuildService.applyTemplate(templateId, request, currentUserUtils.getCurrentUserId()));
    }
}
