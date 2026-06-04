package org.researchedc.module.discrepancynote.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.module.discrepancynote.entity.DiscrepancyNoteEntity;
import org.researchedc.module.discrepancynote.repository.DiscrepancyNoteRepository;

@ExtendWith(MockitoExtension.class)
class DiscrepancyNoteDaoAdapterTest {

    @Mock
    private DiscrepancyNoteRepository discrepancyNoteRepository;

    private DiscrepancyNoteDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DiscrepancyNoteDaoAdapter(discrepancyNoteRepository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        DiscrepancyNoteEntity entity = note(7, "Query", 2, 1, 5);
        entity.setDetailedNotes("details");
        entity.setDateCreated(created);
        entity.setOwnerId(20);
        entity.setParentDnId(3);
        entity.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
        entity.setEntityId(22);
        entity.setStudyId(9);
        entity.setAssignedUserId(11);
        when(discrepancyNoteRepository.findById(7)).thenReturn(Optional.of(entity));

        DiscrepancyNoteBean bean = (DiscrepancyNoteBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals("Query", bean.getDescription());
        assertEquals(2, bean.getDiscrepancyNoteTypeId());
        assertEquals(1, bean.getResolutionStatusId());
        assertEquals("details", bean.getDetailedNotes());
        assertEquals(20, bean.getOwnerId());
        assertEquals(3, bean.getParentDnId());
        assertEquals(DiscrepancyNoteBean.ITEM_DATA, bean.getEntityType());
        assertEquals(22, bean.getEntityId());
        assertEquals(9, bean.getStudyId());
        assertEquals(11, bean.getAssignedUserId());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyDiscrepancyNoteBean() {
        when(discrepancyNoteRepository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(DiscrepancyNoteBean.class, bean);
        assertEquals(0, ((DiscrepancyNoteBean) bean).getId());
    }

    @Test
    void findAllParentsByStudy_usesParentNullLookupAndSortsById() {
        StudyBean study = new StudyBean();
        study.setId(12);
        when(discrepancyNoteRepository.findByStudyIdAndParentDnIdIsNull(12))
                .thenReturn(List.of(note(5, "B", 1, 1, 12), note(2, "A", 1, 1, 12)));

        ArrayList notes = adapter.findAllParentsByStudy(study);

        assertEquals(2, notes.size());
        assertEquals(2, ((DiscrepancyNoteBean) notes.get(0)).getId());
        assertEquals(5, ((DiscrepancyNoteBean) notes.get(1)).getId());
        verify(discrepancyNoteRepository).findByStudyIdAndParentDnIdIsNull(12);
    }

    @Test
    void findAllParentItemNotesByEventCRF_usesItemDataEntityTypeAndParentNullLookup() {
        when(discrepancyNoteRepository.findByEntityTypeAndEntityIdAndParentDnIdIsNull("itemData", 44))
                .thenReturn(List.of(note(8, "Parent", 1, 1, 3)));

        ArrayList<DiscrepancyNoteBean> notes = adapter.findAllParentItemNotesByEventCRF(44);

        assertEquals(1, notes.size());
        assertEquals(8, notes.get(0).getId());
        verify(discrepancyNoteRepository).findByEntityTypeAndEntityIdAndParentDnIdIsNull("itemData", 44);
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        DiscrepancyNoteEntity saved = note(15, "Created", 4, 1, 6);
        saved.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
        saved.setEntityId(70);
        when(discrepancyNoteRepository.save(argThat(e -> {
            assertEquals("Created", e.getDescription());
            assertEquals(4, e.getDiscrepancyNoteTypeId());
            assertEquals(1, e.getResolutionStatusId());
            assertEquals("created detail", e.getDetailedNotes());
            assertEquals(6, e.getStudyId());
            assertEquals(61, e.getOwnerId());
            assertEquals(62, e.getParentDnId());
            assertEquals(DiscrepancyNoteBean.ITEM_DATA, e.getEntityType());
            assertEquals(70, e.getEntityId());
            assertEquals(63, e.getAssignedUserId());
            return true;
        }))).thenReturn(saved);

        DiscrepancyNoteBean input = new DiscrepancyNoteBean();
        input.setDescription("Created");
        input.setDiscrepancyNoteTypeId(4);
        input.setResolutionStatusId(1);
        input.setDetailedNotes("created detail");
        input.setStudyId(6);
        input.setOwnerId(61);
        input.setParentDnId(62);
        input.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
        input.setEntityId(70);
        input.setAssignedUserId(63);

        DiscrepancyNoteBean result = (DiscrepancyNoteBean) adapter.create(input);

        assertEquals(15, result.getId());
        verify(discrepancyNoteRepository).save(argThat(e -> e.getDateCreated() != null));
    }

    @Test
    void updateAssignedUser_updatesExistingEntityAndReturnsFreshBean() {
        DiscrepancyNoteEntity existing = note(18, "Existing", 1, 1, 4);
        DiscrepancyNoteEntity refreshed = note(18, "Existing", 1, 1, 4);
        refreshed.setAssignedUserId(91);
        when(discrepancyNoteRepository.findById(18))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(refreshed));
        when(discrepancyNoteRepository.save(argThat(e -> {
            assertEquals(18, e.getDiscrepancyNoteId());
            assertEquals(91, e.getAssignedUserId());
            return true;
        }))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscrepancyNoteBean input = new DiscrepancyNoteBean();
        input.setId(18);
        input.setAssignedUserId(91);

        DiscrepancyNoteBean result = (DiscrepancyNoteBean) adapter.updateAssignedUser(input);

        assertEquals(91, result.getAssignedUserId());
    }

    @Test
    void updateAssignedUserToNull_clearsAssignmentAndReturnsFreshBean() {
        DiscrepancyNoteEntity existing = note(19, "Existing", 1, 1, 4);
        existing.setAssignedUserId(91);
        when(discrepancyNoteRepository.findById(19))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(note(19, "Existing", 1, 1, 4)));
        when(discrepancyNoteRepository.save(argThat(e -> {
            assertEquals(19, e.getDiscrepancyNoteId());
            assertEquals(null, e.getAssignedUserId());
            return true;
        }))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscrepancyNoteBean input = new DiscrepancyNoteBean();
        input.setId(19);

        DiscrepancyNoteBean result = (DiscrepancyNoteBean) adapter.updateAssignedUserToNull(input);

        assertEquals(0, result.getAssignedUserId());
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        Date now = new Date();
        HashMap row = new HashMap();
        row.put("discrepancy_note_id", 30);
        row.put("description", "From row");
        row.put("discrepancy_note_type_id", 2);
        row.put("resolution_status_id", 1);
        row.put("detailed_notes", "row details");
        row.put("date_created", now);
        row.put("owner_id", 31);
        row.put("parent_dn_id", 32);
        row.put("entity_type", DiscrepancyNoteBean.ITEM_DATA);
        row.put("entity_id", 33);
        row.put("study_id", 34);
        row.put("assigned_user_id", 35);

        DiscrepancyNoteBean bean = (DiscrepancyNoteBean) adapter.getEntityFromHashMap(row);

        assertEquals(30, bean.getId());
        assertEquals("From row", bean.getDescription());
        assertEquals(2, bean.getDiscrepancyNoteTypeId());
        assertEquals(1, bean.getResolutionStatusId());
        assertEquals("row details", bean.getDetailedNotes());
        assertEquals(31, bean.getOwnerId());
        assertEquals(32, bean.getParentDnId());
        assertEquals(DiscrepancyNoteBean.ITEM_DATA, bean.getEntityType());
        assertEquals(33, bean.getEntityId());
        assertEquals(34, bean.getStudyId());
        assertEquals(35, bean.getAssignedUserId());
    }

    private static DiscrepancyNoteEntity note(Integer id, String description, Integer typeId,
                                              Integer resolutionStatusId, Integer studyId) {
        DiscrepancyNoteEntity entity = new DiscrepancyNoteEntity();
        entity.setDiscrepancyNoteId(id);
        entity.setDescription(description);
        entity.setDiscrepancyNoteTypeId(typeId);
        entity.setResolutionStatusId(resolutionStatusId);
        entity.setStudyId(studyId);
        return entity;
    }
}
