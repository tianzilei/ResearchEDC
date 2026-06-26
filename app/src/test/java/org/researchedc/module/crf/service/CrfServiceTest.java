package org.researchedc.module.crf.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.researchedc.module.crf.dto.CrfSummaryDTO;
import org.researchedc.app.dto.CrfVersionDTO;
import org.researchedc.module.crf.dto.ItemDTO;
import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.entity.ItemFormMetadataEntity;
import org.researchedc.module.crf.entity.SectionEntity;
import org.researchedc.module.crf.internal.adapter.SCDItemMetadataDaoAdapter;
import org.researchedc.module.crf.repository.CrfRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.module.crf.repository.ItemFormMetadataRepository;
import org.researchedc.module.crf.repository.ItemRepository;
import org.researchedc.module.crf.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrfServiceTest {

    @Mock private CrfRepository crfRepository;
    @Mock private CrfVersionRepository crfVersionRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private ItemFormMetadataRepository itemFormMetadataRepository;
    @Mock private SCDItemMetadataDaoAdapter scdAdapter;

    private CrfService service;

    @BeforeEach
    void setUp() {
        service = new CrfService(crfRepository, crfVersionRepository, sectionRepository,
                itemRepository, itemFormMetadataRepository, scdAdapter);
    }

    // --- listCrfs ---

    @Test
    void listCrfs_returnsSummariesWithVersionCount() {
        CrfEntity crf = createCrfEntity(1, "Vital Signs");
        when(crfRepository.findAll()).thenReturn(List.of(crf));
        when(crfVersionRepository.findByCrfIdOrderByCrfVersionId(1))
                .thenReturn(List.of(createCrfVersionEntity(10, 1, "v1"), createCrfVersionEntity(11, 1, "v2")));

        List<CrfSummaryDTO> result = service.listCrfs();

        assertEquals(1, result.size());
        assertEquals("Vital Signs", result.get(0).getName());
        assertEquals(2, result.get(0).getVersionCount());
    }

    @Test
    void listCrfs_empty_returnsEmpty() {
        when(crfRepository.findAll()).thenReturn(List.of());

        assertTrue(service.listCrfs().isEmpty());
    }

    // --- getVersion ---

    @Test
    void getVersion_returnsVersionWithSections() {
        CrfVersionEntity ver = createCrfVersionEntity(1, 1, "v1.0");
        when(crfVersionRepository.findById(1)).thenReturn(Optional.of(ver));

        SectionEntity section = new SectionEntity();
        section.setSectionId(10);
        section.setLabel("Section A");
        section.setTitle("Demographics");
        section.setOrdinal(1);
        when(sectionRepository.findByCrfVersionIdOrderByOrdinal(1)).thenReturn(List.of(section));

        CrfVersionDTO result = service.getVersion(1);

        assertEquals(1, result.getCrfVersionId());
        assertEquals("v1.0", result.getName());
        assertEquals(1, result.getSections().size());
        assertEquals("Demographics", result.getSections().get(0).getTitle());
    }

    @Test
    void getVersion_notFound_returnsNull() {
        when(crfVersionRepository.findById(99)).thenReturn(Optional.empty());

        assertNull(service.getVersion(99));
    }

    // --- getItemsBySection ---

    @Test
    void getItemsBySection_returnsItemsForSection() {
        ItemFormMetadataEntity meta = new ItemFormMetadataEntity();
        meta.setItemId(20);
        meta.setSectionId(10);
        meta.setOrdinal(1);
        meta.setDefaultValue("0");
        meta.setRequired(true);
        when(itemFormMetadataRepository.findByCrfVersionId(1)).thenReturn(List.of(meta));

        ItemEntity item = new ItemEntity();
        item.setItemId(20);
        item.setName("Height");
        item.setDescription("Patient height");
        item.setUnits("cm");
        item.setOcOid("item_height");
        item.setPhiStatus(false);
        when(itemRepository.findById(20)).thenReturn(Optional.of(item));

        List<ItemDTO> result = service.getItemsBySection(10, 1);

        assertEquals(1, result.size());
        assertEquals("Height", result.get(0).getName());
        assertEquals("cm", result.get(0).getUnits());
        assertEquals(1, result.get(0).getOrdinal());
        assertTrue(result.get(0).isRequired());
    }

    @Test
    void getItemsBySection_wrongSection_excluded() {
        ItemFormMetadataEntity meta = new ItemFormMetadataEntity();
        meta.setItemId(20);
        meta.setSectionId(99);
        when(itemFormMetadataRepository.findByCrfVersionId(1)).thenReturn(List.of(meta));

        assertTrue(service.getItemsBySection(10, 1).isEmpty());
    }

    // --- getAllCrfEntities ---

    @Test
    void getAllCrfEntities_returnsAll() {
        CrfEntity crf1 = createCrfEntity(1, "Vital Signs");
        CrfEntity crf2 = createCrfEntity(2, "Demographics");
        when(crfRepository.findAll()).thenReturn(List.of(crf1, crf2));

        List<CrfEntity> result = service.getAllCrfEntities();

        assertEquals(2, result.size());
        assertEquals("Vital Signs", result.get(0).getName());
        assertEquals("Demographics", result.get(1).getName());
    }

    // --- getCrfEntity ---

    @Test
    void getCrfEntity_whenFound_returnsEntity() {
        CrfEntity crf = createCrfEntity(1, "Vital Signs");
        when(crfRepository.findById(1)).thenReturn(Optional.of(crf));

        CrfEntity result = service.getCrfEntity(1);

        assertEquals(1, result.getCrfId());
        assertEquals("Vital Signs", result.getName());
    }

    @Test
    void getCrfEntity_whenNotFound_throwsException() {
        when(crfRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getCrfEntity(99));
    }

    // --- getCrfVersionEntity ---

    @Test
    void getCrfVersionEntity_whenFound_returnsEntity() {
        CrfVersionEntity ver = createCrfVersionEntity(1, 1, "v1.0");
        when(crfVersionRepository.findById(1)).thenReturn(Optional.of(ver));

        CrfVersionEntity result = service.getCrfVersionEntity(1);

        assertEquals(1, result.getCrfVersionId());
        assertEquals("v1.0", result.getName());
    }

    @Test
    void getCrfVersionEntity_whenNotFound_throwsException() {
        when(crfVersionRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getCrfVersionEntity(99));
    }

    // --- listVersionEntities ---

    @Test
    void listVersionEntities_returnsVersionsForCrf() {
        CrfVersionEntity v1 = createCrfVersionEntity(1, 1, "v1.0");
        CrfVersionEntity v2 = createCrfVersionEntity(2, 1, "v2.0");
        when(crfVersionRepository.findByCrfIdOrderByCrfVersionId(1)).thenReturn(List.of(v1, v2));

        List<CrfVersionEntity> result = service.listVersionEntities(1);

        assertEquals(2, result.size());
        assertEquals("v1.0", result.get(0).getName());
        assertEquals("v2.0", result.get(1).getName());
    }

    @Test
    void listVersionEntities_noVersions_returnsEmpty() {
        when(crfVersionRepository.findByCrfIdOrderByCrfVersionId(999)).thenReturn(List.of());

        assertTrue(service.listVersionEntities(999).isEmpty());
    }

    // --- createCrf ---

    @Test
    void createCrf_savesAndReturns() {
        when(crfRepository.save(any(CrfEntity.class)))
                .thenAnswer(i -> {
                    CrfEntity e = i.getArgument(0);
                    e.setCrfId(5);
                    return e;
                });

        CrfEntity result = service.createCrf("Lab Results", "Lab CRF", 42);

        assertEquals(5, result.getCrfId());
        assertEquals("Lab Results", result.getName());
        assertEquals("Lab CRF", result.getDescription());
        assertEquals(1, result.getStatusId());
        assertEquals(42, result.getOwnerId());
        assertNotNull(result.getDateCreated());
        verify(crfRepository).save(any(CrfEntity.class));
    }

    @Test
    void createCrf_nullDescription_defaultsToEmpty() {
        when(crfRepository.save(any(CrfEntity.class)))
                .thenAnswer(i -> {
                    CrfEntity e = i.getArgument(0);
                    e.setCrfId(6);
                    return e;
                });

        CrfEntity result = service.createCrf("Test", null, 42);

        assertEquals("", result.getDescription());
    }

    // --- updateCrf ---

    @Test
    void updateCrf_updatesFieldsAndReturns() {
        CrfEntity existing = createCrfEntity(1, "Old Name");
        when(crfRepository.findById(1)).thenReturn(Optional.of(existing));
        when(crfRepository.save(any(CrfEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        CrfEntity result = service.updateCrf(1, "New Name", "Updated desc");

        assertEquals("New Name", result.getName());
        assertEquals("Updated desc", result.getDescription());
        assertNotNull(result.getDateUpdated());
        verify(crfRepository).save(existing);
    }

    @Test
    void updateCrf_nullDescription_defaultsToEmpty() {
        CrfEntity existing = createCrfEntity(1, "Old Name");
        when(crfRepository.findById(1)).thenReturn(Optional.of(existing));
        when(crfRepository.save(any(CrfEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        CrfEntity result = service.updateCrf(1, "New Name", null);

        assertEquals("", result.getDescription());
    }

    @Test
    void updateCrf_whenNotFound_throwsException() {
        when(crfRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.updateCrf(99, "name", "desc"));
    }

    // --- createVersion ---

    @Test
    void createVersion_savesAndReturns() {
        when(crfVersionRepository.save(any(CrfVersionEntity.class)))
                .thenAnswer(i -> {
                    CrfVersionEntity e = i.getArgument(0);
                    e.setCrfVersionId(10);
                    return e;
                });

        CrfVersionEntity result = service.createVersion(1, "v1.0", "Initial", "First release", 42);

        assertEquals(10, result.getCrfVersionId());
        assertEquals(1, result.getCrfId());
        assertEquals("v1.0", result.getName());
        assertEquals("Initial", result.getDescription());
        assertEquals("First release", result.getRevisionNotes());
        assertEquals(1, result.getStatusId());
        assertEquals(42, result.getOwnerId());
        assertNotNull(result.getDateCreated());
    }

    @Test
    void createVersion_nullDescriptionAndNotes_defaultsToEmpty() {
        when(crfVersionRepository.save(any(CrfVersionEntity.class)))
                .thenAnswer(i -> {
                    CrfVersionEntity e = i.getArgument(0);
                    e.setCrfVersionId(11);
                    return e;
                });

        CrfVersionEntity result = service.createVersion(1, "v2.0", null, null, 42);

        assertEquals("", result.getDescription());
        assertEquals("", result.getRevisionNotes());
    }

    // --- deleteVersion ---

    @Test
    void deleteVersion_deletesExisting() {
        when(crfVersionRepository.existsById(1)).thenReturn(true);

        service.deleteVersion(1);

        verify(crfVersionRepository).deleteById(1);
    }

    @Test
    void deleteVersion_whenNotFound_throwsException() {
        when(crfVersionRepository.existsById(99)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> service.deleteVersion(99));
    }

    // --- factory methods ---

    private static CrfEntity createCrfEntity(Integer id, String name) {
        CrfEntity e = new CrfEntity();
        e.setCrfId(id);
        e.setName(name);
        e.setDescription("desc");
        e.setStatusId(1);
        e.setOwnerId(1);
        return e;
    }

    private static CrfVersionEntity createCrfVersionEntity(Integer id, Integer crfId, String name) {
        CrfVersionEntity e = new CrfVersionEntity();
        e.setCrfVersionId(id);
        e.setCrfId(crfId);
        e.setName(name);
        e.setDescription("desc");
        e.setStatusId(1);
        e.setOwnerId(1);
        return e;
    }
}
