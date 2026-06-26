package org.researchedc.module.crf.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.researchedc.module.crf.dto.CrfSummaryDTO;
import org.researchedc.app.dto.CrfVersionDTO;
import org.researchedc.module.crf.dto.ItemDTO;
import org.researchedc.module.crf.dto.SectionDTO;
import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.entity.SectionEntity;
import org.researchedc.module.crf.entity.ItemFormMetadataEntity;
import org.researchedc.module.crf.internal.adapter.SCDItemMetadataDaoAdapter;
import org.researchedc.module.crf.internal.adapter.SCDItemMetadataDaoAdapter.ScdRule;
import org.researchedc.module.crf.repository.CrfRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.module.crf.repository.ItemFormMetadataRepository;
import org.researchedc.module.crf.repository.ItemRepository;
import org.researchedc.module.crf.repository.SectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CrfService {

    private final CrfRepository crfRepository;
    private final CrfVersionRepository crfVersionRepository;
    private final SectionRepository sectionRepository;
    private final ItemRepository itemRepository;
    private final ItemFormMetadataRepository itemFormMetadataRepository;
    private final SCDItemMetadataDaoAdapter scdAdapter;

    public CrfService(CrfRepository crfRepository,
                      CrfVersionRepository crfVersionRepository,
                      SectionRepository sectionRepository,
                      ItemRepository itemRepository,
                      ItemFormMetadataRepository itemFormMetadataRepository,
                      SCDItemMetadataDaoAdapter scdAdapter) {
        this.crfRepository = crfRepository;
        this.crfVersionRepository = crfVersionRepository;
        this.sectionRepository = sectionRepository;
        this.itemRepository = itemRepository;
        this.itemFormMetadataRepository = itemFormMetadataRepository;
        this.scdAdapter = scdAdapter;
    }

    public List<CrfSummaryDTO> listCrfs() {
        List<CrfSummaryDTO> result = new ArrayList<>();
        for (CrfEntity crf : crfRepository.findAll()) {
            CrfSummaryDTO dto = new CrfSummaryDTO();
            dto.setCrfId(crf.getCrfId());
            dto.setName(crf.getName());
            dto.setDescription(crf.getDescription());
            dto.setOcOid(crf.getOcOid());
            dto.setStatus(String.valueOf(crf.getStatusId()));
            dto.setVersionCount(crfVersionRepository.findByCrfIdOrderByCrfVersionId(crf.getCrfId()).size());
            result.add(dto);
        }
        return result;
    }

    public CrfVersionDTO getVersion(int crfVersionId) {
        CrfVersionEntity version = crfVersionRepository.findById(crfVersionId).orElse(null);
        if (version == null) {
            return null;
        }

        CrfVersionDTO dto = new CrfVersionDTO();
        dto.setCrfVersionId(version.getCrfVersionId());
        dto.setCrfId(version.getCrfId());
        dto.setName(version.getName());
        dto.setDescription(version.getDescription());
        dto.setRevisionNotes(version.getRevisionNotes());
        dto.setOcOid(version.getOcOid());
        dto.setStatusName(String.valueOf(version.getStatusId()));
        dto.setSections(findSectionsByVersionId(crfVersionId));
        return dto;
    }

    public List<ItemDTO> getItemsBySection(int sectionId, int crfVersionId) {
        List<ItemDTO> result = new ArrayList<>();
        for (ItemFormMetadataEntity meta : itemFormMetadataRepository.findByCrfVersionId(crfVersionId)) {
            if (meta.getSectionId() == null || meta.getSectionId() != sectionId) {
                continue;
            }
            ItemEntity item = itemRepository.findById(meta.getItemId()).orElse(null);
            if (item == null) {
                continue;
            }
            ItemDTO dto = new ItemDTO();
            dto.setItemId(item.getItemId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setUnits(item.getUnits());
            dto.setDataType("text");
            dto.setOcOid(item.getOcOid());
            dto.setPhi(item.getPhiStatus() != null && item.getPhiStatus());
            dto.setOrdinal(meta.getOrdinal() != null ? meta.getOrdinal() : 0);
            dto.setDefaultValue(meta.getDefaultValue());
            dto.setRequired(meta.getRequired() != null && meta.getRequired());
            dto.setRegexp(meta.getRegexp());
            dto.setRegexpErrorMsg(meta.getRegexpErrorMsg());
            result.add(dto);
        }
        return result;
    }

    public List<Map<String, Object>> getScdRulesBySection(int sectionId) {
        List<ScdRule> scdBeans = scdAdapter.findRulesBySectionId(sectionId);
        List<Map<String, Object>> rules = new ArrayList<>();
        for (ScdRule scd : scdBeans) {
            Map<String, Object> rule = new HashMap<>();
            rule.put("scdItemId", scd.scdItemId());
            rule.put("controlItemId", scd.controlItemFormMetadataId());
            rule.put("controlItemName", scd.controlItemName());
            rule.put("optionValue", scd.optionValue());
            rule.put("message", scd.message());

            var targetMeta = itemFormMetadataRepository.findById(scd.scdItemId());
            targetMeta.ifPresent(m -> rule.put("targetItemId", m.getItemId()));
            var controlMeta = itemFormMetadataRepository.findById(scd.controlItemFormMetadataId());
            controlMeta.ifPresent(m -> rule.put("controlItemId", m.getItemId()));

            rules.add(rule);
        }
        return rules;
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

    @Transactional
    public void updateVersionStatus(Integer crfVersionId, Integer statusId) {
        CrfVersionEntity entity = crfVersionRepository.findById(crfVersionId)
                .orElseThrow(() -> new NoSuchElementException("CRF version not found: " + crfVersionId));
        entity.setStatusId(statusId);
        entity.setDateUpdated(LocalDateTime.now());
        crfVersionRepository.save(entity);
    }

    private List<SectionDTO> findSectionsByVersionId(int crfVersionId) {
        List<SectionDTO> sectionDTOs = new ArrayList<>();
        for (SectionEntity section : sectionRepository.findByCrfVersionIdOrderByOrdinal(crfVersionId)) {
            SectionDTO sd = new SectionDTO();
            sd.setSectionId(section.getSectionId());
            sd.setCrfVersionId(crfVersionId);
            sd.setLabel(section.getLabel());
            sd.setTitle(section.getTitle());
            sd.setOrdinal(section.getOrdinal() != null ? section.getOrdinal() : 0);
            sectionDTOs.add(sd);
        }
        return sectionDTOs;
    }
}
