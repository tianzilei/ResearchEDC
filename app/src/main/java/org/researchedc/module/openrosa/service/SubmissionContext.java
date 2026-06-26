package org.researchedc.module.openrosa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.openrosa.dto.SubmissionRequest;
import org.researchedc.module.study.entity.StudyEntity;
import org.researchedc.module.subject.entity.StudySubjectEntity;

public class SubmissionContext {

    private StudyEntity study;
    private StudySubjectEntity subject;
    private StudyEventEntity studyEvent;
    private EventCrfEntity eventCrf;
    private CrfVersionEntity crfVersion;
    private SubmissionRequest request;
    private Map<String, String> subjectContextMap = new HashMap<>();
    private List<ItemValue> items = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private String requestBody;
    private Integer ownerId;

    public StudyEntity getStudy() { return study; }
    public void setStudy(StudyEntity v) { this.study = v; }

    public StudySubjectEntity getSubject() { return subject; }
    public void setSubject(StudySubjectEntity v) { this.subject = v; }

    public StudyEventEntity getStudyEvent() { return studyEvent; }
    public void setStudyEvent(StudyEventEntity v) { this.studyEvent = v; }

    public EventCrfEntity getEventCrf() { return eventCrf; }
    public void setEventCrf(EventCrfEntity v) { this.eventCrf = v; }

    public CrfVersionEntity getCrfVersion() { return crfVersion; }
    public void setCrfVersion(CrfVersionEntity v) { this.crfVersion = v; }

    public SubmissionRequest getRequest() { return request; }
    public void setRequest(SubmissionRequest v) { this.request = v; }

    public Map<String, String> getSubjectContextMap() { return subjectContextMap; }
    public void setSubjectContextMap(Map<String, String> v) { this.subjectContextMap = v; }

    public List<ItemValue> getItems() { return items; }
    public void setItems(List<ItemValue> v) { this.items = v; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> v) { this.errors = v; }
    public void addError(String error) { this.errors.add(error); }

    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String v) { this.requestBody = v; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }

    public boolean hasErrors() { return !errors.isEmpty(); }
}
