package org.researchedc.module.crf.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.domain.crfdata.DynamicsItemGroupMetadataBean;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class DynamicsItemGroupMetadataDaoAdapterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private DynamicsItemGroupMetadataDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DynamicsItemGroupMetadataDaoAdapter(jdbcTemplate);
    }

    @Test
    void saveOrUpdate_whenExisting_updatesDynamicGroupMetadata() {
        DynamicsItemGroupMetadataBean entity = entity();
        entity.setId(7);

        DynamicsItemGroupMetadataBean result = adapter.saveOrUpdate(entity);

        assertSame(entity, result);
        verify(jdbcTemplate).update(contains("UPDATE dyn_item_group_metadata"),
                eq(false), eq(11), eq(22), eq(33), eq(1), eq(7));
    }

    @Test
    void saveOrUpdate_whenNew_insertsDynamicGroupMetadata() {
        DynamicsItemGroupMetadataBean entity = entity();

        DynamicsItemGroupMetadataBean result = adapter.saveOrUpdate(entity);

        assertSame(entity, result);
        verify(jdbcTemplate).update(contains("INSERT INTO dyn_item_group_metadata"),
                eq(false), eq(11), eq(22), eq(33), eq(1));
    }

    private DynamicsItemGroupMetadataBean entity() {
        DynamicsItemGroupMetadataBean entity = new DynamicsItemGroupMetadataBean();
        entity.setShowGroup(false);
        entity.setEventCrfId(11);
        entity.setItemGroupMetadataId(22);
        entity.setItemGroupId(33);
        entity.setPassedDde(1);
        return entity;
    }
}
