package org.researchedc.module.dataset.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.dataset.entity.DatasetEntity;
import org.researchedc.module.dataset.repository.DatasetRepository;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {

    @Mock private DatasetRepository datasetRepository;
    @Mock private CurrentStudyAccessService currentStudyAccessService;
    private DatasetService service;

    @BeforeEach
    void setUp() {
        service = new DatasetService(datasetRepository, currentStudyAccessService);
    }

    @Test
    void listAll_whenAdmin_returnsAll() {
        DatasetEntity d1 = dataset(1, "DS1", "desc1", 10, 100);
        DatasetEntity d2 = dataset(2, "DS2", "desc2", 10, 100);
        when(currentStudyAccessService.canReadAllStudies(42)).thenReturn(true);
        when(datasetRepository.findAll()).thenReturn(List.of(d1, d2));

        List<DatasetEntity> result = service.listAll(42);

        assertEquals(2, result.size());
        assertEquals("DS1", result.get(0).getName());
        assertEquals("DS2", result.get(1).getName());
    }

    @Test
    void listAll_whenNonAdmin_returnsReadableStudyDatasets() {
        DatasetEntity d = dataset(1, "DS1", "desc", 10, 100);
        when(currentStudyAccessService.canReadAllStudies(42)).thenReturn(false);
        when(currentStudyAccessService.readableStudyIds(42)).thenReturn(Set.of(10, 11));
        when(datasetRepository.findByStudyIdInOrderByStudyIdAscNameAsc(Set.of(10, 11)))
                .thenReturn(List.of(d));

        List<DatasetEntity> result = service.listAll(42);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getStudyId());
        verify(datasetRepository, never()).findAll();
    }

    @Test
    void listAll_whenNoReadableStudies_returnsEmptyList() {
        when(currentStudyAccessService.canReadAllStudies(42)).thenReturn(false);
        when(currentStudyAccessService.readableStudyIds(42)).thenReturn(Set.of());

        List<DatasetEntity> result = service.listAll(42);

        assertTrue(result.isEmpty());
        verifyNoInteractions(datasetRepository);
    }

    @Test
    void listByStudy_whenStudyHasDatasets_returnsFiltered() {
        DatasetEntity d = dataset(1, "DS1", "desc", 10, 100);
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(true);
        when(datasetRepository.findByStudyId(10)).thenReturn(List.of(d));

        List<DatasetEntity> result = service.listByStudy(10, 42);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getStudyId());
    }

    @Test
    void listByStudy_whenNoMatch_returnsEmptyList() {
        when(currentStudyAccessService.canReadStudy(42, 999)).thenReturn(true);
        when(datasetRepository.findByStudyId(999)).thenReturn(List.of());

        List<DatasetEntity> result = service.listByStudy(999, 42);

        assertTrue(result.isEmpty());
    }

    @Test
    void listByStudy_whenAccessDenied_throwsAndDoesNotQuery() {
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.listByStudy(10, 42));
        verifyNoInteractions(datasetRepository);
    }

    @Test
    void getById_whenFound_returnsEntity() {
        DatasetEntity d = dataset(1, "DS1", "desc", 10, 100);
        when(datasetRepository.findById(1)).thenReturn(Optional.of(d));
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(true);

        DatasetEntity result = service.getById(1, 42);

        assertEquals(1, result.getDatasetId());
        assertEquals("DS1", result.getName());
    }

    @Test
    void getById_whenNotFound_throwsNoSuchElement() {
        when(datasetRepository.findById(999)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> service.getById(999, 42));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    void getById_whenAccessDenied_throws() {
        DatasetEntity d = dataset(1, "DS1", "desc", 10, 100);
        when(datasetRepository.findById(1)).thenReturn(Optional.of(d));
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getById(1, 42));
    }

    @Test
    void create_whenValid_returnsSavedEntity() {
        DatasetEntity saved = dataset(1, "New", "New desc", 10, 100);
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(datasetRepository.save(any(DatasetEntity.class))).thenReturn(saved);

        DatasetEntity result = service.create("New", "New desc", 10, 100, 42);

        assertEquals("New", result.getName());
        assertEquals("New desc", result.getDescription());
        verify(datasetRepository).save(argThat(e -> {
            assertEquals("New", e.getName());
            assertEquals(10, e.getStudyId());
            assertEquals(1, e.getStatusId());
            assertEquals(0, e.getNumRuns());
            return true;
        }));
    }

    @Test
    void create_whenDescriptionNull_defaultsToEmpty() {
        DatasetEntity saved = dataset(1, "New", "", 10, 100);
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(datasetRepository.save(any(DatasetEntity.class))).thenReturn(saved);

        DatasetEntity result = service.create("New", null, 10, 100, 42);

        assertEquals("", result.getDescription());
    }

    @Test
    void create_whenAccessDenied_throwsAndDoesNotSave() {
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.create("New", null, 10, 100, 42));
        verify(datasetRepository, never()).save(any());
    }

    @Test
    void update_whenFound_savesUpdatedFields() {
        DatasetEntity existing = dataset(1, "Old", "Old desc", 10, 100);
        when(datasetRepository.findById(1)).thenReturn(Optional.of(existing));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(i -> i.getArgument(0));

        DatasetEntity result = service.update(1, "Updated", "Updated desc", 42);

        assertEquals("Updated", result.getName());
        assertEquals("Updated desc", result.getDescription());
        assertNotNull(result.getDateUpdated());
        verify(datasetRepository).save(any());
    }

    @Test
    void update_whenDescriptionNull_defaultsToEmpty() {
        DatasetEntity existing = dataset(1, "Old", "Old desc", 10, 100);
        when(datasetRepository.findById(1)).thenReturn(Optional.of(existing));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(i -> i.getArgument(0));

        DatasetEntity result = service.update(1, "Updated", null, 42);

        assertEquals("", result.getDescription());
    }

    @Test
    void update_whenNotFound_throwsNoSuchElement() {
        when(datasetRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.update(999, "name", "desc", 42));
    }

    @Test
    void update_whenAccessDenied_throwsAndDoesNotSave() {
        DatasetEntity existing = dataset(1, "Old", "Old desc", 10, 100);
        when(datasetRepository.findById(1)).thenReturn(Optional.of(existing));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.update(1, "Updated", "desc", 42));
        verify(datasetRepository, never()).save(any());
    }

    private static DatasetEntity dataset(Integer id, String name, String description,
                                         Integer studyId, Integer ownerId) {
        DatasetEntity e = new DatasetEntity();
        e.setDatasetId(id);
        e.setName(name);
        e.setDescription(description);
        e.setStudyId(studyId);
        e.setOwnerId(ownerId);
        e.setStatusId(1);
        e.setNumRuns(0);
        return e;
    }
}
