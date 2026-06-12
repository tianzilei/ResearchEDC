package org.researchedc.module.datacapture.controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.researchedc.module.datacapture.dto.BatchSaveItemsRequest;
import org.researchedc.module.datacapture.dto.ItemDataDTO;
import org.researchedc.module.datacapture.dto.ItemGroupDTO;
import org.researchedc.module.datacapture.dto.ResponseSetDTO;
import org.researchedc.module.datacapture.dto.RuleEvalResponse;
import org.researchedc.module.datacapture.dto.SaveItemDataRequest;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.datacapture.service.DataCaptureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/data-capture")
public class DataCaptureController {

    private final DataCaptureService dataCaptureService;
    private final CurrentUserUtils currentUserUtils;

    public DataCaptureController(DataCaptureService dataCaptureService, CurrentUserUtils currentUserUtils) {
        this.dataCaptureService = dataCaptureService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemDataDTO>> getItemData(
            @RequestParam Integer eventCrfId) {
        return ResponseEntity.ok(dataCaptureService.getItemDataByEventCrf(eventCrfId));
    }

    @GetMapping("/response-sets/{id}")
    public ResponseEntity<ResponseSetDTO> getResponseSet(@PathVariable Integer id) {
        return ResponseEntity.ok(dataCaptureService.getResponseSet(id));
    }

    @GetMapping("/item-groups")
    public ResponseEntity<List<ItemGroupDTO>> getItemGroups(
            @RequestParam(required = false) Integer crfId,
            @RequestParam(required = false) Integer crfVersionId) {
        if (crfVersionId != null) {
            return ResponseEntity.ok(dataCaptureService.getItemGroupsByCrfVersion(crfVersionId));
        }
        if (crfId != null) {
            return ResponseEntity.ok(dataCaptureService.getItemGroupsByCrf(crfId));
        }
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/items")
    public ResponseEntity<ItemDataDTO> saveItemData(
            @Valid @RequestBody SaveItemDataRequest request) {
        Integer userId = currentUserUtils.getCurrentUserId();
        ItemDataDTO dto = dataCaptureService.saveItemData(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/items/batch")
    public ResponseEntity<List<ItemDataDTO>> batchSaveItems(
            @Valid @RequestBody BatchSaveItemsRequest request) {
        Integer userId = currentUserUtils.getCurrentUserId();
        List<ItemDataDTO> dtos = dataCaptureService.batchSaveItems(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    @GetMapping("/{eventCrfId}/rules")
    public ResponseEntity<RuleEvalResponse> evaluateRules(
            @PathVariable int eventCrfId) {
        return ResponseEntity.ok(dataCaptureService.evaluateRules(eventCrfId));
    }

    @GetMapping("/attachments")
    public void downloadAttachment(@RequestParam String fileName,
                                    @RequestParam String studyOid,
                                    HttpServletResponse response) {
        dataCaptureService.downloadAttachment(fileName, studyOid, response);
    }

    @GetMapping("/attachments/by-event-crf")
    public void downloadAttachmentByEventCrf(@RequestParam int eventCrfId,
                                              @RequestParam String fileName,
                                              HttpServletResponse response) {
        dataCaptureService.downloadAttachmentByEventCrf(eventCrfId, fileName, response);
    }

    @GetMapping("/attachments/list-by-event-crf")
    public ResponseEntity<List<String>> listAttachmentsByEventCrf(
            @RequestParam int eventCrfId) {
        return ResponseEntity.ok(dataCaptureService.listAttachmentsByEventCrf(eventCrfId));
    }

    @PostMapping("/attachments")
    public ResponseEntity<Void> uploadAttachment(
            @RequestParam int eventCrfId,
            @RequestParam("file") MultipartFile file) {
        try {
            dataCaptureService.uploadAttachment(eventCrfId, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
