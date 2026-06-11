package org.researchedc.module.dataimport.controller;

import java.util.List;

import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.dataimport.dto.CreateImportJobRequest;
import org.researchedc.module.dataimport.dto.ImportJobDTO;
import org.researchedc.module.dataimport.service.ImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/imports")
public class ImportController {

    private final ImportService importService;
    private final CurrentUserUtils currentUserUtils;

    public ImportController(ImportService importService, CurrentUserUtils currentUserUtils) {
        this.importService = importService;
        this.currentUserUtils = currentUserUtils;
    }

    @PostMapping
    public ResponseEntity<ImportJobDTO> createJob(@RequestBody CreateImportJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(importService.createJob(request));
    }

    @PostMapping("/upload")
    public ResponseEntity<ImportJobDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "importType", defaultValue = "CRF_DATA") String importType,
            @RequestParam(value = "studyId", required = false) Integer studyId,
            @RequestParam(value = "name", required = false) String name) {
        Integer userId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(importService.uploadFile(file, importType, studyId, name, userId));
    }

    @GetMapping
    public ResponseEntity<List<ImportJobDTO>> listJobs(@RequestParam Integer studyId) {
        return ResponseEntity.ok(importService.listJobs(studyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportJobDTO> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(importService.getJob(id));
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<ImportJobDTO> validate(@PathVariable Long id) {
        try {
            importService.validate(id);
            return ResponseEntity.ok(importService.getJob(id));
        } catch (Exception e) {
            importService.markFailed(id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(importService.getJob(id));
        }
    }

    @PostMapping("/{id}/commit")
    public ResponseEntity<ImportJobDTO> commit(@PathVariable Long id) {
        try {
            importService.commit(id);
            return ResponseEntity.ok(importService.getJob(id));
        } catch (Exception e) {
            importService.markFailed(id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(importService.getJob(id));
        }
    }
}
