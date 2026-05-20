package org.researchedc.module.datacapture.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.researchedc.module.datacapture.dto.BatchSaveItemsRequest;
import org.researchedc.module.datacapture.dto.ItemDataDTO;
import org.researchedc.module.datacapture.dto.ItemGroupDTO;
import org.researchedc.module.datacapture.dto.ResponseSetDTO;
import org.researchedc.module.datacapture.dto.SaveItemDataRequest;
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

@RestController
@RequestMapping("/api/v1/data-capture")
public class DataCaptureController {

    private final DataCaptureService dataCaptureService;

    public DataCaptureController(DataCaptureService dataCaptureService) {
        this.dataCaptureService = dataCaptureService;
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
            @RequestParam Integer crfId) {
        return ResponseEntity.ok(dataCaptureService.getItemGroupsByCrf(crfId));
    }

    @PostMapping("/items")
    public ResponseEntity<ItemDataDTO> saveItemData(
            @Valid @RequestBody SaveItemDataRequest request) {
        Integer userId = 1;
        ItemDataDTO dto = dataCaptureService.saveItemData(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/items/batch")
    public ResponseEntity<List<ItemDataDTO>> batchSaveItems(
            @Valid @RequestBody BatchSaveItemsRequest request) {
        Integer userId = 1;
        List<ItemDataDTO> dtos = dataCaptureService.batchSaveItems(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }
}
