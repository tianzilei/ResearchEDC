package org.researchedc.module.dataset.controller;

import java.util.List;

import org.researchedc.module.dataset.dto.CreateDatasetRequest;
import org.researchedc.module.dataset.dto.DatasetDTO;
import org.researchedc.module.dataset.entity.DatasetEntity;
import org.researchedc.module.dataset.service.DatasetService;
import org.researchedc.config.CurrentUserUtils;
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
@RequestMapping("/api/v1/datasets")
public class DatasetController {

    private final DatasetService datasetService;
    private final CurrentUserUtils currentUserUtils;

    public DatasetController(DatasetService datasetService, CurrentUserUtils currentUserUtils) {
        this.datasetService = datasetService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping
    public ResponseEntity<List<DatasetDTO>> listDatasets(
            @RequestParam(required = false) Integer studyId) {
        List<DatasetEntity> entities = studyId != null
                ? datasetService.listByStudy(studyId)
                : datasetService.listAll();
        return ResponseEntity.ok(entities.stream().map(this::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatasetDTO> getDataset(@PathVariable int id) {
        try {
            return ResponseEntity.ok(toDto(datasetService.getById(id)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<DatasetDTO> createDataset(@RequestBody CreateDatasetRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        DatasetEntity entity = datasetService.create(
                request.getName(), request.getDescription(), request.getStudyId(), ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(entity));
    }

    private DatasetDTO toDto(DatasetEntity entity) {
        DatasetDTO dto = new DatasetDTO();
        dto.setDatasetId(entity.getDatasetId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStudyId(entity.getStudyId());
        dto.setOwnerId(entity.getOwnerId());
        dto.setStatusId(entity.getStatusId());
        dto.setDateCreated(entity.getDateCreated());
        return dto;
    }
}
