package org.researchedc.module.audit.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.researchedc.module.audit.dto.AuditStudySubjectEventsDTO;
import org.researchedc.module.audit.dto.AuditStudySubjectLogDTO;

class AuditStudySubjectEventAdapterTest {

    @Test
    void findStudySubjectEvents_mapsNativeStudySubjectAuditAndEventRows() {
        EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
        Query studyQuery = queryReturning(new Object[] {11, "Main Study", "PROTO-1", "SECONDARY", "S_MAIN"});
        Query studySubjectsQuery = queryReturning(new Object[] {
                21,
                "SUBJ-001",
                "ALT-001",
                "SS_SUBJ001",
                31,
                11,
                Timestamp.from(Instant.parse("2026-06-07T10:00:00Z")),
                7,
                "owner",
                1
        });
        Query subjectQuery = queryReturning(new Object[] {
                31,
                "PERSON-001",
                Timestamp.from(Instant.parse("1990-01-01T00:00:00Z")),
                "f",
                true,
                1
        });
        Query studySubjectAuditsQuery = queryReturning(new Object[] {
                41,
                Timestamp.from(Instant.parse("2026-06-07T12:00:00Z")),
                "study_subject",
                8,
                21,
                "secondary_label",
                "Subject Updated",
                2,
                "old",
                "new",
                "corrected",
                "updater"
        });
        Query subjectAuditsQuery = queryReturning(new Object[] {
                42,
                null,
                "subject",
                0,
                31,
                "unique_identifier",
                "",
                5,
                "",
                "",
                "",
                ""
        });
        Query eventsQuery = queryReturning(new Object[] {
                51,
                61,
                21,
                "Clinic",
                1,
                Timestamp.from(Instant.parse("2026-06-08T00:00:00Z")),
                Timestamp.from(Instant.parse("2026-06-08T03:00:00Z")),
                1,
                1
        });
        Query eventCrfsQuery = queryReturning(new Object[] {
                71,
                51,
                21,
                81,
                Timestamp.from(Instant.parse("2026-06-08T01:00:00Z")),
                "Interviewer",
                Timestamp.from(Instant.parse("2026-06-08T02:00:00Z")),
                1,
                true,
                false
        });
        Query definitionQuery = queryReturning(new Object[] {
                61,
                "Baseline",
                "SE_BASE",
                "desc",
                "screening",
                "common",
                false
        });
        when(entityManager.createNativeQuery(startsWith("SELECT study_id"))).thenReturn(studyQuery);
        when(entityManager.createNativeQuery(startsWith("SELECT ss.study_subject_id"))).thenReturn(studySubjectsQuery);
        when(entityManager.createNativeQuery(startsWith("SELECT subject_id"))).thenReturn(subjectQuery);
        when(entityManager.createNativeQuery(startsWith("SELECT ale.audit_id")))
                .thenReturn(studySubjectAuditsQuery, subjectAuditsQuery);
        when(entityManager.createNativeQuery(startsWith("SELECT se.study_event_id"))).thenReturn(eventsQuery);
        when(entityManager.createNativeQuery(startsWith("SELECT event_crf_id"))).thenReturn(eventCrfsQuery);
        when(entityManager.createNativeQuery(startsWith("SELECT study_event_definition_id"))).thenReturn(definitionQuery);

        AuditStudySubjectEventAdapter adapter = new AuditStudySubjectEventAdapter();
        adapter.setEntityManager(entityManager);

        AuditStudySubjectEventsDTO result = adapter.findStudySubjectEvents(11);

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
        assertEquals("", row.subject().label());
        assertEquals("1990-01-01T00:00:00Z", row.subject().dateOfBirth());
        assertEquals(2, row.audits().size());
        assertEquals("study_subject", row.audits().getFirst().auditTable());
        assertEquals("subject", row.audits().get(1).auditTable());
        assertEquals(51, row.events().getFirst().id());
        assertEquals("Baseline", row.events().getFirst().definition().name());
        assertEquals("Clinic", row.events().getFirst().location());
        assertEquals("scheduled", row.events().getFirst().subjectEventStatus());
        assertNull(row.events().getFirst().stage());
        assertEquals(71, row.events().getFirst().eventCrfs().getFirst().id());
        assertEquals("Interviewer", row.events().getFirst().eventCrfs().getFirst().interviewerName());
        assertNull(row.events().getFirst().eventCrfs().getFirst().stage());
    }

    private Query queryReturning(Object[] row) {
        Query query = org.mockito.Mockito.mock(Query.class);
        when(query.setParameter(org.mockito.Mockito.eq(1), org.mockito.Mockito.any())).thenReturn(query);
        when(query.getResultList()).thenReturn(java.util.Collections.singletonList(row));
        return query;
    }
}
