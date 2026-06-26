package org.researchedc.module.subject.internal.adapter;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.researchedc.app.dto.Status;
import org.researchedc.module.dataimport.service.ImportStudySubjectPort;
import org.researchedc.module.dataimport.dto.ImportStudySubject;
import org.researchedc.app.dto.StudyDto;
import org.researchedc.app.dto.StudySubjectDTO;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("studySubjectDAO")
@Primary
@Transactional(readOnly = true)
public class StudySubjectDaoAdapter implements ImportStudySubjectPort {

    private final StudySubjectRepository repository;

    public StudySubjectDaoAdapter(StudySubjectRepository repository) {
        this.repository = repository;
    }

    public StudySubjectDTO findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(StudySubjectDTO::new);
    }

    public ArrayList findAllByStudy(StudyDto study) {
        return toBeans(repository.findByStudyId(study.getId()));
    }

    @Transactional
    public StudySubjectDTO create(StudySubjectDTO dto) {
        StudySubjectEntity entity = new StudySubjectEntity();
        apply(dto, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Transactional
    public StudySubjectDTO update(StudySubjectDTO dto) {
        StudySubjectEntity entity = repository.findById(dto.getId())
                .orElseGet(StudySubjectEntity::new);
        entity.setStudySubjectId(dto.getId() > 0 ? dto.getId() : null);
        apply(dto, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    public Collection findAll() {
        return toBeans(repository.findAll());
    }

    public Object getEntityFromHashMap(HashMap hm) {
        StudySubjectEntity entity = new StudySubjectEntity();
        entity.setStudySubjectId((Integer) hm.get("study_subject_id"));
        entity.setStudyId((Integer) hm.get("study_id"));
        entity.setSubjectId((Integer) hm.get("subject_id"));
        entity.setLabel((String) hm.get("label"));
        entity.setSecondaryLabel((String) hm.get("secondary_label"));
        entity.setEnrollmentDate(toLocalDateTime((Date) hm.get("enrollment_date")));
        entity.setOcOid((String) hm.get("oc_oid"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        return toBean(entity);
    }

    public ArrayList findAllByStudyOrderByLabel(StudyDto sb) {
        return toBeans(repository.findByStudyIdOrderByLabel(sb.getId()));
    }

    public ArrayList findAllActiveByStudyOrderByLabel(StudyDto sb) {
        return toBeans(repository.findByStudyIdAndStatusIdOrderByLabel(sb.getId(), Status.AVAILABLE.getId()));
    }

    public ArrayList findAllBySubjectId(int subjectId) {
        return toBeans(repository.findBySubjectId(subjectId));
    }

    public StudySubjectDTO findAnotherBySameLabel(String label, int studyId, int studySubjectId) {
        return repository.findByLabelAndStudyId(label, studyId).stream()
                .filter(e -> !e.getStudySubjectId().equals(studySubjectId))
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public StudySubjectDTO findAnotherBySameLabelInSites(String label, int studyId, int studySubjectId) {
        return repository.findByLabelContainingIgnoreCase(label).stream()
                .filter(e -> !e.getStudySubjectId().equals(studySubjectId))
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public StudySubjectDTO findSameByLabelAndStudy(String label, int studyId, int id) {
        return repository.findByLabelAndStudyId(label, studyId).stream()
                .filter(e -> !e.getStudySubjectId().equals(id))
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public StudySubjectDTO findByOidAndStudy(String oid, int studyId) {
        return repository.findByOcOidAndStudyId(oid, studyId)
                .map(this::toBean)
                .orElse(null);
    }

    public ImportStudySubject findImportStudySubjectByOidAndStudy(String oid, int studyId) {
        return repository.findByOcOidAndStudyId(oid, studyId)
                .map(entity -> new ImportStudySubject(entity.getStudySubjectId(), entity.getLabel()))
                .orElse(null);
    }

    public StudySubjectDTO findByOid(String oid) {
        return repository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    public String findStudySubjectIdsByStudyIds(String studyIds) {
        if (studyIds == null || studyIds.isEmpty()) return "";
        List<StudySubjectEntity> results = new ArrayList<>();
        for (String idStr : studyIds.split(",")) {
            try {
                results.addAll(repository.findByStudyId(Integer.parseInt(idStr.trim())));
            } catch (NumberFormatException ignored) {
            }
        }
        return results.stream()
                .map(e -> String.valueOf(e.getStudySubjectId()))
                .collect(Collectors.joining(","));
    }

    public StudySubjectDTO findBySubjectIdAndStudy(int subjectId, StudyDto study) {
        return repository.findBySubjectIdAndStudyId(subjectId, study.getId())
                .map(this::toBean)
                .orElse(null);
    }

    public ArrayList findAllByStudyId(int studyId) {
        return toBeans(repository.findByStudyId(studyId));
    }

    public ArrayList findAllByStudyIdAndLimit(int studyId, boolean isLimited) {
        return toBeans(repository.findByStudyId(studyId));
    }

    public int findTheGreatestLabel() {
        return repository.findTopByOrderByStudySubjectIdDesc()
                .map(e -> {
                    try {
                        return Integer.parseInt(e.getLabel());
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                })
                .orElse(0);
    }

    @Transactional
    public StudySubjectDTO create(StudySubjectDTO sb, boolean withGroup) {
        return create(sb);
    }

    @Transactional
    public StudySubjectDTO createWithGroup(StudySubjectDTO sb) {
        return create(sb, true);
    }

    @Transactional
    public StudySubjectDTO createWithoutGroup(StudySubjectDTO sb) {
        return create(sb, false);
    }

    @Transactional
    public StudySubjectDTO update(StudySubjectDTO dto, Connection con) {
        return update(dto);
    }

    public Integer getCountofStudySubjectsAtStudyOrSite(StudyDto currentStudy) {
        return (int) repository.countByStudyId(currentStudy.getId());
    }

    public Integer getCountofStudySubjectsAtStudy(StudyDto currentStudy) {
        return (int) repository.countByStudyId(currentStudy.getId());
    }

    public Integer getCountofStudySubjects(StudyDto currentStudy) {
        return (int) repository.countByStudyId(currentStudy.getId());
    }

    public Integer getCountofStudySubjectsBasedOnStatus(StudyDto currentStudy, Status status) {
        return (int) repository.countByStudyIdAndStatusId(currentStudy.getId(), status.getId());
    }

    private void apply(StudySubjectDTO dto, StudySubjectEntity entity) {
        entity.setStudyId(dto.getStudyId());
        entity.setSubjectId(dto.getSubjectId());
        entity.setLabel(dto.getLabel());
        entity.setSecondaryLabel(dto.getSecondaryLabel());
        entity.setEnrollmentDate(dto.getEnrollmentDate());
        entity.setOcOid(dto.getOid());
        entity.setOwnerId(dto.getOwnerId());
        entity.setUpdateId(dto.getUpdaterId());
        entity.setStatusId(dto.getStatus() != null ? dto.getStatus().getId() : Status.AVAILABLE.getId());
    }

    private ArrayList<StudySubjectDTO> toBeans(List<StudySubjectEntity> entities) {
        ArrayList<StudySubjectDTO> dtos = new ArrayList<>();
        entities.stream()
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    private StudySubjectDTO toBean(StudySubjectEntity entity) {
        StudySubjectDTO dto = new StudySubjectDTO();
        if (entity.getStudySubjectId() != null) {
            dto.setId(entity.getStudySubjectId());
        }
        dto.setStudyId(valueOrZero(entity.getStudyId()));
        dto.setSubjectId(valueOrZero(entity.getSubjectId()));
        dto.setLabel(entity.getLabel() != null ? entity.getLabel() : "");
        dto.setSecondaryLabel(entity.getSecondaryLabel() != null ? entity.getSecondaryLabel() : "");
        dto.setEnrollmentDate(entity.getEnrollmentDate());
        dto.setOid(entity.getOcOid());
        dto.setCreatedDate(toDate(entity.getDateCreated()));
        dto.setUpdatedDate(toDate(entity.getDateUpdated()));
        dto.setOwnerId(valueOrZero(entity.getOwnerId()));
        dto.setUpdaterId(valueOrZero(entity.getUpdateId()));
        dto.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        return dto;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return new Date(0);
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }
}
