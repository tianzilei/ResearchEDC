package org.researchedc.module.datacapture.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.module.datacapture.entity.ItemGroupEntity;
import org.researchedc.module.datacapture.repository.ItemGroupRepository;

@ExtendWith(MockitoExtension.class)
class ItemGroupDaoAdapterTest {

    @Mock
    private ItemGroupRepository itemGroupRepository;

    private ItemGroupDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ItemGroupDaoAdapter(itemGroupRepository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        ItemGroupEntity entity = group(7, "Vitals", 3, Status.AVAILABLE.getId());
        entity.setOcOid("IG_VITALS");
        entity.setDateCreated(created);
        entity.setDateUpdated(created.plusHours(1));
        entity.setOwnerId(20);
        entity.setUpdateId(21);
        when(itemGroupRepository.findById(7)).thenReturn(Optional.of(entity));

        ItemGroupBean bean = (ItemGroupBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals("Vitals", bean.getName());
        assertEquals(3, bean.getCrfId());
        assertEquals("IG_VITALS", bean.getOid());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(20, bean.getOwnerId());
        assertEquals(21, bean.getUpdaterId());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyItemGroupBean() {
        when(itemGroupRepository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(ItemGroupBean.class, bean);
        assertEquals(0, ((ItemGroupBean) bean).getId());
    }

    @Test
    void findByName_returnsFirstMatchOrEmptyBean() {
        when(itemGroupRepository.findByName("Vitals"))
                .thenReturn(List.of(group(8, "Vitals", 3, Status.AVAILABLE.getId())));

        ItemGroupBean bean = (ItemGroupBean) adapter.findByName("Vitals");

        assertEquals(8, bean.getId());
    }

    @Test
    void findByOid_whenMissing_returnsNullLikeLegacyDao() {
        when(itemGroupRepository.findByOcOid("UNKNOWN")).thenReturn(List.of());

        assertNull(adapter.findByOid("UNKNOWN"));
    }

    @Test
    void findAllByOid_sortsByItemGroupId() {
        when(itemGroupRepository.findByOcOid("IG"))
                .thenReturn(List.of(group(3, "C", 1, 1), group(1, "A", 1, 1)));

        List<ItemGroupBean> groups = adapter.findAllByOid("IG");

        assertEquals(2, groups.size());
        assertEquals(1, groups.get(0).getId());
        assertEquals(3, groups.get(1).getId());
    }

    @Test
    void findGroupByCRFVersionID_delegatesToNativeRepositoryLookup() {
        when(itemGroupRepository.findGroupByCRFVersionIdNative(5))
                .thenReturn(List.of(group(9, "Vitals", 3, Status.AVAILABLE.getId())));

        List<ItemGroupBean> groups = adapter.findGroupByCRFVersionID(5);

        assertEquals(1, groups.size());
        verify(itemGroupRepository).findGroupByCRFVersionIdNative(5);
    }

    @Test
    void findOnlyGroupsByCRFVersionID_delegatesToNativeRepositoryLookup() {
        when(itemGroupRepository.findOnlyGroupsByCRFVersionIdNative(5))
                .thenReturn(List.of(group(9, "Vitals", 3, Status.AVAILABLE.getId())));

        List<ItemGroupBean> groups = adapter.findOnlyGroupsByCRFVersionID(5);

        assertEquals(1, groups.size());
        verify(itemGroupRepository).findOnlyGroupsByCRFVersionIdNative(5);
    }

    @Test
    void findGroupBySectionId_delegatesToNativeRepositoryLookup() {
        when(itemGroupRepository.findGroupBySectionIdNative(12))
                .thenReturn(List.of(group(10, "Labs", 3, Status.AVAILABLE.getId())));

        List<ItemGroupBean> groups = adapter.findGroupBySectionId(12);

        assertEquals(1, groups.size());
        verify(itemGroupRepository).findGroupBySectionIdNative(12);
    }

    @Test
    void findGroupByGroupNameAndCrfVersionId_returnsFirstMatchOrNull() {
        when(itemGroupRepository.findGroupByGroupNameAndCrfVersionIdNative(5, "Vitals"))
                .thenReturn(List.of(group(11, "Vitals", 3, Status.AVAILABLE.getId())));

        ItemGroupBean group = adapter.findGroupByGroupNameAndCrfVersionId("Vitals", 5);

        assertEquals(11, group.getId());
        verify(itemGroupRepository).findGroupByGroupNameAndCrfVersionIdNative(5, "Vitals");
    }

    @Test
    void findTopOneGroupBySectionId_returnsEmptyBeanWhenMissing() {
        when(itemGroupRepository.findTopOneGroupBySectionIdNative(12)).thenReturn(List.of());

        ItemGroupBean group = adapter.findTopOneGroupBySectionId(12);

        assertEquals(0, group.getId());
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        ItemGroupEntity saved = group(15, "Created", 6, Status.AVAILABLE.getId());
        when(itemGroupRepository.save(argThat(e -> {
            assertEquals("Created", e.getName());
            assertEquals(6, e.getCrfId());
            assertEquals("IG_CREATED", e.getOcOid());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals(30, e.getOwnerId());
            assertEquals(31, e.getUpdateId());
            return e.getDateCreated() != null;
        }))).thenReturn(saved);

        ItemGroupBean input = new ItemGroupBean();
        input.setName("Created");
        input.setCrfId(6);
        input.setOid("IG_CREATED");
        input.setStatus(Status.AVAILABLE);
        input.setOwnerId(30);
        input.setUpdaterId(31);

        ItemGroupBean result = (ItemGroupBean) adapter.create(input);

        assertEquals(15, result.getId());
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        Date now = new Date();
        HashMap row = new HashMap();
        row.put("item_group_id", 50);
        row.put("name", "Row group");
        row.put("crf_id", 51);
        row.put("oc_oid", "IG_ROW");
        row.put("status_id", Status.AVAILABLE.getId());
        row.put("date_created", now);
        row.put("date_updated", now);
        row.put("owner_id", 52);
        row.put("update_id", 53);

        ItemGroupBean bean = (ItemGroupBean) adapter.getEntityFromHashMap(row);

        assertEquals(50, bean.getId());
        assertEquals("Row group", bean.getName());
        assertEquals(51, bean.getCrfId());
        assertEquals("IG_ROW", bean.getOid());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(52, bean.getOwnerId());
        assertEquals(53, bean.getUpdaterId());
    }

    private static ItemGroupEntity group(Integer id, String name, Integer crfId, Integer statusId) {
        ItemGroupEntity entity = new ItemGroupEntity();
        entity.setItemGroupId(id);
        entity.setName(name);
        entity.setCrfId(crfId);
        entity.setStatusId(statusId);
        return entity;
    }
}
