package org.researchedc.module.crf.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.module.crf.entity.ItemFormMetadataEntity;
import org.researchedc.module.crf.repository.ItemFormMetadataRepository;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class ItemFormMetadataDaoAdapterTest {

    @Mock
    private ItemFormMetadataRepository repository;

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ItemFormMetadataDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ItemFormMetadataDaoAdapter(repository, dataSource);
    }

    @Test
    void setTypesExpected_doesNotThrow() {
        adapter.setTypesExpected();
    }

    @Test
    void getEntityFromHashMap_mapsRowToItemFormMetadataBean() {
        HashMap row = new HashMap();
        row.put("item_form_metadata_id", 10);
        row.put("item_id", 20);
        row.put("crf_version_id", 5);
        row.put("section_id", 3);
        row.put("response_set_id", 7);
        row.put("header", "Header");
        row.put("subheader", "Subheader");
        row.put("parent_id", 0);
        row.put("parent_label", "");
        row.put("column_number", 1);
        row.put("page_number_label", "1");
        row.put("question_number_label", "Q1");
        row.put("left_item_text", "Left");
        row.put("right_item_text", "Right");
        row.put("decision_condition_id", 0);
        row.put("regexp", "");
        row.put("regexp_error_msg", "");
        row.put("ordinal", 1);
        row.put("required", true);
        row.put("default_value", "default");
        row.put("response_layout", "LEFT");
        row.put("width_decimal", "10");
        row.put("show_item", true);

        ItemFormMetadataBean bean = (ItemFormMetadataBean) adapter.getEntityFromHashMap(row);

        assertEquals(10, bean.getId());
        assertEquals(20, bean.getItemId());
        assertEquals(5, bean.getCrfVersionId());
    }

    @Test
    void findByPK_whenFound_returnsItemFormMetadataBean() {
        ItemFormMetadataEntity entity = ifmEntity(7, 20, 5);
        when(repository.findById(7)).thenReturn(Optional.of(entity));

        ItemFormMetadataBean bean = (ItemFormMetadataBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals(20, bean.getItemId());
        assertEquals(5, bean.getCrfVersionId());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyBean() {
        when(repository.findById(404)).thenReturn(Optional.empty());

        ItemFormMetadataBean bean = (ItemFormMetadataBean) adapter.findByPK(404);

        assertEquals(0, bean.getId());
    }

    @Test
    void create_savesAndReturnsBean() {
        ItemFormMetadataEntity saved = ifmEntity(11, 20, 5);
        when(repository.save(any(ItemFormMetadataEntity.class))).thenReturn(saved);

        ItemFormMetadataBean input = new ItemFormMetadataBean();
        input.setItemId(20);
        input.setCrfVersionId(5);

        ItemFormMetadataBean result = (ItemFormMetadataBean) adapter.create(input);

        assertEquals(11, result.getId());
        verify(repository).save(any(ItemFormMetadataEntity.class));
    }

    @Test
    void update_savesUpdatedBean() {
        ItemFormMetadataEntity existing = ifmEntity(7, 20, 5);
        when(repository.findById(7)).thenReturn(Optional.of(existing));
        ItemFormMetadataEntity saved = ifmEntity(7, 21, 5);
        when(repository.save(any(ItemFormMetadataEntity.class))).thenReturn(saved);

        ItemFormMetadataBean input = new ItemFormMetadataBean();
        input.setId(7);
        input.setItemId(21);

        ItemFormMetadataBean result = (ItemFormMetadataBean) adapter.update(input);

        assertEquals(7, result.getId());
        verify(repository).save(any(ItemFormMetadataEntity.class));
    }

    @Test
    void sectionInstantMapInSameSection_returnsEmptyMap() {
        Map<Integer, List<org.researchedc.domain.crfdata.InstantOnChangePairContainer>> result =
                adapter.sectionInstantMapInSameSection(5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByMultiplePKs_returnsMultipleBeans() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(ifmEntity(1, 20, 5)));
        when(repository.findById(2)).thenReturn(Optional.of(ifmEntity(2, 21, 5)));

        ArrayList pks = new ArrayList();
        pks.add(1);
        pks.add(2);

        ArrayList<ItemFormMetadataBean> result = adapter.findByMultiplePKs(pks);

        assertEquals(2, result.size());
    }

    private static ItemFormMetadataEntity ifmEntity(Integer id, Integer itemId, Integer crfVersionId) {
        ItemFormMetadataEntity entity = new ItemFormMetadataEntity();
        entity.setItemFormMetadataId(id);
        entity.setItemId(itemId);
        entity.setCrfVersionId(crfVersionId);
        return entity;
    }
}
