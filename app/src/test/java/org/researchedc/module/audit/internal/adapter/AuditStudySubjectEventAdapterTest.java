package org.researchedc.module.audit.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.researchedc.bean.admin.AuditBean;
import org.researchedc.bean.core.DataEntryStage;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.dao.spi.AuditDao;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.module.audit.dto.AuditStudySubjectEventsDTO;
import org.researchedc.module.audit.dto.AuditStudySubjectLogDTO;

class AuditStudySubjectEventAdapterTest {

    @BeforeEach
    void setUpLocale() {
        ResourceBundleProvider.updateLocale(Locale.of("us"));
    }

    @Test
    void findStudySubjectEvents_mapsLegacyStudySubjectAuditAndEventRows() {
        IStudyDAO studyDao = org.mockito.Mockito.mock(IStudyDAO.class);
        IStudySubjectDAO studySubjectDao = org.mockito.Mockito.mock(IStudySubjectDAO.class);
        ISubjectDAO subjectDao = org.mockito.Mockito.mock(ISubjectDAO.class);
        AuditDao auditDao = org.mockito.Mockito.mock(AuditDao.class);
        IStudyEventDAO studyEventDao = org.mockito.Mockito.mock(IStudyEventDAO.class);
        IStudyEventDefinitionDAO studyEventDefinitionDao =
                org.mockito.Mockito.mock(IStudyEventDefinitionDAO.class);
        EventCRFDao eventCrfDao = org.mockito.Mockito.mock(EventCRFDao.class);

        StudyBean study = new StudyBean();
        study.setId(11);
        study.setName("Main Study");
        study.setIdentifier("PROTO-1");
        study.setSecondaryIdentifier("SECONDARY");
        study.setOid("S_MAIN");

        UserAccountBean owner = new UserAccountBean();
        owner.setId(7);
        owner.setName("owner");

        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setId(21);
        studySubject.setLabel("SUBJ-001");
        studySubject.setSecondaryLabel("ALT-001");
        studySubject.setOid("SS_SUBJ001");
        studySubject.setSubjectId(31);
        studySubject.setStudyId(11);
        studySubject.setCreatedDate(Date.from(Instant.parse("2026-06-07T10:00:00Z")));
        studySubject.setOwner(owner);
        studySubject.setStatus(Status.AVAILABLE);

        SubjectBean subject = new SubjectBean();
        subject.setId(31);
        subject.setUniqueIdentifier("PERSON-001");
        subject.setLabel("subject-label");
        subject.setDateOfBirth(Date.from(Instant.parse("1990-01-01T00:00:00Z")));
        subject.setGender('f');
        subject.setDobCollected(true);
        subject.setStatus(Status.AVAILABLE);

        AuditBean studySubjectAudit = new AuditBean();
        studySubjectAudit.setId(41);
        studySubjectAudit.setAuditDate(Date.from(Instant.parse("2026-06-07T12:00:00Z")));
        studySubjectAudit.setAuditTable("study_subject");
        studySubjectAudit.setUserId(8);
        studySubjectAudit.setUserName("updater");
        studySubjectAudit.setEntityId(21);
        studySubjectAudit.setEntityName("secondary_label");
        studySubjectAudit.setAuditEventTypeName("Subject Updated");
        studySubjectAudit.setAuditEventTypeId(2);
        studySubjectAudit.setOldValue("old");
        studySubjectAudit.setNewValue("new");
        studySubjectAudit.setReasonForChange("corrected");

        AuditBean subjectAudit = new AuditBean();
        subjectAudit.setId(42);
        subjectAudit.setAuditTable("subject");
        subjectAudit.setEntityId(31);
        subjectAudit.setEntityName("unique_identifier");

        StudyEventBean event = new StudyEventBean();
        event.setId(51);
        event.setStudyEventDefinitionId(61);
        event.setStudySubjectId(21);
        event.setLocation("Clinic");
        event.setSampleOrdinal(1);
        event.setDateStarted(Date.from(Instant.parse("2026-06-08T00:00:00Z")));
        event.setDateEnded(Date.from(Instant.parse("2026-06-08T03:00:00Z")));
        event.setStatus(Status.AVAILABLE);
        event.setStage(DataEntryStage.INITIAL_DATA_ENTRY);
        event.setSubjectEventStatus(SubjectEventStatus.SCHEDULED);

        StudyEventDefinitionBean definition = new StudyEventDefinitionBean();
        definition.setId(61);
        definition.setName("Baseline");
        definition.setOid("SE_BASE");
        definition.setDescription("desc");
        definition.setCategory("screening");
        definition.setType("common");
        definition.setRepeating(false);

        EventCRFBean eventCrf = new EventCRFBean();
        eventCrf.setId(71);
        eventCrf.setStudyEventId(51);
        eventCrf.setStudySubjectId(21);
        eventCrf.setCRFVersionId(81);
        eventCrf.setDateInterviewed(Date.from(Instant.parse("2026-06-08T01:00:00Z")));
        eventCrf.setInterviewerName("Interviewer");
        eventCrf.setDateCompleted(Date.from(Instant.parse("2026-06-08T02:00:00Z")));
        eventCrf.setStatus(Status.AVAILABLE);
        eventCrf.setStage(DataEntryStage.INITIAL_DATA_ENTRY);
        eventCrf.setElectronicSignatureStatus(true);
        eventCrf.setSdvStatus(false);

        when(studyDao.findByPK(11)).thenReturn(study);
        when(studySubjectDao.findAllByStudyOrderByLabel(study))
                .thenReturn(new ArrayList<>(List.of(studySubject)));
        when(subjectDao.findByPK(31)).thenReturn(subject);
        when(auditDao.findStudySubjectAuditEvents(21)).thenReturn(new ArrayList<>(List.of(studySubjectAudit)));
        when(auditDao.findSubjectAuditEvents(31)).thenReturn(new ArrayList<>(List.of(subjectAudit)));
        when(studyEventDao.findAllByStudySubject(studySubject)).thenReturn(new ArrayList<>(List.of(event)));
        when(studyEventDefinitionDao.findByPK(61)).thenReturn(definition);
        when(eventCrfDao.findAllByStudyEvent(event)).thenReturn(new ArrayList<>(List.of(eventCrf)));

        AuditStudySubjectEventsDTO result = new AuditStudySubjectEventAdapter(
                studyDao, studySubjectDao, subjectDao, auditDao, studyEventDao,
                studyEventDefinitionDao, eventCrfDao).findStudySubjectEvents(11);

        assertEquals(11, result.study().id());
        assertEquals("Main Study", result.study().name());
        assertEquals("PROTO-1", result.study().identifier());
        AuditStudySubjectLogDTO row = result.subjects().getFirst();
        assertEquals(21, row.studySubject().id());
        assertEquals("SUBJ-001", row.studySubject().label());
        assertEquals("owner", row.studySubject().ownerName());
        assertEquals("available", row.studySubject().status());
        assertEquals(31, row.subject().id());
        assertEquals("PERSON-001", row.subject().uniqueIdentifier());
        assertEquals("1990-01-01T00:00:00Z", row.subject().dateOfBirth());
        assertEquals(2, row.audits().size());
        assertEquals("study_subject", row.audits().getFirst().auditTable());
        assertEquals("subject", row.audits().get(1).auditTable());
        assertEquals(51, row.events().getFirst().id());
        assertEquals("Baseline", row.events().getFirst().definition().name());
        assertEquals("Clinic", row.events().getFirst().location());
        assertEquals("scheduled", row.events().getFirst().subjectEventStatus());
        assertEquals(71, row.events().getFirst().eventCrfs().getFirst().id());
        assertEquals("Interviewer", row.events().getFirst().eventCrfs().getFirst().interviewerName());
        assertEquals("initial data entry", row.events().getFirst().eventCrfs().getFirst().stage());
    }
}
