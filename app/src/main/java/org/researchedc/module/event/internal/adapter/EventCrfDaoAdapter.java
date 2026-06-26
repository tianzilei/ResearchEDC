package org.researchedc.module.event.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.researchedc.app.dto.EventCrfDto;
import org.researchedc.app.dto.Status;
import org.researchedc.app.dto.StudyDto;
import org.researchedc.app.dto.StudyEventDto;
import org.researchedc.app.dto.CrfVersionDTO;
import org.researchedc.module.dataimport.service.ImportEventCrfPort;
import org.researchedc.module.dataimport.dto.ImportEventCrf;
import org.researchedc.app.dto.StudySubjectDTO;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("eventCRFDAO")
@Primary
@Transactional(readOnly = true)
public class EventCrfDaoAdapter implements ImportEventCrfPort {

    private final EventCrfRepository eventCrfRepository;

    public EventCrfDaoAdapter(EventCrfRepository eventCrfRepository) {
        this.eventCrfRepository = eventCrfRepository;
    }

    public EventCrfDto findByPK(int ID) {
        return eventCrfRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(EventCrfDto::new);
    }

    @Transactional
    public EventCrfDto create(EventCrfDto dto) {
        EventCrfEntity entity = new EventCrfEntity();
        apply(dto, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(eventCrfRepository.save(entity));
    }

    @Transactional
    public EventCrfDto update(EventCrfDto dto) {
        EventCrfEntity entity = eventCrfRepository.findById(dto.getId())
                .orElseGet(EventCrfEntity::new);
        entity.setEventCrfId(dto.getId() > 0 ? dto.getId() : null);
        apply(dto, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(eventCrfRepository.save(entity));
    }

    public Collection findAll() {
        return toBeans(eventCrfRepository.findAll());
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
        EventCrfEntity entity = new EventCrfEntity();
        entity.setEventCrfId((Integer) hm.get("event_crf_id"));
        entity.setStudyEventId((Integer) hm.get("study_event_id"));
        entity.setStudySubjectId((Integer) hm.get("study_subject_id"));
        entity.setCrfVersionId((Integer) hm.get("crf_version_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setDateInterviewed(toLocalDateTime((Date) hm.get("date_interviewed")));
        entity.setInterviewerName((String) hm.get("interviewer_name"));
        entity.setAnnotations((String) hm.get("annotations"));
        entity.setDateCompleted(toLocalDateTime((Date) hm.get("date_completed")));
        entity.setValidatorId((Integer) hm.get("validator_id"));
        entity.setDateValidate(toLocalDateTime((Date) hm.get("date_validate")));
        entity.setDateValidateCompleted(toLocalDateTime((Date) hm.get("date_validate_completed")));
        entity.setValidatorAnnotations((String) hm.get("validator_annotations"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setElectronicSignatureStatus((Boolean) hm.get("electronic_signature_status"));
        entity.setSdvStatus((Boolean) hm.get("sdv_status"));
        entity.setOldStatusId((Integer) hm.get("old_status_id"));
        entity.setSdvUpdateId((Integer) hm.get("sdv_update_id"));
        return toBean(entity);
    }

    public ArrayList findAllByStudyEvent(StudyEventDto studyEvent) {
        return toBeans(eventCrfRepository.findByStudyEventId(studyEvent.getId()));
    }

    public ArrayList findAllByStudyEventAndStatus(StudyEventDto studyEvent, Status status) {
        return toBeans(eventCrfRepository.findByStudyEventIdAndStatusId(
                studyEvent.getId(), status.getId()));
    }

    public ArrayList<EventCrfDto> findAllByStudySubject(int studySubjectId) {
        return toBeans(eventCrfRepository.findByStudySubjectId(studySubjectId));
    }

    public ArrayList findAllByStudyEventAndCrfOrCrfVersionOid(StudyEventDto studyEvent, String crfVersionOrCrfOID) {
        try {
            return toBeans(eventCrfRepository.findByStudyEventIdAndCrfVersionId(
                    studyEvent.getId(), Integer.parseInt(crfVersionOrCrfOID)));
        } catch (NumberFormatException e) {
            return new ArrayList();
        }
    }

    public ArrayList<EventCrfDto> findAllByStudyEventInParticipantForm(StudyEventDto studyEvent,
                                                                        int sed_Id, int studyId) {
        return new ArrayList();
    }

    public ArrayList<EventCrfDto> findAllByStudyEventDefinition(int sed_Id, int studyId) {
        return new ArrayList();
    }

    public ArrayList findAllByCRF(int crfId) {
        return new ArrayList();
    }

    public ArrayList findAllByCRFVersion(int versionId) {
        return toBeans(eventCrfRepository.findByCrfVersionId(versionId));
    }

    public ArrayList findAllStudySubjectByCRFVersion(int versionId) {
        return toBeans(eventCrfRepository.findByCrfVersionId(versionId));
    }

    public ArrayList findUndeletedWithStudySubjectsByCRFVersion(int versionId) {
        return toBeans(eventCrfRepository.findByCrfVersionId(versionId).stream()
                .filter(e -> e.getStatusId() == null
                        || (e.getStatusId() != Status.DELETED.getId()
                                && e.getStatusId() != Status.AUTO_DELETED.getId()))
                .collect(Collectors.toList()));
    }

    public ArrayList findByEventSubjectVersion(StudyEventDto studyEvent, StudySubjectDTO studySubject,
                                               CrfVersionDTO crfVersion) {
        return toBeans(eventCrfRepository.findByStudyEventIdAndStudySubjectIdAndCrfVersionId(
                studyEvent.getId(), studySubject.getId(), crfVersion.getId()));
    }

    public List<ImportEventCrf> findImportEventCrfsByEventSubjectVersion(
            int studyEventId, int studySubjectId, int crfVersionId) {
        return eventCrfRepository.findByStudyEventIdAndStudySubjectIdAndCrfVersionId(
                        studyEventId, studySubjectId, crfVersionId).stream()
                .map(this::toImportRow)
                .toList();
    }

    public EventCrfDto findByEventCrfVersion(StudyEventDto studyEvent, CrfVersionDTO crfVersion) {
        return eventCrfRepository.findByStudyEventIdAndCrfVersionId(
                        studyEvent.getId(), crfVersion.getId()).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public ArrayList<EventCrfDto> findByCrfVersion(CrfVersionDTO crfVersion) {
        return toBeans(eventCrfRepository.findByCrfVersionId(crfVersion.getId()));
    }

    @Transactional
    public void delete(int eventCRFId) {
    }

    @Transactional
    public void setSDVStatus(boolean sdvStatus, int userId, int eventCRFId) {
    }

    @Transactional
    public void markComplete(EventCrfDto ecb, boolean ide) {
    }

    @Transactional
    public void updateCRFVersionID(int event_crf_id, int crf_version_id, int user_id) {
    }

    @Transactional
    public void updateCRFVersionID(int event_crf_id, int crf_version_id, int user_id, java.sql.Connection con) {
    }

    public ArrayList findByEventSubjectCRFid(StudyEventDto studyEvent, StudySubjectDTO studySubject,
                                             CrfVersionDTO crfVersion) {
        return toBeans(eventCrfRepository.findByStudyEventIdAndStudySubjectIdAndCrfVersionId(
                studyEvent.getId(), studySubject.getId(), crfVersion.getId()));
    }

    public List<ImportEventCrf> findImportEventCrfsByEventSubjectCrfId(
            int studyEventId, int studySubjectId, int crfVersionId) {
        return eventCrfRepository.findByStudyEventIdAndStudySubjectIdAndCrfVersionId(
                        studyEventId, studySubjectId, crfVersionId).stream()
                .map(this::toImportRow)
                .toList();
    }

    @Transactional
    public ImportEventCrf createImportEventCrf(
            int studyEventId,
            int studySubjectId,
            int crfVersionId,
            int ownerId,
            String interviewerName,
            int statusId) {
        EventCrfEntity entity = new EventCrfEntity();
        entity.setStudyEventId(studyEventId);
        entity.setStudySubjectId(studySubjectId);
        entity.setCrfVersionId(crfVersionId);
        entity.setOwnerId(ownerId);
        entity.setInterviewerName(interviewerName);
        entity.setStatusId(statusId);
        entity.setDateInterviewed(LocalDateTime.now());
        entity.setDateCreated(LocalDateTime.now());
        return toImportRow(eventCrfRepository.save(entity));
    }

    public EventCrfDto findByEventCrfID(StudyEventDto studyEvent, CrfVersionDTO crfVersion) {
        return eventCrfRepository.findByStudyEventIdAndCrfVersionId(
                        studyEvent.getId(), crfVersion.getId()).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public Map<Integer, SortedSet<EventCrfDto>> buildEventCrfListByStudyEvent(Integer studySubjectId) {
        return new HashMap();
    }

    public Set<Integer> buildNonEmptyEventCrfIds(Integer studySubjectId) {
        return new java.util.LinkedHashSet();
    }

    public List<EventCrfDto> findAllCRFMigrationReportList(CrfVersionDTO sourceCrfVersionBean,
                                                            CrfVersionDTO targetCrfVersionBean,
                                                            ArrayList<String> studyEventDefnlist,
                                                            ArrayList<String> sitelist) {
        return new ArrayList();
    }

    public Integer countEventCRFsByEventNameSubjectLabel(String eventName, String subjectLabel) {
        return 0;
    }

    public EventCrfDto findByPKAndStudy(int id, StudyDto study) {
        return null;
    }

    public boolean isQuerySuccessful() {
        return true;
    }

    public Integer countEventCRFsByStudySubject(int studySubjectId, int studyId, int parentStudyId) {
        return 0;
    }

    public ArrayList getEventCRFsByStudySubjectCompleteOrLocked(int studySubjectId) {
        return new ArrayList();
    }

    public ArrayList getEventCRFsByStudySubjectLimit(int studySubjectId, int studyId, int parentStudyId,
                                                     int limit, int offset) {
        return new ArrayList();
    }

    public ArrayList getEventCRFsByStudySubject(int studySubjectId, int studyId, int parentStudyId) {
        return new ArrayList();
    }

    public ArrayList getEventCRFsByStudySubjectLabelLimit(String label, int studyId, int parentStudyId,
                                                          int limit, int offset) {
        return new ArrayList();
    }

    public Integer countEventCRFsByStudySubjectLabel(String label, int studyId, int parentStudyId) {
        return 0;
    }

    private void apply(EventCrfDto dto, EventCrfEntity entity) {
        entity.setStudyEventId(dto.getStudyEventId() > 0 ? dto.getStudyEventId() : null);
        entity.setStudySubjectId(dto.getStudySubjectId() > 0 ? dto.getStudySubjectId() : null);
        entity.setCrfVersionId(dto.getCRFVersionId() > 0 ? dto.getCRFVersionId() : null);
        entity.setStatusId(dto.getStatus() != null ? dto.getStatus().getId() : Status.INVALID.getId());
        entity.setDateInterviewed(toLocalDateTime(dto.getDateInterviewed()));
        entity.setInterviewerName(dto.getInterviewerName());
        entity.setAnnotations(dto.getAnnotations());
        entity.setDateCompleted(toLocalDateTime(dto.getDateCompleted()));
        entity.setValidatorId(dto.getValidatorId() > 0 ? dto.getValidatorId() : null);
        entity.setDateValidate(toLocalDateTime(dto.getDateValidate()));
        entity.setDateValidateCompleted(toLocalDateTime(dto.getDateValidateCompleted()));
        entity.setValidatorAnnotations(dto.getValidatorAnnotations());
        entity.setOwnerId(dto.getOwnerId() > 0 ? dto.getOwnerId() : null);
        entity.setUpdateId(dto.getUpdaterId() > 0 ? dto.getUpdaterId() : null);
        entity.setElectronicSignatureStatus(dto.isElectronicSignatureStatus());
        entity.setSdvStatus(dto.isSdvStatus());
        entity.setSdvUpdateId(dto.getSdvUpdateId() > 0 ? dto.getSdvUpdateId() : null);
    }

    private ArrayList<EventCrfDto> toBeans(List<EventCrfEntity> entities) {
        ArrayList<EventCrfDto> dtos = new ArrayList<>();
        entities.stream()
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    private ImportEventCrf toImportRow(EventCrfEntity entity) {
        return new ImportEventCrf(
                valueOrZero(entity.getEventCrfId()),
                valueOrZero(entity.getCrfVersionId()),
                valueOrZero(entity.getStatusId()));
    }

    private EventCrfDto toBean(EventCrfEntity entity) {
        EventCrfDto dto = new EventCrfDto();
        if (entity.getEventCrfId() != null) {
            dto.setId(entity.getEventCrfId());
        }
        dto.setStudyEventId(valueOrZero(entity.getStudyEventId()));
        dto.setStudySubjectId(valueOrZero(entity.getStudySubjectId()));
        dto.setCRFVersionId(valueOrZero(entity.getCrfVersionId()));
        dto.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        dto.setDateInterviewed(toDate(entity.getDateInterviewed()));
        dto.setInterviewerName(entity.getInterviewerName() != null ? entity.getInterviewerName() : "");
        dto.setAnnotations(entity.getAnnotations() != null ? entity.getAnnotations() : "");
        dto.setDateCompleted(toDate(entity.getDateCompleted()));
        dto.setValidatorId(valueOrZero(entity.getValidatorId()));
        dto.setDateValidate(toDate(entity.getDateValidate()));
        dto.setDateValidateCompleted(toDate(entity.getDateValidateCompleted()));
        dto.setValidatorAnnotations(entity.getValidatorAnnotations() != null ? entity.getValidatorAnnotations() : "");
        dto.setCreatedDate(toDate(entity.getDateCreated()));
        dto.setUpdatedDate(toDate(entity.getDateUpdated()));
        dto.setOwnerId(valueOrZero(entity.getOwnerId()));
        dto.setUpdaterId(valueOrZero(entity.getUpdateId()));
        dto.setElectronicSignatureStatus(entity.getElectronicSignatureStatus() != null
                && entity.getElectronicSignatureStatus());
        dto.setSdvStatus(entity.getSdvStatus() != null && entity.getSdvStatus());
        dto.setSdvUpdateId(valueOrZero(entity.getSdvUpdateId()));
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
