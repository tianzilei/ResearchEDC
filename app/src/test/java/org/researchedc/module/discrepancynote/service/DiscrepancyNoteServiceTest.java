package org.researchedc.module.discrepancynote.service;

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
import org.researchedc.module.discrepancynote.entity.DiscrepancyNoteEntity;
import org.researchedc.module.discrepancynote.repository.DiscrepancyNoteRepository;

@ExtendWith(MockitoExtension.class)
class DiscrepancyNoteServiceTest {

    @Mock private DiscrepancyNoteRepository discrepancyNoteRepository;
    private DiscrepancyNoteService service;

    @BeforeEach
    void setUp() {
        service = new DiscrepancyNoteService(discrepancyNoteRepository);
    }

    @Test
    void listByStudy_whenStudyHasNotes_returnsFiltered() {
        DiscrepancyNoteEntity dn = dn(1, "Missing data", 10, 20);
        dn.setStudyId(10);
        when(discrepancyNoteRepository.findByStudyId(10)).thenReturn(List.of(dn));

        List<DiscrepancyNoteEntity> result = service.listByStudy(10);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getStudyId());
    }

    @Test
    void listByStudy_whenNoMatch_returnsEmptyList() {
        when(discrepancyNoteRepository.findByStudyId(999)).thenReturn(List.of());

        List<DiscrepancyNoteEntity> result = service.listByStudy(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_whenFound_returnsEntity() {
        DiscrepancyNoteEntity dn = dn(1, "Issue", 10, 20);
        when(discrepancyNoteRepository.findById(1)).thenReturn(Optional.of(dn));

        DiscrepancyNoteEntity result = service.getById(1);

        assertEquals(1, result.getDiscrepancyNoteId());
        assertEquals("Issue", result.getDescription());
    }

    @Test
    void getById_whenNotFound_throwsNoSuchElement() {
        when(discrepancyNoteRepository.findById(999)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> service.getById(999));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    void create_whenValid_returnsSavedEntity() {
        DiscrepancyNoteEntity saved = dn(1, "desc", 10, 20);
        when(discrepancyNoteRepository.save(any(DiscrepancyNoteEntity.class))).thenReturn(saved);

        DiscrepancyNoteEntity result = service.create(
                "desc", 10, 20, "detailed", 100, null,
                "itemData", 55, 20, 200);

        assertEquals("desc", result.getDescription());
        assertEquals(10, result.getDiscrepancyNoteTypeId());
        assertEquals(20, result.getResolutionStatusId());
        verify(discrepancyNoteRepository).save(argThat(e -> {
            assertEquals("desc", e.getDescription());
            assertEquals(10, e.getDiscrepancyNoteTypeId());
            assertEquals(100, e.getOwnerId());
            assertEquals("itemData", e.getEntityType());
            assertEquals(55, e.getEntityId());
            assertEquals(20, e.getStudyId());
            assertEquals(200, e.getAssignedUserId());
            assertNotNull(e.getDateCreated());
            return true;
        }));
    }

    @Test
    void create_withParentDnId_setsParent() {
        DiscrepancyNoteEntity saved = dn(2, "follow-up", 10, 20);
        saved.setParentDnId(1);
        when(discrepancyNoteRepository.save(any(DiscrepancyNoteEntity.class))).thenReturn(saved);

        DiscrepancyNoteEntity result = service.create(
                "follow-up", 10, 20, "notes", 100, 1,
                "itemData", 55, 20, 200);

        assertEquals(1, result.getParentDnId());
    }

    @Test
    void resolveNote_whenFound_setsResolutionStatus5() {
        DiscrepancyNoteEntity existing = dn(1, "Issue", 10, 1);
        when(discrepancyNoteRepository.findById(1)).thenReturn(Optional.of(existing));
        when(discrepancyNoteRepository.save(any(DiscrepancyNoteEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        DiscrepancyNoteEntity result = service.resolveNote(1);

        assertEquals(5, result.getResolutionStatusId());
        verify(discrepancyNoteRepository).save(any());
    }

    @Test
    void resolveNote_whenNotFound_throwsNoSuchElement() {
        when(discrepancyNoteRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.resolveNote(999));
    }

    private static DiscrepancyNoteEntity dn(Integer id, String description,
                                            Integer typeId, Integer resolutionStatusId) {
        DiscrepancyNoteEntity e = new DiscrepancyNoteEntity();
        e.setDiscrepancyNoteId(id);
        e.setDescription(description);
        e.setDiscrepancyNoteTypeId(typeId);
        e.setResolutionStatusId(resolutionStatusId);
        e.setOwnerId(100);
        e.setStudyId(20);
        return e;
    }
}
