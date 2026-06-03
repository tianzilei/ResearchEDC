package org.researchedc.module.filter.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.extract.FilterBean;
import org.researchedc.bean.extract.FilterObjectBean;
import org.researchedc.module.filter.entity.FilterEntity;
import org.researchedc.module.filter.repository.FilterRepository;

@ExtendWith(MockitoExtension.class)
class FilterDaoAdapterTest {

    @Mock
    private FilterRepository filterRepository;

    private FilterDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FilterDaoAdapter(filterRepository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        FilterEntity entity = filter(7, "Vitals", "vital signs", Status.AVAILABLE.getId());
        entity.setSqlStatement("sql");
        entity.setDateCreated(LocalDateTime.now());
        entity.setOwnerId(22);
        entity.setUpdateId(33);
        when(filterRepository.findById(7)).thenReturn(Optional.of(entity));

        FilterBean bean = (FilterBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals("Vitals", bean.getName());
        assertEquals("vital signs", bean.getDescription());
        assertEquals("sql", bean.getSQLStatement());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(22, bean.getOwnerId());
        assertEquals(33, bean.getUpdaterId());
        assertTrue(bean.isActive());
    }

    @Test
    void findByPK_whenMissing_returnsInactiveFilterBean() {
        when(filterRepository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(FilterBean.class, bean);
        assertEquals(0, ((FilterBean) bean).getId());
    }

    @Test
    void findAll_returnsAvailableFiltersOnly() {
        when(filterRepository.findByStatusId(Status.AVAILABLE.getId()))
                .thenReturn(List.of(filter(2, "B", "", 1), filter(1, "A", "", 1)));

        ArrayList filters = (ArrayList) adapter.findAll();

        assertEquals(2, filters.size());
        assertEquals(1, ((FilterBean) filters.get(0)).getId());
        assertEquals(2, ((FilterBean) filters.get(1)).getId());
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        FilterEntity saved = filter(11, "Saved", "desc", Status.AVAILABLE.getId());
        saved.setSqlStatement("where");
        when(filterRepository.save(argThat(e -> {
            assertEquals("Saved", e.getName());
            assertEquals("desc", e.getDescription());
            assertEquals("where", e.getSqlStatement());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals(5, e.getOwnerId());
            return true;
        }))).thenReturn(saved);

        FilterBean input = new FilterBean();
        input.setName("Saved");
        input.setDescription("desc");
        input.setSQLStatement("where");
        input.setStatus(Status.AVAILABLE);
        input.setOwnerId(5);

        FilterBean result = (FilterBean) adapter.create(input);

        assertEquals(11, result.getId());
        verify(filterRepository).save(argThat(e -> e.getDateCreated() != null));
    }

    @Test
    void genSQLStatement_preservesLegacyLikeWrapping() {
        ArrayList filters = new ArrayList();
        filters.add(filterObject(100, " like ", "abc"));
        filters.add(filterObject(101, "=", "yes"));

        String sql = adapter.genSQLStatement(null, "and", filters);

        assertEquals(" and subject_id in (select subject_id from extract_data_table where "
                + "(((item_id = 100 and value  like  '%abc%')) and (item_id = 101 and value = 'yes'))", sql);
    }

    @Test
    void genExplanation_preservesLegacyText() {
        ArrayList filters = new ArrayList();
        filters.add(filterObject(100, "=", "yes", "Consent"));
        filters.add(filterObject(101, "!=", "no", "Eligible"));

        ArrayList explanation = adapter.genExplanation(null, "or", filters);

        assertEquals(List.of(
                "This Filter will look for:",
                "A value = yes for question Consent",
                "or ",
                "A value != no for question Eligible"), explanation);
    }

    private static FilterEntity filter(Integer id, String name, String description, Integer statusId) {
        FilterEntity entity = new FilterEntity();
        entity.setFilterId(id);
        entity.setName(name);
        entity.setDescription(description);
        entity.setStatusId(statusId);
        return entity;
    }

    private static FilterObjectBean filterObject(int itemId, String operand, String value) {
        return filterObject(itemId, operand, value, "Item " + itemId);
    }

    private static FilterObjectBean filterObject(int itemId, String operand, String value, String itemName) {
        FilterObjectBean filterObject = new FilterObjectBean();
        filterObject.setItemId(itemId);
        filterObject.setOperand(operand);
        filterObject.setValue(value);
        filterObject.setItemName(itemName);
        return filterObject;
    }
}
