package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.module.crf.dto.CrfSummaryDTO;
import org.researchedc.module.crf.dto.CrfVersionDTO;
import org.researchedc.module.crf.dto.ItemDTO;
import org.researchedc.module.crf.dto.SectionDTO;
import org.springframework.stereotype.Component;

/**
 * Anti-corruption layer adapter that bridges the CRF module to legacy
 * {@code core.dao.*} and {@code core.bean.*} classes.
 *
 * <p>This is the ONLY class in the CRF module that may import from legacy
 * packages. All other classes must go through this adapter to access
 * legacy CRF data.</p>
 */
@Component
public class LegacyCrfAdapter {

    private final CRFDAO crfDao;
    private final CRFVersionDAO crfVersionDao;
    private final ItemDAO itemDao;
    private final ItemFormMetadataDAO itemFormMetadataDao;
    private final SectionDAO sectionDao;

    @SuppressWarnings("unchecked")
    public LegacyCrfAdapter(DataSource dataSource) {
        this.crfDao = new CRFDAO(dataSource);
        this.crfVersionDao = new CRFVersionDAO(dataSource);
        this.itemDao = new ItemDAO(dataSource);
        this.itemFormMetadataDao = new ItemFormMetadataDAO(dataSource);
        this.sectionDao = new SectionDAO(dataSource);
    }

    @SuppressWarnings("unchecked")
    public List<CrfSummaryDTO> findAllCrfs() {
        List<CrfSummaryDTO> result = new ArrayList<>();
        for (Object obj : crfDao.findAll()) {
            CRFBean crf = (CRFBean) obj;
            CrfSummaryDTO dto = new CrfSummaryDTO();
            dto.setCrfId(crf.getId());
            dto.setName(crf.getName());
            dto.setDescription(crf.getDescription());
            dto.setOcOid(crf.getOid());
            dto.setStatus(crf.getStatus() != null ? crf.getStatus().getName() : "unknown");
            dto.setDateCreated(toDate(crf.getCreatedDate()));
            dto.setDateUpdated(toDate(crf.getUpdatedDate()));
            dto.setVersionCount(countVersionsByCrf(crf.getId()));
            result.add(dto);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public CrfVersionDTO findVersionById(int crfVersionId) {
        CRFVersionBean version = (CRFVersionBean) crfVersionDao.findByPK(crfVersionId);
        if (version == null) {
            return null;
        }

        CrfVersionDTO dto = new CrfVersionDTO();
        dto.setCrfVersionId(version.getId());
        dto.setCrfId(version.getCrfId());
        dto.setName(version.getName());
        dto.setDescription(version.getDescription());
        dto.setRevisionNotes(version.getRevisionNotes());
        dto.setOcOid(version.getOid());
        dto.setStatus(version.getStatus() != null ? version.getStatus().getName() : "unknown");
        dto.setDateCreated(toDate(version.getCreatedDate()));
        dto.setSections(findSectionsByVersionId(crfVersionId));
        return dto;
    }

    @SuppressWarnings("unchecked")
    public List<ItemDTO> findItemsBySectionAndVersion(int sectionId, int crfVersionId) {
        List<ItemDTO> result = new ArrayList<>();
        List<Object> metadataList;
        try {
            metadataList = (List<Object>) (List<?>) itemFormMetadataDao.findAllByCRFVersionId(crfVersionId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load items for section " + sectionId, e);
        }
        for (Object obj : metadataList) {
            ItemFormMetadataBean meta = (ItemFormMetadataBean) obj;
            if (meta.getSectionId() != sectionId) {
                continue;
            }
            ItemBean item = (ItemBean) itemDao.findByPK(meta.getItemId());
            if (item == null) {
                continue;
            }
            ItemDTO dto = new ItemDTO();
            dto.setItemId(item.getId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setUnits(item.getUnits());
            dto.setDataType(item.getDataType() != null ? item.getDataType().getName() : "text");
            dto.setOcOid(item.getOid());
            dto.setPhi(item.isPhiStatus());
            dto.setOrdinal(meta.getOrdinal());
            dto.setDefaultValue(meta.getDefaultValue());
            dto.setRequired(meta.isRequired());
            dto.setRegexp(meta.getRegexp());
            dto.setRegexpErrorMsg(meta.getRegexpErrorMsg());
            result.add(dto);
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    private int countVersionsByCrf(int crfId) {
        return crfVersionDao.findAllByCRF(crfId).size();
    }

    @SuppressWarnings("unchecked")
    private List<SectionDTO> findSectionsByVersionId(int crfVersionId) {
        List<Object> sectionList = sectionDao.findAllByCRFVersionId(crfVersionId);
        List<SectionDTO> sectionDTOs = new ArrayList<>();
        for (Object obj : sectionList) {
            SectionBean section = (SectionBean) obj;
            SectionDTO sd = new SectionDTO();
            sd.setSectionId(section.getId());
            sd.setCrfVersionId(crfVersionId);
            sd.setLabel(section.getLabel());
            sd.setTitle(section.getTitle());
            sd.setOrdinal(section.getOrdinal());
            sectionDTOs.add(sd);
        }
        return sectionDTOs;
    }

    private static Date toDate(Object value) {
        if (value instanceof Date) {
            return (Date) value;
        }
        return null;
    }
}
