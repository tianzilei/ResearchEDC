package org.researchedc.module.filter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.module.filter.entity.FilterEntity;
import org.researchedc.module.filter.repository.FilterRepository;

@ExtendWith(MockitoExtension.class)
class FilterServiceTest {

    @Mock private FilterRepository filterRepository;
    private FilterService service;

    @BeforeEach
    void setUp() {
        service = new FilterService(filterRepository);
    }

    @Test
    void listAll_whenFiltersExist_returnsAll() {
        FilterEntity f1 = filter(1, "Active", "active filter", 100);
        FilterEntity f2 = filter(2, "Closed", "closed filter", 100);
        when(filterRepository.findAll()).thenReturn(List.of(f1, f2));

        List<FilterEntity> result = service.listAll();

        assertEquals(2, result.size());
        assertEquals("Active", result.get(0).getName());
        assertEquals("Closed", result.get(1).getName());
    }

    @Test
    void listAll_whenEmpty_returnsEmptyList() {
        when(filterRepository.findAll()).thenReturn(List.of());

        List<FilterEntity> result = service.listAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_whenFound_returnsEntity() {
        FilterEntity f = filter(1, "MyFilter", "desc", 100);
        when(filterRepository.findById(1)).thenReturn(Optional.of(f));

        FilterEntity result = service.getById(1);

        assertEquals(1, result.getFilterId());
        assertEquals("MyFilter", result.getName());
    }

    @Test
    void getById_whenNotFound_throwsNoSuchElement() {
        when(filterRepository.findById(999)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> service.getById(999));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    void create_whenValid_returnsSavedEntity() {
        FilterEntity saved = filter(1, "New", "new filter", 100);
        when(filterRepository.save(any(FilterEntity.class))).thenReturn(saved);

        FilterEntity result = service.create("New", "new filter", 100);

        assertEquals("New", result.getName());
        assertEquals("new filter", result.getDescription());
        verify(filterRepository).save(argThat(e -> {
            assertEquals("New", e.getName());
            assertEquals(1, e.getStatusId());
            assertEquals(100, e.getOwnerId());
            return true;
        }));
    }

    @Test
    void create_whenDescriptionNull_defaultsToEmpty() {
        FilterEntity saved = filter(1, "New", "", 100);
        when(filterRepository.save(any(FilterEntity.class))).thenReturn(saved);

        FilterEntity result = service.create("New", null, 100);

        assertEquals("", result.getDescription());
    }

    @Test
    void update_whenFound_savesUpdatedFields() {
        FilterEntity existing = filter(1, "Old", "old desc", 100);
        when(filterRepository.findById(1)).thenReturn(Optional.of(existing));
        when(filterRepository.save(any(FilterEntity.class))).thenAnswer(i -> i.getArgument(0));

        FilterEntity result = service.update(1, "Updated", "Updated desc");

        assertEquals("Updated", result.getName());
        assertEquals("Updated desc", result.getDescription());
        assertNotNull(result.getDateUpdated());
        verify(filterRepository).save(any());
    }

    @Test
    void update_whenDescriptionNull_defaultsToEmpty() {
        FilterEntity existing = filter(1, "Old", "old desc", 100);
        when(filterRepository.findById(1)).thenReturn(Optional.of(existing));
        when(filterRepository.save(any(FilterEntity.class))).thenAnswer(i -> i.getArgument(0));

        FilterEntity result = service.update(1, "Updated", null);

        assertEquals("", result.getDescription());
    }

    @Test
    void update_whenNotFound_throwsNoSuchElement() {
        when(filterRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.update(999, "name", "desc"));
    }

    private static FilterEntity filter(Integer id, String name, String description,
                                       Integer ownerId) {
        FilterEntity e = new FilterEntity();
        e.setFilterId(id);
        e.setName(name);
        e.setDescription(description);
        e.setOwnerId(ownerId);
        e.setStatusId(1);
        return e;
    }
}
