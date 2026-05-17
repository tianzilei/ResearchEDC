package org.akaza.openclinica.module.export.controller;

import java.util.List;
import org.akaza.openclinica.module.export.dto.CreateExportJobRequest;
import org.akaza.openclinica.module.export.dto.ExportJobDTO;
import org.akaza.openclinica.module.export.service.ExportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exports")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping
    public ResponseEntity<ExportJobDTO> createJob(@RequestBody CreateExportJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exportService.createJob(request));
    }

    @GetMapping
    public ResponseEntity<List<ExportJobDTO>> listJobs(@RequestParam Integer studyId) {
        return ResponseEntity.ok(exportService.listJobs(studyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExportJobDTO> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(exportService.getJob(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ExportJobDTO> cancelJob(@PathVariable Long id) {
        return ResponseEntity.ok(exportService.cancelJob(id));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<ExportJobDTO> retryJob(@PathVariable Long id) {
        return ResponseEntity.ok(exportService.retryJob(id));
    }
}
