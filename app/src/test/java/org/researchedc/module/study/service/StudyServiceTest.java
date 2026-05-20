package org.researchedc.module.study.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import static org.researchedc.testutil.TestDataFactory.*;

import org.researchedc.module.study.dto.CreateStudyRequest;
import org.researchedc.module.study.dto.StudyDetailDTO;
import org.researchedc.module.study.dto.StudySummaryDTO;
import org.researchedc.module.study.dto.UpdateStudyRequest;
import org.researchedc.module.study.entity.StudyEntity;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.study.repository.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Mock private StudyRepository studyRepository;
    @Mock private AuditService auditService;

    private StudyService service;

    @BeforeEach
    void setUp() {
        service = new StudyService(studyRepository, auditService);
    }

    @Test
    void listStudies_returnsOnlyParentStudies() {
        StudyEntity s1 = createStudy(1, "Study A");
        StudyEntity s2 = createStudy(2, "Study B");
        when(studyRepository.findByParentStudyIdIsNullOrderByName())
                .thenReturn(List.of(s1, s2));

        List<StudySummaryDTO> result = service.listStudies();

        assertEquals(2, result.size());
        assertEquals("Study A", result.get(0).getName());
        assertEquals("Study B", result.get(1).getName());
    }

    @Test
    void listSites_returnsSitesForParent() {
        StudyEntity site = createStudy(3, "Site X");
        site.setParentStudyId(1);
        when(studyRepository.findByParentStudyIdOrderByName(1))
                .thenReturn(List.of(site));

        List<StudySummaryDTO> result = service.listSites(1);

        assertEquals(1, result.size());
        assertEquals("Site X", result.getFirst().getName());
    }

    @Test
    void searchByName_returnsMatchingStudies() {
        StudyEntity s = createStudy(1, "Test Study");
        when(studyRepository.findByNameContainingIgnoreCase("Test"))
                .thenReturn(List.of(s));

        List<StudySummaryDTO> result = service.searchByName("Test");

        assertEquals(1, result.size());
        assertEquals("Test Study", result.getFirst().getName());
    }

    @Test
    void getStudy_whenFound_returnsDetail() {
        StudyEntity s = createStudy(1, "Detailed Study");
        s.setOfficialTitle("Official Title");
        when(studyRepository.findById(1)).thenReturn(Optional.of(s));
        when(studyRepository.findByParentStudyIdOrderByName(1))
                .thenReturn(List.of());

        StudyDetailDTO result = service.getStudy(1);

        assertEquals(1, result.getStudyId());
        assertEquals("Detailed Study", result.getName());
        assertEquals("Official Title", result.getOfficialTitle());
    }

    @Test
    void getStudy_whenStudyHasSites_returnsWithSites() {
        StudyEntity s = createStudy(1, "Parent Study");
        StudyEntity site = createStudy(2, "Child Site");
        site.setParentStudyId(1);
        when(studyRepository.findById(1)).thenReturn(Optional.of(s));
        when(studyRepository.findByParentStudyIdOrderByName(1))
                .thenReturn(List.of(site));

        StudyDetailDTO result = service.getStudy(1);

        assertNotNull(result.getSites());
        assertEquals(1, result.getSites().size());
        assertEquals("Child Site", result.getSites().getFirst().getName());
    }

    @Test
    void getStudy_whenNotFound_throwsException() {
        when(studyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getStudy(99));
    }

    @Test
    void createStudy_withValidRequest_savesAndReturns() {
        CreateStudyRequest request = new CreateStudyRequest();
        request.setName("New Study");
        request.setUniqueIdentifier("NEW-001");
        request.setSummary("A new study");
        request.setTypeId(1);
        request.setStatusId(1);

        when(studyRepository.save(any(StudyEntity.class)))
                .thenAnswer(i -> {
                    StudyEntity e = i.getArgument(0);
                    if (e.getStudyId() == null) e.setStudyId(1);
                    return e;
                });

        StudyDetailDTO result = service.createStudy(request, 42);

        assertEquals("New Study", result.getName());
        assertNotNull(result.getDateCreated());
        verify(studyRepository).save(any(StudyEntity.class));
    }

    @Test
    void createStudy_withBlankName_throwsException() {
        CreateStudyRequest request = new CreateStudyRequest();
        request.setName("");

        assertThrows(IllegalArgumentException.class,
                () -> service.createStudy(request, 42));
    }

    @Test
    void updateStudy_updatesFieldsAndReturns() {
        StudyEntity existing = createStudy(1, "Old Name");
        existing.setDateCreated(LocalDateTime.now().minusDays(1));
        when(studyRepository.findById(1)).thenReturn(Optional.of(existing));
        when(studyRepository.save(any(StudyEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        UpdateStudyRequest request = new UpdateStudyRequest();
        request.setName("New Name");

        StudyDetailDTO result = service.updateStudy(1, request, 99);

        assertEquals("New Name", result.getName());
        assertNotNull(result.getDateUpdated());
        verify(studyRepository).save(existing);
    }

    @Test
    void updateStudy_whenNotFound_throwsException() {
        when(studyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.updateStudy(99, new UpdateStudyRequest(), 1));
    }

    @Test
    void deleteStudy_deletesExisting() {
        StudyEntity existing = createStudy(1, "To Delete");
        when(studyRepository.findById(1)).thenReturn(Optional.of(existing));

        service.deleteStudy(1, 42);

        verify(studyRepository).delete(existing);
    }

    @Test
    void deleteStudy_whenNotFound_throwsException() {
        when(studyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.deleteStudy(99, 1));
    }

    @Test
    void updateStudyStatus_updatesStatusAndReturns() {
        StudyEntity existing = createStudy(1, "Study");
        existing.setStatusId(1);
        when(studyRepository.findById(1)).thenReturn(Optional.of(existing));
        when(studyRepository.save(any(StudyEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        service.updateStudyStatus(1, 2, 42);

        assertEquals(2, existing.getStatusId());
        assertNotNull(existing.getDateUpdated());
        assertEquals(42, existing.getUpdateId());
        verify(studyRepository).save(existing);
    }

    @Test
    void updateStudyStatus_whenNotFound_throwsException() {
        when(studyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.updateStudyStatus(99, 2, 1));
    }
}
