package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.module.crf.dto.CrfSummaryDTO;
import org.researchedc.module.crf.dto.CrfVersionDTO;
import org.researchedc.module.crf.dto.ItemDTO;
import org.researchedc.module.crf.dto.SectionDTO;
import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.entity.ItemFormMetadataEntity;
import org.researchedc.module.crf.entity.SectionEntity;
import org.researchedc.module.crf.repository.CrfRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.module.crf.repository.ItemFormMetadataRepository;
import org.researchedc.module.crf.repository.ItemRepository;
import org.researchedc.module.crf.repository.SectionRepository;
import org.springframework.stereotype.Component;

@Component
public class LegacyCrfAdapter {

    private final CrfRepository crfRepository;
    private final CrfVersionRepository crfVersionRepository;
    private final SectionRepository sectionRepository;
    private final ItemRepository itemRepository;
    private final ItemFormMetadataRepository itemFormMetadataRepository;

    public LegacyCrfAdapter(CrfRepository crfRepository,
                            CrfVersionRepository crfVersionRepository,
                            SectionRepository sectionRepository,
                            ItemRepository itemRepository,
                            ItemFormMetadataRepository itemFormMetadataRepository) {
        this.crfRepository = crfRepository;
        this.crfVersionRepository = crfVersionRepository;
        this.sectionRepository = sectionRepository;
        this.itemRepository = itemRepository;
        this.itemFormMetadataRepository = itemFormMetadataRepository;
    }

    public List<CrfSummaryDTO> findAllCrfs() {
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

    public CrfVersionDTO findVersionById(int crfVersionId) {
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
        dto.setStatus(String.valueOf(version.getStatusId()));
        dto.setSections(findSectionsByVersionId(crfVersionId));
        return dto;
    }

    public List<ItemDTO> findItemsBySectionAndVersion(int sectionId, int crfVersionId) {
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
