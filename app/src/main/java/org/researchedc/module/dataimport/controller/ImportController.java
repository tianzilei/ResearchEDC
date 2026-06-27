package org.researchedc.module.dataimport.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.dataimport.dto.CreateImportJobRequest;
import org.researchedc.module.dataimport.dto.ImportJobDTO;
import org.researchedc.module.dataimport.dto.ImportPreviewDTO;
import org.researchedc.module.dataimport.dto.ImportResultDTO;
import org.researchedc.module.dataimport.service.ImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize(CoreEdcAuthorityExpressions.IMPORT_DATA)
    public ResponseEntity<ImportJobDTO> createJob(@RequestBody CreateImportJobRequest request) {
        Integer userId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(importService.createJob(request, userId));
    }

    @PostMapping("/upload")
    @PreAuthorize(CoreEdcAuthorityExpressions.IMPORT_DATA)
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
    @PreAuthorize(CoreEdcAuthorityExpressions.IMPORT_DATA)
    public ResponseEntity<List<ImportJobDTO>> listJobs(@RequestParam Integer studyId) {
        Integer userId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(importService.listJobs(studyId, userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.IMPORT_DATA)
    public ResponseEntity<ImportJobDTO> getJob(@PathVariable Long id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(importService.getJob(id, userId));
    }

    @PostMapping("/{id}/validate")
    @PreAuthorize(CoreEdcAuthorityExpressions.IMPORT_DATA)
    public ResponseEntity<ImportPreviewDTO> validate(@PathVariable Long id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        try {
            return ResponseEntity.ok(importService.validate(id, userId));
        } catch (AccessDeniedException | NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            importService.markFailed(id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImportPreviewDTO.failed(e.getMessage()));
        }
    }

    @GetMapping("/{id}/preview")
    @PreAuthorize(CoreEdcAuthorityExpressions.IMPORT_DATA)
    public ResponseEntity<ImportPreviewDTO> preview(@PathVariable Long id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(importService.getPreview(id, userId));
    }

    @PostMapping("/{id}/commit")
    @PreAuthorize(CoreEdcAuthorityExpressions.IMPORT_DATA)
    public ResponseEntity<ImportResultDTO> commit(@PathVariable Long id) {
        Integer userId = currentUserUtils.getCurrentUserId();
        try {
            return ResponseEntity.ok(importService.commit(id, userId));
        } catch (AccessDeniedException | NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            importService.markFailed(id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImportResultDTO.failed(e.getMessage()));
        }
    }
}
