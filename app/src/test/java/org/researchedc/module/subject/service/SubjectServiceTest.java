package org.researchedc.module.subject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.researchedc.testutil.TestDataFactory.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.event.service.EventService;
import org.researchedc.module.subject.dto.CreateSubjectRequest;
import org.researchedc.module.subject.dto.EnrollSubjectRequest;
import org.researchedc.module.subject.dto.StudySubjectDTO;
import org.researchedc.module.subject.dto.SubjectDTO;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.entity.SubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.researchedc.module.subject.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock private SubjectRepository subjectRepository;
    @Mock private StudySubjectRepository studySubjectRepository;
    @Mock private EventService eventService;
    @Mock private AuditService auditService;

    private SubjectService service;

    @BeforeEach
    void setUp() {
        service = new SubjectService(subjectRepository, studySubjectRepository,
                eventService, auditService);
    }

    @Test
    void searchSubjects_returnsMatching() {
        SubjectEntity s = createSubject(1, "SUBJ-001");
        when(subjectRepository.findByUniqueIdentifierContainingIgnoreCase("001"))
                .thenReturn(List.of(s));

        List<SubjectDTO> result = service.searchSubjects("001");

        assertEquals(1, result.size());
        assertEquals("SUBJ-001", result.getFirst().getUniqueIdentifier());
    }

    @Test
    void searchSubjects_whenNoMatch_returnsEmpty() {
        when(subjectRepository.findByUniqueIdentifierContainingIgnoreCase("ZZZ"))
                .thenReturn(List.of());

        List<SubjectDTO> result = service.searchSubjects("ZZZ");

        assertTrue(result.isEmpty());
    }

    @Test
    void getSubject_whenFound_returnsDto() {
        SubjectEntity s = createSubject(1, "SUBJ-001");
        when(subjectRepository.findById(1)).thenReturn(Optional.of(s));

        SubjectDTO result = service.getSubject(1);

        assertEquals(1, result.getSubjectId());
        assertEquals("SUBJ-001", result.getUniqueIdentifier());
    }

    @Test
    void getSubject_whenNotFound_throwsException() {
        when(subjectRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getSubject(99));
    }

    @Test
    void listStudySubjects_returnsByStudyId() {
        StudySubjectEntity ss = createStudySubject(1, 5, 1, "SUBJ-001");
        when(studySubjectRepository.findByStudyIdOrderByLabel(5))
                .thenReturn(List.of(ss));

        List<StudySubjectDTO> result = service.listStudySubjects(5);

        assertEquals(1, result.size());
        assertEquals("SUBJ-001", result.getFirst().getLabel());
    }

    @Test
    void getStudySubject_whenFound_returnsDto() {
        StudySubjectEntity ss = createStudySubject(1, 5, 1, "SUBJ-001");
        when(studySubjectRepository.findById(1)).thenReturn(Optional.of(ss));

        StudySubjectDTO result = service.getStudySubject(1);

        assertEquals(1, result.getStudySubjectId());
        assertEquals("SUBJ-001", result.getLabel());
    }

    @Test
    void getStudySubject_whenNotFound_throwsException() {
        when(studySubjectRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.getStudySubject(99));
    }

    @Test
    void createSubject_withValidRequest_savesAndReturns() {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setUniqueIdentifier("NEW-SUBJ");
        request.setGender("F");

        when(subjectRepository.save(any(SubjectEntity.class)))
                .thenAnswer(i -> {
                    SubjectEntity e = i.getArgument(0);
                    if (e.getSubjectId() == null) e.setSubjectId(1);
                    return e;
                });

        SubjectDTO result = service.createSubject(request, 42);

        assertEquals("NEW-SUBJ", result.getUniqueIdentifier());
        assertNotNull(result.getDateCreated());
        verify(subjectRepository).save(any(SubjectEntity.class));
        verify(auditService).recordAudit(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void createSubject_withBlankIdentifier_throwsException() {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setUniqueIdentifier("");

        assertThrows(IllegalArgumentException.class,
                () -> service.createSubject(request, 42));
    }

    @Test
    void createSubject_withNullIdentifier_throwsException() {
        CreateSubjectRequest request = new CreateSubjectRequest();

        assertThrows(IllegalArgumentException.class,
                () -> service.createSubject(request, 42));
    }

    @Test
    void enrollSubject_withValidRequest_savesAndReturns() {
        when(subjectRepository.existsById(1)).thenReturn(true);
        when(studySubjectRepository.save(any(StudySubjectEntity.class)))
                .thenAnswer(i -> {
                    StudySubjectEntity e = i.getArgument(0);
                    if (e.getStudySubjectId() == null) e.setStudySubjectId(1);
                    return e;
                });

        EnrollSubjectRequest request = new EnrollSubjectRequest();
        request.setStudyId(5);
        request.setSubjectId(1);
        request.setLabel("ENROLL-001");
        request.setEnrollmentDate(LocalDateTime.now());

        StudySubjectDTO result = service.enrollSubject(request, 42);

        assertEquals("ENROLL-001", result.getLabel());
        assertEquals(5, result.getStudyId());
        verify(studySubjectRepository).save(any(StudySubjectEntity.class));
    }

    @Test
    void enrollSubject_withMissingStudyId_throwsException() {
        EnrollSubjectRequest request = new EnrollSubjectRequest();
        request.setSubjectId(1);

        assertThrows(IllegalArgumentException.class,
                () -> service.enrollSubject(request, 42));
    }

    @Test
    void enrollSubject_withMissingSubjectId_throwsException() {
        EnrollSubjectRequest request = new EnrollSubjectRequest();
        request.setStudyId(5);

        assertThrows(IllegalArgumentException.class,
                () -> service.enrollSubject(request, 42));
    }

    @Test
    void enrollSubject_withNonExistentSubject_throwsException() {
        when(subjectRepository.existsById(99)).thenReturn(false);

        EnrollSubjectRequest request = new EnrollSubjectRequest();
        request.setStudyId(5);
        request.setSubjectId(99);

        assertThrows(NoSuchElementException.class,
                () -> service.enrollSubject(request, 42));
    }

    @Test
    void enrollSubject_withEventDefinition_schedulesEvent() {
        when(subjectRepository.existsById(1)).thenReturn(true);
        when(studySubjectRepository.save(any(StudySubjectEntity.class)))
                .thenAnswer(i -> {
                    StudySubjectEntity e = i.getArgument(0);
                    if (e.getStudySubjectId() == null) e.setStudySubjectId(1);
                    return e;
                });

        EnrollSubjectRequest request = new EnrollSubjectRequest();
        request.setStudyId(5);
        request.setSubjectId(1);
        request.setLabel("ENROLL-001");
        request.setEnrollmentDate(LocalDateTime.now());
        request.setEventDefinitionId(10);

        service.enrollSubject(request, 42);

        verify(eventService).scheduleEvent(any(), eq(42));
    }
    @Test
    void removeSubject_setsRemovedStatusAndAudits() {
        SubjectEntity subject = createSubject(1, "SUBJ-001");
        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject));

        service.removeSubject(1, 42);

        assertEquals(5, subject.getStatusId());
        assertNotNull(subject.getDateUpdated());
        assertEquals(42, subject.getUpdateId());
        verify(subjectRepository).save(subject);
        verify(auditService).recordAudit(isNull(), any(), eq("Subject"), eq(1L), eq("SUBJ-001"),
                isNull(), isNull(), eq(42), eq("Subject removed (status=5)"), eq("subject"));
    }

    @Test
    void restoreSubject_setsAvailableStatusAndAudits() {
        SubjectEntity subject = createSubject(1, "SUBJ-001");
        subject.setStatusId(5);
        when(subjectRepository.findById(1)).thenReturn(Optional.of(subject));

        service.restoreSubject(1, 42);

        assertEquals(1, subject.getStatusId());
        assertNotNull(subject.getDateUpdated());
        assertEquals(42, subject.getUpdateId());
        verify(subjectRepository).save(subject);
        verify(auditService).recordAudit(isNull(), any(), eq("Subject"), eq(1L), eq("SUBJ-001"),
                isNull(), isNull(), eq(42), eq("Subject restored (status=1)"), eq("subject"));
    }

    @Test
    void removeStudySubject_setsRemovedStatusAndAudits() {
        StudySubjectEntity studySubject = createStudySubject(7, 5, 1, "SS-001");
        when(studySubjectRepository.findById(7)).thenReturn(Optional.of(studySubject));

        service.removeStudySubject(7, 42);

        assertEquals(5, studySubject.getStatusId());
        assertNotNull(studySubject.getDateUpdated());
        assertEquals(42, studySubject.getUpdateId());
        verify(studySubjectRepository).save(studySubject);
        verify(auditService).recordAudit(eq(5), any(), eq("StudySubject"), eq(7L), eq("SS-001"),
                isNull(), isNull(), eq(42), eq("StudySubject removed (status=5)"), eq("subject"));
    }

    @Test
    void restoreStudySubject_setsAvailableStatusAndAudits() {
        StudySubjectEntity studySubject = createStudySubject(7, 5, 1, "SS-001");
        studySubject.setStatusId(5);
        when(studySubjectRepository.findById(7)).thenReturn(Optional.of(studySubject));

        service.restoreStudySubject(7, 42);

        assertEquals(1, studySubject.getStatusId());
        assertNotNull(studySubject.getDateUpdated());
        assertEquals(42, studySubject.getUpdateId());
        verify(studySubjectRepository).save(studySubject);
        verify(auditService).recordAudit(eq(5), any(), eq("StudySubject"), eq(7L), eq("SS-001"),
                isNull(), isNull(), eq(42), eq("StudySubject restored (status=1)"), eq("subject"));
    }
}
