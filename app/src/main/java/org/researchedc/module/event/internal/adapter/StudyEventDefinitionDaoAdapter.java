package org.researchedc.module.event.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.app.dto.StudyDto;
import org.researchedc.app.dto.StudyEventDefinitionDto;
import org.researchedc.app.dto.Status;
import org.researchedc.module.dataimport.service.ImportStudyEventDefinitionPort;
import org.researchedc.module.dataimport.dto.ImportStudyEventDefinition;
import org.researchedc.module.event.entity.StudyEventDefinitionEntity;
import org.researchedc.module.event.repository.StudyEventDefinitionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("studyEventDefinitionDAO")
@Primary
@Transactional(readOnly = true)
public class StudyEventDefinitionDaoAdapter implements ImportStudyEventDefinitionPort {

    private final StudyEventDefinitionRepository repository;

    public StudyEventDefinitionDaoAdapter(StudyEventDefinitionRepository repository) {
        this.repository = repository;
    }

    public StudyEventDefinitionDto findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(StudyEventDefinitionDto::new);
    }

    public StudyEventDefinitionDto findByPKAndStudy(int id, StudyDto study) {
        return repository.findById(id)
                .filter(e -> e.getStudyId() != null && e.getStudyId().equals(study.getId()))
                .map(this::toBean)
                .orElseGet(StudyEventDefinitionDto::new);
    }

    @Transactional
    public StudyEventDefinitionDto create(StudyEventDefinitionDto dto) {
        StudyEventDefinitionEntity entity = new StudyEventDefinitionEntity();
        apply(dto, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Transactional
    public StudyEventDefinitionDto update(StudyEventDefinitionDto dto) {
        StudyEventDefinitionEntity entity = repository.findById(dto.getId())
                .orElseGet(StudyEventDefinitionEntity::new);
        entity.setStudyEventDefinitionId(dto.getId() > 0 ? dto.getId() : null);
        apply(dto, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    public Collection findAll() {
        return toBeans(repository.findAll());
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn,
                                          boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    public Object getEntityFromHashMap(HashMap hm) {
        StudyEventDefinitionEntity entity = new StudyEventDefinitionEntity();
        entity.setStudyEventDefinitionId((Integer) hm.get("study_event_definition_id"));
        entity.setStudyId((Integer) hm.get("study_id"));
        entity.setName((String) hm.get("name"));
        entity.setDescription((String) hm.get("description"));
        entity.setRepeating((Boolean) hm.get("repeating"));
        entity.setType((String) hm.get("type"));
        entity.setCategory((String) hm.get("category"));
        entity.setOcOid((String) hm.get("oc_oid"));
        entity.setOrdinal((Integer) hm.get("ordinal"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        return toBean(entity);
    }

    public StudyEventDefinitionDto findByOid(String oid) {
        return repository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    public StudyEventDefinitionDto findByOidAndStudy(String oid, int studyId, int parentStudyId) {
        StudyEventDefinitionDto bean = findByOidAndStudy(oid, studyId);
        if (bean == null) {
            bean = findByOidAndStudy(oid, parentStudyId);
        }
        return bean;
    }

    public ImportStudyEventDefinition findImportStudyEventDefinitionByOidAndStudy(
            String oid, int studyId, int parentStudyId) {
        StudyEventDefinitionEntity entity = repository.findByOcOidAndStudyId(oid, studyId)
                .or(() -> repository.findByOcOidAndStudyId(oid, parentStudyId))
                .orElse(null);
        if (entity == null) {
            return null;
        }
        return new ImportStudyEventDefinition(entity.getStudyEventDefinitionId(), entity.getName());
    }

    private StudyEventDefinitionDto findByOidAndStudy(String oid, int studyId) {
        return repository.findByOcOidAndStudyId(oid, studyId)
                .map(this::toBean)
                .orElse(null);
    }

    public ArrayList findAllByStudy(StudyDto study) {
        if (study.getParentStudyId() > 0) {
            return toBeans(repository.findByStudyIdOrderByName(study.getParentStudyId()));
        }
        return toBeans(repository.findByStudyIdOrderByName(study.getId()));
    }

    public ArrayList findAllWithStudyEvent(StudyDto currentStudy) {
        return new ArrayList();
    }

    public ArrayList<StudyEventDefinitionDto> findAllByCrf(Object crf) {
        return new ArrayList<StudyEventDefinitionDto>();
    }

    public StudyEventDefinitionDto findByName(String name) {
        List<StudyEventDefinitionEntity> results = repository.findByName(name);
        if (results.isEmpty()) {
            return new StudyEventDefinitionDto();
        }
        return toBean(results.get(0));
    }

    public StudyEventDefinitionDto findByEventDefinitionCRFId(int eventDefinitionCRFId) {
        return repository.findByEventDefinitionCRFId(eventDefinitionCRFId)
                .map(this::toBean)
                .orElseGet(StudyEventDefinitionDto::new);
    }

    public Collection findAllByStudyAndLimit(int studyId) {
        return toBeans(repository.findByStudyIdOrderByName(studyId));
    }

    public ArrayList<StudyEventDefinitionDto> findAllActiveByParentStudyId(int parentStudyId) {
        return toBeans(repository.findByStatusIdAndStudyId(Status.AVAILABLE.getId(), parentStudyId));
    }

    public ArrayList findAllActiveByStudy(StudyDto study) {
        if (study.getParentStudyId() > 0) {
            return toBeans(repository.findByStatusIdAndStudyId(Status.AVAILABLE.getId(), study.getParentStudyId()));
        }
        return toBeans(repository.findByStatusIdAndStudyId(Status.AVAILABLE.getId(), study.getId()));
    }

    private void apply(StudyEventDefinitionDto dto, StudyEventDefinitionEntity entity) {
        entity.setStudyId(dto.getStudyId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setRepeating(dto.isRepeating());
        entity.setType(dto.getType());
        entity.setCategory(dto.getCategory());
        entity.setOcOid(dto.getOid());
        entity.setOrdinal(dto.getOrdinal());
        entity.setStatusId(dto.getStatus() != null ? dto.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(dto.getOwnerId());
        entity.setUpdateId(dto.getUpdaterId());
    }

    private ArrayList<StudyEventDefinitionDto> toBeans(List<StudyEventDefinitionEntity> entities) {
        ArrayList<StudyEventDefinitionDto> dtos = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(StudyEventDefinitionEntity::getStudyEventDefinitionId,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    private StudyEventDefinitionDto toBean(StudyEventDefinitionEntity entity) {
        StudyEventDefinitionDto dto = new StudyEventDefinitionDto();
        if (entity.getStudyEventDefinitionId() != null) {
            dto.setId(entity.getStudyEventDefinitionId());
        }
        dto.setStudyId(valueOrZero(entity.getStudyId()));
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setRepeating(entity.getRepeating() != null && entity.getRepeating());
        dto.setType(entity.getType());
        dto.setCategory(entity.getCategory());
        dto.setOid(entity.getOcOid());
        dto.setOrdinal(valueOrZero(entity.getOrdinal()));
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
