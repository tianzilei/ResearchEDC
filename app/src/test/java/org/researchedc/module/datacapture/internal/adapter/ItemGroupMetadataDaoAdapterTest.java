package org.researchedc.module.datacapture.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.submit.ItemGroupMetadataBean;
import org.researchedc.dao.spi.IItemGroupMetadataDAO;
import org.researchedc.dao.submit.ItemGroupMetadataDAO;
import org.researchedc.module.datacapture.entity.ItemGroupMetadataEntity;
import org.researchedc.module.datacapture.repository.ItemGroupMetadataRepository;

@ExtendWith(MockitoExtension.class)
class ItemGroupMetadataDaoAdapterTest {

    @Mock
    private ItemGroupMetadataRepository itemGroupMetadataRepository;

    private ItemGroupMetadataDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ItemGroupMetadataDaoAdapter(itemGroupMetadataRepository);
    }

    @Test
    void adapterRemainsAssignableToLegacyDaoAndSpi() {
        assertInstanceOf(ItemGroupMetadataDAO.class, adapter);
        assertInstanceOf(IItemGroupMetadataDAO.class, adapter);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        when(itemGroupMetadataRepository.findById(7)).thenReturn(Optional.of(metadata(7, 4, 3, 2)));

        ItemGroupMetadataBean bean = (ItemGroupMetadataBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals(4, bean.getItemGroupId());
        assertEquals(3, bean.getCrfVersionId());
        assertEquals(2, bean.getItemId());
        assertEquals("Header", bean.getHeader());
        assertEquals("Subheader", bean.getSubheader());
        assertEquals("grid", bean.getLayout());
        assertEquals(1, bean.getRepeatNum());
        assertEquals(5, bean.getRepeatMax());
        assertEquals("1,2", bean.getRepeatArray());
        assertEquals(10, bean.getRowStartNumber());
        assertEquals(6, bean.getOrdinal());
        assertEquals(1, bean.getBorders());
        assertTrue(bean.isShowGroup());
        assertTrue(bean.isRepeatingGroup());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyMetadataBean() {
        when(itemGroupMetadataRepository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(ItemGroupMetadataBean.class, bean);
        assertEquals(0, ((ItemGroupMetadataBean) bean).getId());
    }

    @Test
    void findByItemAndCrfVersion_returnsFirstMatch() {
        when(itemGroupMetadataRepository.findByItemIdAndCrfVersionId(2, 3))
                .thenReturn(List.of(metadata(7, 4, 3, 2)));

        ItemGroupMetadataBean bean = (ItemGroupMetadataBean) adapter.findByItemAndCrfVersion(2, 3);

        assertEquals(7, bean.getId());
        verify(itemGroupMetadataRepository).findByItemIdAndCrfVersionId(2, 3);
    }

    @Test
    void findMetaByGroupAndSection_delegatesToRepositoryAndSortsByOrdinal() {
        ItemGroupMetadataEntity later = metadata(8, 4, 3, 2);
        later.setOrdinal(9);
        ItemGroupMetadataEntity earlier = metadata(7, 4, 3, 5);
        earlier.setOrdinal(1);
        when(itemGroupMetadataRepository.findMetaByGroupAndSection(4, 3, 12))
                .thenReturn(List.of(later, earlier));

        List<ItemGroupMetadataBean> beans = adapter.findMetaByGroupAndSection(4, 3, 12);

        assertEquals(2, beans.size());
        assertEquals(7, beans.get(0).getId());
        assertEquals(8, beans.get(1).getId());
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        ItemGroupMetadataEntity saved = metadata(11, 4, 3, 2);
        when(itemGroupMetadataRepository.save(argThat(e -> {
            assertEquals(4, e.getItemGroupId());
            assertEquals(3, e.getCrfVersionId());
            assertEquals(2, e.getItemId());
            assertEquals("Created", e.getHeader());
            assertEquals(6, e.getOrdinal());
            assertEquals(true, e.getShowGroup());
            assertEquals(false, e.getRepeatingGroup());
            return true;
        }))).thenReturn(saved);

        ItemGroupMetadataBean input = new ItemGroupMetadataBean();
        input.setItemGroupId(4);
        input.setCrfVersionId(3);
        input.setItemId(2);
        input.setHeader("Created");
        input.setOrdinal(6);
        input.setShowGroup(true);
        input.setRepeatingGroup(false);

        ItemGroupMetadataBean result = (ItemGroupMetadataBean) adapter.create(input);

        assertEquals(11, result.getId());
        assertEquals(11, input.getId());
    }

    @Test
    void versionIncluded_returnsTrueWhenMetadataExists() {
        when(itemGroupMetadataRepository.findByCrfVersionId(3)).thenReturn(List.of(metadata(7, 4, 3, 2)));

        assertTrue(adapter.versionIncluded(3));
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        HashMap row = new HashMap();
        row.put("item_group_metadata_id", 20);
        row.put("item_group_id", 21);
        row.put("header", "Row header");
        row.put("subheader", "Row subheader");
        row.put("layout", "row-layout");
        row.put("repeat_number", 2);
        row.put("repeat_max", 3);
        row.put("repeat_array", "2,3");
        row.put("row_start_number", 4);
        row.put("crf_version_id", 22);
        row.put("item_id", 23);
        row.put("ordinal", 24);
        row.put("borders", 0);
        row.put("show_group", false);
        row.put("repeating_group", true);

        ItemGroupMetadataBean bean = (ItemGroupMetadataBean) adapter.getEntityFromHashMap(row);

        assertEquals(20, bean.getId());
        assertEquals(21, bean.getItemGroupId());
        assertEquals("Row header", bean.getHeader());
        assertEquals(22, bean.getCrfVersionId());
        assertEquals(23, bean.getItemId());
        assertEquals(24, bean.getOrdinal());
        assertEquals(false, bean.isShowGroup());
        assertTrue(bean.isRepeatingGroup());
    }

    private static ItemGroupMetadataEntity metadata(Integer id, Integer itemGroupId, Integer crfVersionId, Integer itemId) {
        ItemGroupMetadataEntity entity = new ItemGroupMetadataEntity();
        entity.setItemGroupMetadataId(id);
        entity.setItemGroupId(itemGroupId);
        entity.setCrfVersionId(crfVersionId);
        entity.setItemId(itemId);
        entity.setHeader("Header");
        entity.setSubheader("Subheader");
        entity.setLayout("grid");
        entity.setRepeatNumber(1);
        entity.setRepeatMax(5);
        entity.setRepeatArray("1,2");
        entity.setRowStartNumber(10);
        entity.setOrdinal(6);
        entity.setBorders(1);
        entity.setShowGroup(true);
        entity.setRepeatingGroup(true);
        return entity;
    }
}
