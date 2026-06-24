package org.researchedc.module.event.internal.adapter;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.researchedc.app.dto.Status;
import org.researchedc.app.dto.StudyDto;
import org.researchedc.app.dto.StudyEventDefinitionDto;
import org.researchedc.app.dto.StudyEventDto;
import org.researchedc.module.dataimport.service.ImportStudyEventPort;
import org.researchedc.module.dataimport.dto.ImportStudyEvent;
import org.researchedc.app.dto.StudySubjectDTO;
import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("studyEventDAO")
@Primary
@Transactional(readOnly = true)
public class StudyEventDaoAdapter implements ImportStudyEventPort {
    private static final int SUBJECT_EVENT_STATUS_SCHEDULED = 1;

    private final StudyEventRepository studyEventRepository;

    public StudyEventDaoAdapter(StudyEventRepository studyEventRepository) {
        this.studyEventRepository = studyEventRepository;
    }

    public StudyEventDto findByPK(int ID) {
        return studyEventRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(StudyEventDto::new);
    }

    public StudyEventDto findByPKCached(int ID) {
        return findByPK(ID);
    }

    @Transactional
    public StudyEventDto create(StudyEventDto dto) {
        StudyEventEntity entity = new StudyEventEntity();
        apply(dto, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(studyEventRepository.save(entity));
    }

    @Transactional
    public StudyEventDto create(StudyEventDto dto, boolean isTransaction) {
        return create(dto);
    }

    @Transactional
    public StudyEventDto update(StudyEventDto dto) {
        StudyEventEntity entity = studyEventRepository.findById(dto.getId())
                .orElseGet(StudyEventEntity::new);
        if (dto.getId() > 0) {
            entity.setStudyEventId(dto.getId());
        }
        apply(dto, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(studyEventRepository.save(entity));
    }

    @Transactional
    public StudyEventDto update(StudyEventDto dto, boolean isTransaction) {
        return update(dto);
    }

    @Transactional
    public StudyEventDto update(StudyEventDto dto, Connection con) {
        return update(dto);
    }

    @Transactional
    public StudyEventDto update(StudyEventDto dto, Connection con, boolean isTransaction) {
        return update(dto);
    }

    public Collection findAll() {
        return toBeans(studyEventRepository.findAll());
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
        StudyEventEntity entity = new StudyEventEntity();
        entity.setStudyEventId((Integer) hm.get("study_event_id"));
        entity.setStudySubjectId((Integer) hm.get("study_subject_id"));
        entity.setStudyEventDefinitionId((Integer) hm.get("study_event_definition_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setSubjectEventStatusId((Integer) hm.get("subject_event_status_id"));
        entity.setLocation((String) hm.get("location"));
        entity.setSampleOrdinal((Integer) hm.get("sample_ordinal"));
        entity.setDateStart(toLocalDateTime((Date) hm.get("date_start")));
        entity.setDateEnd(toLocalDateTime((Date) hm.get("date_end")));
        entity.setStartTimeFlag((Boolean) hm.get("start_time_flag"));
        entity.setEndTimeFlag((Boolean) hm.get("end_time_flag"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setSedOrdinal((Integer) hm.get("sed_ordinal"));
        return toBean(entity);
    }

    public Collection findAllByDefinition(int definitionId) {
        return toBeans(studyEventRepository.findByStudyEventDefinitionId(definitionId));
    }

    public ArrayList findAllByStudyEventDefinitionAndCrfOids(String studyEventDefinitionOid, String crfOrCrfVersionOid) {
        return new ArrayList();
    }

    public ArrayList findAllWithSubjectLabelByDefinition(int definitionId) {
        return new ArrayList();
    }

    public ArrayList findAllWithSubjectLabelByStudySubjectAndDefinition(StudySubjectDTO studySubject, int definitionId) {
        return new ArrayList();
    }

    public StudyEventDto findByStudySubjectIdAndDefinitionIdAndOrdinal(int ssbid, int sedid, int ord) {
        List<StudyEventEntity> results = studyEventRepository
                .findByStudySubjectIdAndStudyEventDefinitionIdAndSampleOrdinal(ssbid, sedid, ord);
        if (results.isEmpty()) {
            return new StudyEventDto();
        }
        return toBean(results.get(0));
    }

    public ImportStudyEvent findImportStudyEventBySubjectDefinitionOrdinal(
            int studySubjectId, int studyEventDefinitionId, int ordinal) {
        return studyEventRepository
                .findByStudySubjectIdAndStudyEventDefinitionIdAndSampleOrdinal(
                        studySubjectId, studyEventDefinitionId, ordinal)
                .stream()
                .findFirst()
                .map(entity -> new ImportStudyEvent(
                        entity.getStudyEventId(),
                        entity.getSubjectEventStatusId(),
                        entity.getLocation()))
                .orElse(null);
    }

    public ArrayList findAllByDefinitionAndSubject(StudyEventDefinitionDto definition, StudySubjectDTO subject) {
        return toBeans(studyEventRepository
                .findByStudyEventDefinitionIdAndStudySubjectId(definition.getId(), subject.getId()));
    }

    public ArrayList findAllByDefinitionAndSubjectOrderByOrdinal(StudyEventDefinitionDto definition, StudySubjectDTO subject) {
        return toBeans(studyEventRepository
                .findByStudyEventDefinitionIdAndStudySubjectIdOrderBySampleOrdinal(definition.getId(), subject.getId()));
    }

    public ArrayList findAllByStudyAndStudySubjectId(StudyDto study, int studySubjectId) {
        return toBeans(studyEventRepository.findByStudySubjectId(studySubjectId));
    }

    public ArrayList findAllByStudyAndEventDefinitionId(StudyDto study, int eventDefinitionId) {
        return toBeans(studyEventRepository.findByStudyEventDefinitionId(eventDefinitionId));
    }

    public int getMaxSampleOrdinal(StudyEventDefinitionDto sedb, StudySubjectDTO studySubject) {
        Optional<StudyEventEntity> result = studyEventRepository
                .findTopByStudyEventDefinitionIdAndStudySubjectIdOrderBySampleOrdinalDesc(
                        sedb.getId(), studySubject.getId());
        return result.map(e -> valueOrZero(e.getSampleOrdinal())).orElse(0);
    }

    public StudyEventDto findByPKAndStudy(int id, StudyDto study) {
        return findByPK(id);
    }

    public ArrayList findAllByStudy(StudyDto study) {
        return new ArrayList();
    }

    public ArrayList findAllBySubjectAndStudy(int subjectId, int studyId) {
        return new ArrayList();
    }

    public ArrayList findAllBySubjectId(int subjectId) {
        return toBeans(studyEventRepository.findByStudySubjectId(subjectId));
    }

    public ArrayList findAllBySubjectIdOrdered(int subjectId) {
        return toBeans(studyEventRepository.findByStudySubjectIdOrderByDateStart(subjectId));
    }

    public HashMap findCRFsByStudy(StudyDto sb) {
        return new HashMap();
    }

    public HashMap findCRFsByStudyEvent(StudyEventDto seb) {
        return new HashMap();
    }

    public int getDefinitionIdFromStudyEventId(int studyEventId) {
        return studyEventRepository.findById(studyEventId)
                .map(e -> valueOrZero(e.getStudyEventDefinitionId()))
                .orElse(0);
    }

    public StudyEventDto getNextScheduledEvent(String studySubjectOID) {
        return new StudyEventDto();
    }

    public ArrayList findAllByStudySubject(StudySubjectDTO ssb) {
        return toBeans(studyEventRepository.findByStudySubjectId(ssb.getId()));
    }

    public ArrayList findAllByStudySubjectAndDefinition(StudySubjectDTO ssb, StudyEventDefinitionDto sed) {
        return new ArrayList();
    }

    public Integer countNotRemovedEvents(Integer studyEventDefinitionId) {
        return 0;
    }

    public int getCurrentPK() {
        return 0;
    }

    public Integer getCountofEventsBasedOnEventStatus(StudyDto currentStudy, int subjectEventStatusId) {
        return 0;
    }

    public Integer getCountofEvents(StudyDto currentStudy) {
        return 0;
    }

    public StudyEventDto findAllByStudyEventDefinitionAndCrfOidsAndOrdinal(
            String studyEventDefinitionOid, String crfOrCrfVersionOid, String ordinal, String studySubjectId) {
        return new StudyEventDto();
    }

    public HashMap getStudySubjectCRFData(StudyDto sb, int studySubjectId, int eventDefId,
                                           String crfVersionOid) {
        return new HashMap();
    }

    public boolean isThisRepeatingEventScheduledMoreThanOneTime(int studyId, int sed_Id) {
        return false;
    }

    // --- Private helpers ---

    private void apply(StudyEventDto dto, StudyEventEntity entity) {
        entity.setStudyEventDefinitionId(dto.getStudyEventDefinitionId());
        entity.setStudySubjectId(dto.getStudySubjectId());
        entity.setStatusId(dto.getStatus() != null ? dto.getStatus().getId() : Status.INVALID.getId());
        entity.setSubjectEventStatusId(
                dto.getSubjectEventStatusId() != 0 ? dto.getSubjectEventStatusId() : SUBJECT_EVENT_STATUS_SCHEDULED);
        entity.setLocation(dto.getLocation());
        entity.setSampleOrdinal(dto.getSampleOrdinal());
        entity.setDateStart(toLocalDateTime(dto.getDateStarted()));
        entity.setDateEnd(toLocalDateTime(dto.getDateEnded()));
        entity.setStartTimeFlag(dto.getStartTimeFlag());
        entity.setEndTimeFlag(dto.getEndTimeFlag());
        entity.setOwnerId(dto.getOwnerId());
        entity.setUpdateId(dto.getUpdaterId());
    }

    private ArrayList<StudyEventDto> toBeans(List<StudyEventEntity> entities) {
        ArrayList<StudyEventDto> dtos = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(StudyEventEntity::getStudyEventId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    private StudyEventDto toBean(StudyEventEntity entity) {
        StudyEventDto dto = new StudyEventDto();
        if (entity.getStudyEventId() != null) {
            dto.setId(entity.getStudyEventId());
        }
        dto.setStudySubjectId(valueOrZero(entity.getStudySubjectId()));
        dto.setStudyEventDefinitionId(valueOrZero(entity.getStudyEventDefinitionId()));
        dto.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        dto.setSubjectEventStatusId(valueOrZero(entity.getSubjectEventStatusId()));
        dto.setLocation(entity.getLocation() != null ? entity.getLocation() : "");
        dto.setSampleOrdinal(valueOrZero(entity.getSampleOrdinal()));
        dto.setDateStarted(toDate(entity.getDateStart()));
        dto.setDateEnded(toDate(entity.getDateEnd()));
        dto.setStartTimeFlag(entity.getStartTimeFlag() != null && entity.getStartTimeFlag());
        dto.setEndTimeFlag(entity.getEndTimeFlag() != null && entity.getEndTimeFlag());
        dto.setCreatedDate(toDate(entity.getDateCreated()));
        dto.setUpdatedDate(toDate(entity.getDateUpdated()));
        dto.setOwnerId(valueOrZero(entity.getOwnerId()));
        dto.setUpdaterId(valueOrZero(entity.getUpdateId()));
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
