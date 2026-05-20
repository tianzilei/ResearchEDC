package org.researchedc.module.crf.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.module.crf.dto.CrfSummaryDTO;
import org.researchedc.module.crf.dto.CrfVersionDTO;
import org.researchedc.module.crf.dto.ItemDTO;
import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.internal.adapter.LegacyCrfAdapter;
import org.researchedc.module.crf.repository.CrfRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CrfService {

    private final LegacyCrfAdapter legacyCrfAdapter;
    private final CrfRepository crfRepository;
    private final CrfVersionRepository crfVersionRepository;

    public CrfService(LegacyCrfAdapter legacyCrfAdapter,
                      CrfRepository crfRepository,
                      CrfVersionRepository crfVersionRepository) {
        this.legacyCrfAdapter = legacyCrfAdapter;
        this.crfRepository = crfRepository;
        this.crfVersionRepository = crfVersionRepository;
    }

    public List<CrfSummaryDTO> listCrfs() {
        return legacyCrfAdapter.findAllCrfs();
    }

    public CrfVersionDTO getVersion(int crfVersionId) {
        return legacyCrfAdapter.findVersionById(crfVersionId);
    }

    public List<ItemDTO> getItemsBySection(int sectionId, int crfVersionId) {
        return legacyCrfAdapter.findItemsBySectionAndVersion(sectionId, crfVersionId);
    }

    public List<CrfEntity> getAllCrfEntities() {
        return crfRepository.findAll();
    }

    public CrfEntity getCrfEntity(Integer crfId) {
        return crfRepository.findById(crfId)
                .orElseThrow(() -> new NoSuchElementException("CRF not found: " + crfId));
    }

    public CrfVersionEntity getCrfVersionEntity(Integer crfVersionId) {
        return crfVersionRepository.findById(crfVersionId)
                .orElseThrow(() -> new NoSuchElementException("CRF version not found: " + crfVersionId));
    }

    public List<CrfVersionEntity> listVersionEntities(Integer crfId) {
        return crfVersionRepository.findByCrfIdOrderByCrfVersionId(crfId);
    }

    @Transactional
    public CrfEntity createCrf(String name, String description, Integer ownerId) {
        CrfEntity entity = new CrfEntity();
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setStatusId(1);
        entity.setOwnerId(ownerId);
        entity.setDateCreated(LocalDateTime.now());
        return crfRepository.save(entity);
    }

    @Transactional
    public CrfEntity updateCrf(Integer crfId, String name, String description) {
        CrfEntity entity = crfRepository.findById(crfId)
                .orElseThrow(() -> new NoSuchElementException("CRF not found: " + crfId));
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setDateUpdated(LocalDateTime.now());
        return crfRepository.save(entity);
    }

    @Transactional
    public CrfVersionEntity createVersion(Integer crfId, String name, String description,
                                          String revisionNotes, Integer ownerId) {
        CrfVersionEntity entity = new CrfVersionEntity();
        entity.setCrfId(crfId);
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setRevisionNotes(revisionNotes != null ? revisionNotes : "");
        entity.setStatusId(1);
        entity.setOwnerId(ownerId);
        entity.setDateCreated(LocalDateTime.now());
        return crfVersionRepository.save(entity);
    }

    @Transactional
    public void deleteVersion(Integer crfVersionId) {
        if (!crfVersionRepository.existsById(crfVersionId)) {
            throw new NoSuchElementException("CRF version not found: " + crfVersionId);
        }
        crfVersionRepository.deleteById(crfVersionId);
    }

    private CrfSummaryDTO toSummary(CrfEntity e) {
        CrfSummaryDTO dto = new CrfSummaryDTO();
        dto.setCrfId(e.getCrfId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setOcOid(e.getOcOid());
        dto.setStatus(String.valueOf(e.getStatusId()));
        return dto;
    }
}
