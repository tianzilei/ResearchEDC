package org.researchedc.module.subject.infrastructure;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.managestudy.StudySubjectDAO;
import org.researchedc.dao.submit.SubjectDAO;
import org.researchedc.module.subject.dto.StudySubjectDTO;
import org.researchedc.module.subject.dto.SubjectDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Anti-corruption layer adapter that bridges the Subject module to legacy
 * {@code core.dao.*} and {@code core.bean.*} classes.
 *
 * <p>This is the ONLY class in the Subject module (outside the entity/repository layer)
 * that may import from legacy packages. All legacy data access goes through this adapter.</p>
 *
 * <p>Follows the same pattern as {@code crf/internal/adapter/LegacyCrfAdapter}.</p>
 */
@Component
public class LegacySubjectAdapter {

    private static final Logger log = LoggerFactory.getLogger(LegacySubjectAdapter.class);

    private final SubjectDAO subjectDao;
    private final StudySubjectDAO studySubjectDao;
    private final StudyDAO studyDao;

    @SuppressWarnings("unchecked")
    public LegacySubjectAdapter(DataSource dataSource) {
        this.subjectDao = new SubjectDAO(dataSource);
        this.studySubjectDao = new StudySubjectDAO(dataSource);
        this.studyDao = new StudyDAO(dataSource);
    }

    /**
     * Find a single subject by its primary key via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public SubjectDTO findSubjectById(int subjectId) {
        SubjectBean bean = (SubjectBean) subjectDao.findByPK(subjectId);
        if (bean == null) {
            return null;
        }
        return toSubjectDto(bean);
    }

    /**
     * Find all subjects via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public List<SubjectDTO> findAllSubjects() {
        List<SubjectDTO> result = new ArrayList<>();
        for (Object obj : subjectDao.findAll()) {
            SubjectBean bean = (SubjectBean) obj;
            result.add(toSubjectDto(bean));
        }
        return result;
    }

    /**
     * Find a subject by unique identifier via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public SubjectDTO findByUniqueIdentifier(String uniqueIdentifier) {
        SubjectBean bean = (SubjectBean) subjectDao.findByUniqueIdentifier(uniqueIdentifier);
        if (bean == null) {
            return null;
        }
        return toSubjectDto(bean);
    }

    /**
     * Find a study-subject enrollment record by its primary key via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public StudySubjectDTO findStudySubjectById(int studySubjectId) {
        StudySubjectBean bean = (StudySubjectBean) studySubjectDao.findByPK(studySubjectId);
        if (bean == null) {
            return null;
        }
        return toStudySubjectDto(bean);
    }

    /**
     * Find all study-subject enrollments for a given study via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public List<StudySubjectDTO> findStudySubjectsByStudyId(int studyId) {
        List<StudySubjectDTO> result = new ArrayList<>();
        for (Object obj : studySubjectDao.findAllByStudyId(studyId)) {
            StudySubjectBean bean = (StudySubjectBean) obj;
            result.add(toStudySubjectDto(bean));
        }
        return result;
    }

    /**
     * Find all study-subject enrollments for a given subject via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public List<StudySubjectDTO> findStudySubjectsBySubjectId(int subjectId) {
        List<StudySubjectDTO> result = new ArrayList<>();
        for (Object obj : studySubjectDao.findAllBySubjectId(subjectId)) {
            StudySubjectBean bean = (StudySubjectBean) obj;
            result.add(toStudySubjectDto(bean));
        }
        return result;
    }

    /**
     * Find a study-subject enrollment by its OID via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public StudySubjectDTO findStudySubjectByOcOid(String ocOid) {
        StudySubjectBean bean = (StudySubjectBean) studySubjectDao.findByOid(ocOid);
        if (bean == null) {
            return null;
        }
        return toStudySubjectDto(bean);
    }

    /**
     * Count the number of subjects enrolled in a study via legacy DAO.
     */
    @SuppressWarnings("unchecked")
    public int countStudySubjects(int studyId) {
        StudyBean study = (StudyBean) studyDao.findByPK(studyId);
        if (study == null) {
            return 0;
        }
        return studySubjectDao.getCountofStudySubjects(study);
    }

    // ──────────────────────────────────────────────────────────
    //  Mapping helpers
    // ──────────────────────────────────────────────────────────

    private SubjectDTO toSubjectDto(SubjectBean bean) {
        SubjectDTO dto = new SubjectDTO();
        dto.setSubjectId(bean.getId());
        dto.setUniqueIdentifier(bean.getUniqueIdentifier());
        dto.setDateOfBirth(toLocalDateTime(bean.getDateOfBirth()));
        dto.setGender(String.valueOf(bean.getGender()));
        dto.setDobCollected(bean.isDobCollected());
        dto.setDateCreated(toLocalDateTime(bean.getCreatedDate()));
        return dto;
    }

    private StudySubjectDTO toStudySubjectDto(StudySubjectBean bean) {
        StudySubjectDTO dto = new StudySubjectDTO();
        dto.setStudySubjectId(bean.getId());
        dto.setStudyId(bean.getStudyId());
        dto.setSubjectId(bean.getSubjectId());
        dto.setLabel(bean.getLabel());
        dto.setSecondaryLabel(bean.getSecondaryLabel());
        dto.setOcOid(bean.getOid());
        dto.setEnrollmentDate(toLocalDateTime(bean.getEnrollmentDate()));
        dto.setDateCreated(toLocalDateTime(bean.getCreatedDate()));
        dto.setDateUpdated(toLocalDateTime(bean.getUpdatedDate()));
        return dto;
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }
}
