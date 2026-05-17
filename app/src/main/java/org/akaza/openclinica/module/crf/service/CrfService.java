package org.akaza.openclinica.module.crf.service;

import java.util.List;
import java.util.ArrayList;
import javax.sql.DataSource;
import org.akaza.openclinica.module.crf.dto.CrfSummaryDTO;
import org.akaza.openclinica.module.crf.dto.CrfVersionDTO;
import org.akaza.openclinica.module.crf.dto.ItemDTO;
import org.akaza.openclinica.module.crf.dto.SectionDTO;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.springframework.stereotype.Service;

@Service
public class CrfService {

    private final CRFDAO crfDao;
    private final CRFVersionDAO crfVersionDao;
    private final ItemDAO itemDao;
    private final ItemFormMetadataDAO itemFormMetadataDao;
    private final SectionDAO sectionDao;

    @SuppressWarnings("unchecked")
    public CrfService(DataSource dataSource) {
        this.crfDao = new CRFDAO(dataSource);
        this.crfVersionDao = new CRFVersionDAO(dataSource);
        this.itemDao = new ItemDAO(dataSource);
        this.itemFormMetadataDao = new ItemFormMetadataDAO(dataSource);
        this.sectionDao = new SectionDAO(dataSource);
    }

    @SuppressWarnings("unchecked")
    public List<CrfSummaryDTO> listCrfs() {
        List<CrfSummaryDTO> result = new ArrayList<>();
        for (Object obj : crfDao.findAll()) {
            CRFBean crf = (CRFBean) obj;
            CrfSummaryDTO dto = new CrfSummaryDTO();
            dto.setCrfId(crf.getId());
            dto.setName(crf.getName());
            dto.setDescription(crf.getDescription());
            dto.setOcOid(crf.getOid());
            dto.setStatus(crf.getStatus() != null ? crf.getStatus().getName() : "unknown");
            dto.setDateCreated(crf.getCreatedDate());
            dto.setDateUpdated(crf.getUpdatedDate());
            dto.setVersionCount(crfVersionDao.findAllByCRF(crf.getId()).size());
            result.add(dto);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public CrfVersionDTO getVersion(int crfVersionId) {
        CRFVersionBean version = (CRFVersionBean) crfVersionDao.findByPK(crfVersionId);
        if (version == null) return null;

        CrfVersionDTO dto = new CrfVersionDTO();
        dto.setCrfVersionId(version.getId());
        dto.setCrfId(version.getCrfId());
        dto.setName(version.getName());
        dto.setDescription(version.getDescription());
        dto.setRevisionNotes(version.getRevisionNotes());
        dto.setOcOid(version.getOid());
        dto.setStatus(version.getStatus() != null ? version.getStatus().getName() : "unknown");
        dto.setDateCreated(version.getCreatedDate());

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
        dto.setSections(sectionDTOs);
        return dto;
    }

    @SuppressWarnings("unchecked")
    public List<ItemDTO> getItemsBySection(int sectionId, int crfVersionId) {
        List<ItemDTO> result = new ArrayList<>();
        List<Object> metadataList;
        try {
            metadataList = (List<Object>) (List<?>) itemFormMetadataDao.findAllByCRFVersionId(crfVersionId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load items for section " + sectionId, e);
        }
        for (Object obj : metadataList) {
            ItemFormMetadataBean meta = (ItemFormMetadataBean) obj;
            ItemBean item = (ItemBean) itemDao.findByPK(meta.getItemId());
            if (item == null) continue;
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
}
