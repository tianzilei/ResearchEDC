package org.researchedc.module.dataset.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.module.dataset.entity.DatasetEntity;
import org.researchedc.module.dataset.repository.DatasetRepository;

@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {

    @Mock private DatasetRepository datasetRepository;
    private DatasetService service;

    @BeforeEach
    void setUp() {
        service = new DatasetService(datasetRepository);
    }

    @Test
    void listAll_whenDatasetsExist_returnsAll() {
        DatasetEntity d1 = dataset(1, "DS1", "desc1", 10, 100);
        DatasetEntity d2 = dataset(2, "DS2", "desc2", 10, 100);
        when(datasetRepository.findAll()).thenReturn(List.of(d1, d2));

        List<DatasetEntity> result = service.listAll();

        assertEquals(2, result.size());
        assertEquals("DS1", result.get(0).getName());
        assertEquals("DS2", result.get(1).getName());
    }

    @Test
    void listAll_whenEmpty_returnsEmptyList() {
        when(datasetRepository.findAll()).thenReturn(List.of());

        List<DatasetEntity> result = service.listAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void listByStudy_whenStudyHasDatasets_returnsFiltered() {
        DatasetEntity d = dataset(1, "DS1", "desc", 10, 100);
        when(datasetRepository.findByStudyId(10)).thenReturn(List.of(d));

        List<DatasetEntity> result = service.listByStudy(10);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getStudyId());
    }

    @Test
    void listByStudy_whenNoMatch_returnsEmptyList() {
        when(datasetRepository.findByStudyId(999)).thenReturn(List.of());

        List<DatasetEntity> result = service.listByStudy(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_whenFound_returnsEntity() {
        DatasetEntity d = dataset(1, "DS1", "desc", 10, 100);
        when(datasetRepository.findById(1)).thenReturn(Optional.of(d));

        DatasetEntity result = service.getById(1);

        assertEquals(1, result.getDatasetId());
        assertEquals("DS1", result.getName());
    }

    @Test
    void getById_whenNotFound_throwsNoSuchElement() {
        when(datasetRepository.findById(999)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> service.getById(999));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    void create_whenValid_returnsSavedEntity() {
        DatasetEntity saved = dataset(1, "New", "New desc", 10, 100);
        when(datasetRepository.save(any(DatasetEntity.class))).thenReturn(saved);

        DatasetEntity result = service.create("New", "New desc", 10, 100);

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
        when(datasetRepository.save(any(DatasetEntity.class))).thenReturn(saved);

        DatasetEntity result = service.create("New", null, 10, 100);

        assertEquals("", result.getDescription());
    }

    @Test
    void update_whenFound_savesUpdatedFields() {
        DatasetEntity existing = dataset(1, "Old", "Old desc", 10, 100);
        when(datasetRepository.findById(1)).thenReturn(Optional.of(existing));
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(i -> i.getArgument(0));

        DatasetEntity result = service.update(1, "Updated", "Updated desc");

        assertEquals("Updated", result.getName());
        assertEquals("Updated desc", result.getDescription());
        assertNotNull(result.getDateUpdated());
        verify(datasetRepository).save(any());
    }

    @Test
    void update_whenDescriptionNull_defaultsToEmpty() {
        DatasetEntity existing = dataset(1, "Old", "Old desc", 10, 100);
        when(datasetRepository.findById(1)).thenReturn(Optional.of(existing));
        when(datasetRepository.save(any(DatasetEntity.class))).thenAnswer(i -> i.getArgument(0));

        DatasetEntity result = service.update(1, "Updated", null);

        assertEquals("", result.getDescription());
    }

    @Test
    void update_whenNotFound_throwsNoSuchElement() {
        when(datasetRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.update(999, "name", "desc"));
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
