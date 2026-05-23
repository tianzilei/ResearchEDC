package org.researchedc.module.legacy.controller;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.researchedc.module.dataset.entity.DatasetEntity;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.dataset.service.DatasetService;
import org.researchedc.module.legacy.dto.DatasetDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/datasets")
public class LegacyDatasetController {

    private final DatasetService datasetService;
    private final CurrentUserUtils currentUserUtils;

    public LegacyDatasetController(DatasetService datasetService, CurrentUserUtils currentUserUtils) {
        this.datasetService = datasetService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping
    public ResponseEntity<List<DatasetDTO>> listDatasets(
            @RequestParam(required = false) Integer studyId) {
        List<DatasetDTO> result = new ArrayList<>();
        List<DatasetEntity> entities;
        if (studyId != null) {
            entities = datasetService.listByStudy(studyId);
        } else {
            entities = datasetService.listAll();
        }
        for (DatasetEntity entity : entities) {
            result.add(toDto(entity));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatasetDTO> getDataset(@PathVariable int id) {
        try {
            DatasetEntity entity = datasetService.getById(id);
            return ResponseEntity.ok(toDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<DatasetDTO> createDataset(@RequestParam String name,
            @RequestParam int studyId) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        DatasetEntity entity = datasetService.create(name, null, studyId, ownerId);
        return ResponseEntity.ok(toDto(entity));
    }

    private static DatasetDTO toDto(DatasetEntity entity) {
        DatasetDTO dto = new DatasetDTO();
        dto.setDatasetId(entity.getDatasetId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStudyId(entity.getStudyId() != null ? entity.getStudyId() : 0);
        dto.setOwnerId(entity.getOwnerId() != null ? entity.getOwnerId() : 0);
        if (entity.getDateCreated() != null) {
            dto.setDateCreated(Date.from(
                    entity.getDateCreated().atZone(ZoneId.systemDefault()).toInstant()));
        }
        dto.setStatus(String.valueOf(entity.getStatusId()));
        return dto;
    }
}
