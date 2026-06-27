package org.researchedc.module.dataset.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.dataset.entity.DatasetEntity;
import org.researchedc.module.dataset.repository.DatasetRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final CurrentStudyAccessService currentStudyAccessService;

    public DatasetService(DatasetRepository datasetRepository,
                          CurrentStudyAccessService currentStudyAccessService) {
        this.datasetRepository = datasetRepository;
        this.currentStudyAccessService = currentStudyAccessService;
    }

    public List<DatasetEntity> listAll(Integer currentUserId) {
        if (currentStudyAccessService.canReadAllStudies(currentUserId)) {
            return datasetRepository.findAll();
        }
        Set<Integer> studyIds = currentStudyAccessService.readableStudyIds(currentUserId);
        if (studyIds.isEmpty()) {
            return List.of();
        }
        return datasetRepository.findByStudyIdInOrderByStudyIdAscNameAsc(studyIds);
    }

    public List<DatasetEntity> listByStudy(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return datasetRepository.findByStudyId(studyId);
    }

    public DatasetEntity getById(Integer id, Integer currentUserId) {
        DatasetEntity entity = datasetRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dataset not found: " + id));
        requireReadAccess(currentUserId, entity.getStudyId());
        return entity;
    }

    @Transactional
    public DatasetEntity create(String name, String description, Integer studyId, Integer ownerId,
                                Integer currentUserId) {
        requireWriteAccess(currentUserId, studyId);
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
    public DatasetEntity update(Integer id, String name, String description, Integer currentUserId) {
        DatasetEntity entity = datasetRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dataset not found: " + id));
        requireWriteAccess(currentUserId, entity.getStudyId());
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setDateUpdated(LocalDateTime.now());
        return datasetRepository.save(entity);
    }

    private void requireReadAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canReadStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have read access to this study");
        }
    }

    private void requireWriteAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canWriteStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have write access to this study");
        }
    }
}
