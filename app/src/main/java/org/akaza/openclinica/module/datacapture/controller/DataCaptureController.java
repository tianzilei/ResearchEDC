package org.akaza.openclinica.module.datacapture.controller;

import java.util.List;
import org.akaza.openclinica.module.datacapture.dto.ItemDataDTO;
import org.akaza.openclinica.module.datacapture.dto.ItemGroupDTO;
import org.akaza.openclinica.module.datacapture.dto.ResponseSetDTO;
import org.akaza.openclinica.module.datacapture.service.DataCaptureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
