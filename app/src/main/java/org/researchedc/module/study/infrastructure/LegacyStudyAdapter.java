package org.researchedc.module.study.infrastructure;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.module.study.dto.StudyDetailDTO;
import org.researchedc.module.study.dto.StudySummaryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Anti-corruption layer adapter that bridges the Study module to legacy
 * {@code core.dao.*} and {@code core.bean.*} classes.
 *
 * <p>This is the ONLY class in the Study module (outside the entity/repository layer)
 * that may import from legacy packages. All legacy data access goes through this adapter.</p>
 *
 * <p>Follows the same pattern as {@code crf/internal/adapter/LegacyCrfAdapter}.</p>
 */
@Component
public class LegacyStudyAdapter {

    private static final Logger log = LoggerFactory.getLogger(LegacyStudyAdapter.class);

    private final StudyDAO studyDao;

    @SuppressWarnings("unchecked")
    public LegacyStudyAdapter(DataSource dataSource) {
        this.studyDao = new StudyDAO(dataSource);
    }

    /**
     * Find a single study by its primary key via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public StudyDetailDTO findStudyById(int studyId) {
        StudyBean bean = (StudyBean) studyDao.findByPK(studyId);
        if (bean == null) {
            return null;
        }
        return toDetail(bean);
    }

    /**
     * Find all top-level studies (parentStudyId IS NULL) via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public List<StudySummaryDTO> findAllStudies() {
        List<StudySummaryDTO> result = new ArrayList<>();
        for (Object obj : studyDao.findAll()) {
            StudyBean bean = (StudyBean) obj;
            if (bean.getParentStudyId() == 0) {
                result.add(toSummary(bean));
            }
        }
        return result;
    }

    /**
     * Find all sites (children) for a given parent study via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public List<StudySummaryDTO> findSitesByStudyId(int parentStudyId) {
        List<StudySummaryDTO> result = new ArrayList<>();
        for (Object obj : studyDao.findAllByParent(parentStudyId)) {
            StudyBean bean = (StudyBean) obj;
            result.add(toSummary(bean));
        }
        return result;
    }

    /**
     * Check if a study exists in the legacy system.
     */
    public boolean existsInLegacy(int studyId) {
        return studyDao.findByPK(studyId) != null;
    }

    /**
     * Count the number of sites under a parent study via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public int countSites(int parentStudyId) {
        return studyDao.findAllByParent(parentStudyId).size();
    }

    /**
     * Search studies by name (containing, case-insensitive) via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public List<StudySummaryDTO> searchByName(String name) {
        List<StudySummaryDTO> result = new ArrayList<>();
        for (Object obj : studyDao.findAll()) {
            StudyBean bean = (StudyBean) obj;
            if (bean.getName() != null && bean.getName().toLowerCase().contains(name.toLowerCase())) {
                result.add(toSummary(bean));
            }
        }
        return result;
    }

    /**
     * Find study by unique identifier via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public StudyDetailDTO findByUniqueIdentifier(String uniqueIdentifier) {
        StudyBean bean = (StudyBean) studyDao.findByUniqueIdentifier(uniqueIdentifier);
        if (bean == null) {
            return null;
        }
        return toDetail(bean);
    }

    /**
     * Find study by OID via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public StudyDetailDTO findByOcOid(String ocOid) {
        StudyBean bean = (StudyBean) studyDao.findByOid(ocOid);
        if (bean == null) {
            return null;
        }
        return toDetail(bean);
    }

    // ──────────────────────────────────────────────────────────
    //  Mapping helpers
    // ──────────────────────────────────────────────────────────

    private StudySummaryDTO toSummary(StudyBean bean) {
        StudySummaryDTO dto = new StudySummaryDTO();
        dto.setStudyId(bean.getId());
        dto.setParentStudyId(bean.getParentStudyId());
        dto.setSite(bean.getParentStudyId() > 0);
        dto.setName(bean.getName());
        dto.setUniqueIdentifier(bean.getIdentifier());
        dto.setOcOid(bean.getOid());
        dto.setPhase(bean.getPhase());
        dto.setPrincipalInvestigator(bean.getPrincipalInvestigator());
        dto.setSponsor(bean.getSponsor());
        dto.setStatus(bean.getStatus() != null ? bean.getStatus().getName() : null);
        dto.setDateCreated(toLocalDateTime(bean.getCreatedDate()));
        dto.setExpectedTotalEnrollment(bean.getExpectedTotalEnrollment());
        return dto;
    }

    private StudyDetailDTO toDetail(StudyBean bean) {
        StudyDetailDTO dto = new StudyDetailDTO();
        dto.setStudyId(bean.getId());
        dto.setParentStudyId(bean.getParentStudyId());
        dto.setSite(bean.getParentStudyId() > 0);
        dto.setName(bean.getName());
        dto.setUniqueIdentifier(bean.getIdentifier());
        dto.setSecondaryIdentifier(bean.getSecondaryIdentifier());
        dto.setOcOid(bean.getOid());
        dto.setOfficialTitle(bean.getOfficialTitle());
        dto.setSummary(bean.getSummary());
        dto.setPhase(bean.getPhase());
        dto.setPrincipalInvestigator(bean.getPrincipalInvestigator());
        dto.setSponsor(bean.getSponsor());
        dto.setCollaborators(bean.getCollaborators());
        dto.setStatus(bean.getStatus() != null ? bean.getStatus().getName() : null);
        dto.setTypeId(bean.getTypeId());
        dto.setFacilityName(bean.getFacilityName());
        dto.setFacilityCity(bean.getFacilityCity());
        dto.setFacilityState(bean.getFacilityState());
        dto.setFacilityCountry(bean.getFacilityCountry());
        dto.setDatePlannedStart(toLocalDateTime(bean.getDatePlannedStart()));
        dto.setDatePlannedEnd(toLocalDateTime(bean.getDatePlannedEnd()));
        dto.setDateCreated(toLocalDateTime(bean.getCreatedDate()));
        dto.setDateUpdated(toLocalDateTime(bean.getUpdatedDate()));
        dto.setOwnerId(bean.getOwnerId());
        dto.setExpectedTotalEnrollment(bean.getExpectedTotalEnrollment());
        dto.setProtocolType(bean.getProtocolType());
        dto.setProtocolDescription(bean.getProtocolDescription());
        dto.setConditions(bean.getConditions());
        dto.setKeywords(bean.getKeywords());
        dto.setEligibility(bean.getEligibility());
        dto.setGender(bean.getGender());
        dto.setPurpose(bean.getPurpose());
        dto.setAllocation(bean.getAllocation());
        dto.setMasking(bean.getMasking());
        return dto;
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }
}
