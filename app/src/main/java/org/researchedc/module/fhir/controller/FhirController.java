package org.researchedc.module.fhir.controller;

import java.util.List;
import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.fhir.dto.CreateFhirConnectorRequest;
import org.researchedc.module.fhir.dto.FhirConnectorDTO;
import org.researchedc.module.fhir.dto.FhirImportRecordDTO;
import org.researchedc.module.fhir.dto.ReconcileFhirRecordRequest;
import org.researchedc.module.fhir.dto.SubmitFhirResourceRequest;
import org.researchedc.module.fhir.enums.FhirImportStatus;
import org.researchedc.module.fhir.service.FhirService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fhir")
public class FhirController {
    private final FhirService fhirService;
    private final CurrentUserUtils currentUserUtils;

    public FhirController(FhirService fhirService, CurrentUserUtils currentUserUtils) {
        this.fhirService = fhirService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/connectors")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<FhirConnectorDTO>> listConnectors(@RequestParam Integer studyId) {
        return ResponseEntity.ok(fhirService.listConnectors(studyId, currentUserUtils.getCurrentUserId()));
    }

    @PostMapping("/connectors")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<FhirConnectorDTO> createConnector(@RequestBody CreateFhirConnectorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fhirService.createConnector(request, currentUserUtils.getCurrentUserId()));
    }

    @GetMapping("/records")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<FhirImportRecordDTO>> listRecords(
            @RequestParam Integer studyId,
            @RequestParam(required = false) FhirImportStatus status) {
        return ResponseEntity.ok(fhirService.listRecords(studyId, status, currentUserUtils.getCurrentUserId()));
    }

    @PostMapping("/records")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<FhirImportRecordDTO> submitResource(@RequestBody SubmitFhirResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fhirService.submitResource(request, currentUserUtils.getCurrentUserId()));
    }

    @PostMapping("/records/{recordId}/reconcile")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<FhirImportRecordDTO> reconcile(
            @PathVariable Long recordId,
            @RequestBody ReconcileFhirRecordRequest request) {
        return ResponseEntity.ok(fhirService.reconcile(recordId, request, currentUserUtils.getCurrentUserId()));
    }
}
