package org.researchedc.module.dataset.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.module.dataset.entity.DatasetEntity;
import org.researchedc.module.dataset.repository.DatasetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatasetService {

    private final DatasetRepository datasetRepository;

    public DatasetService(DatasetRepository datasetRepository) {
        this.datasetRepository = datasetRepository;
    }

    public List<DatasetEntity> listAll() {
        return datasetRepository.findAll();
    }

    public List<DatasetEntity> listByStudy(Integer studyId) {
        return datasetRepository.findByStudyId(studyId);
    }

    public DatasetEntity getById(Integer id) {
        return datasetRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dataset not found: " + id));
    }

    @Transactional
    public DatasetEntity create(String name, String description, Integer studyId, Integer ownerId) {
        DatasetEntity entity = new DatasetEntity();
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setStudyId(studyId);
        entity.setStatusId(1);
        entity.setOwnerId(ownerId);
        entity.setNumRuns(0);
        entity.setDateCreated(LocalDateTime.now());
        return datasetRepository.save(entity);
    }

    @Transactional
    public DatasetEntity update(Integer id, String name, String description) {
        DatasetEntity entity = datasetRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dataset not found: " + id));
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setDateUpdated(LocalDateTime.now());
        return datasetRepository.save(entity);
    }
}
