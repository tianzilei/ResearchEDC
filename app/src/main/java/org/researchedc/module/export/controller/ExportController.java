package org.researchedc.module.export.controller;

import java.util.List;
import org.researchedc.module.export.dto.CreateExportJobRequest;
import org.researchedc.module.export.dto.ExportJobDTO;
import org.researchedc.module.export.dto.ExportJobFilter;
import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.export.enums.OdmContractVersion;
import org.researchedc.module.export.service.ExportService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    public ResponseEntity<List<ExportJobDTO>> listJobs(
            @RequestParam Integer studyId,
            @RequestParam(required = false) ExportJobStatus status,
            @RequestParam(required = false) ExportFormat exportFormat,
            @RequestParam(required = false) OdmContractVersion odmContractVersion,
            @RequestParam(required = false) Integer requestedBy) {
        if (status == null && exportFormat == null && odmContractVersion == null && requestedBy == null) {
            return ResponseEntity.ok(exportService.listJobs(studyId));
        }
        ExportJobFilter filter = new ExportJobFilter();
        filter.setStatus(status);
        filter.setExportFormat(exportFormat);
        filter.setOdmContractVersion(odmContractVersion);
        filter.setRequestedBy(requestedBy);
        return ResponseEntity.ok(exportService.listJobs(studyId, filter));
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

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadExport(@PathVariable Long id) {
        ExportService.DownloadResult result = exportService.getDownload(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.filename() + "\"")
                .contentLength(result.fileSize())
                .body(result.resource());
    }
}
