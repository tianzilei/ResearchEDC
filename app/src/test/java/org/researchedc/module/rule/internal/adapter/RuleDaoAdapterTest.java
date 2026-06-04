package org.researchedc.module.rule.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.rule.RuleBean;
import org.researchedc.bean.rule.RuleSetBean;
import org.researchedc.bean.rule.expression.Context;
import org.researchedc.bean.rule.expression.ExpressionBean;
import org.researchedc.module.rule.entity.RuleEntity;
import org.researchedc.module.rule.entity.RuleExpressionEntity;
import org.researchedc.module.rule.repository.RuleExpressionRepository;
import org.researchedc.module.rule.repository.RuleRepository;

@ExtendWith(MockitoExtension.class)
class RuleDaoAdapterTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private RuleExpressionRepository expressionRepository;

    private RuleDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RuleDaoAdapter(ruleRepository, expressionRepository);
    }

    @Test
    void findByPK_whenFound_mapsRuleAndExpression() {
        RuleEntity rule = rule(7, "R1", "desc", "RULE_1", true, 3, Status.AVAILABLE.getId());
        rule.setOwnerId(20);
        rule.setUpdateId(21);
        rule.setDateCreated(LocalDateTime.now());
        when(ruleRepository.findById(7)).thenReturn(Optional.of(rule));
        when(expressionRepository.findById(3)).thenReturn(Optional.of(expression(3, Context.OC_RULES_V1, "I_A = 1")));

        RuleBean bean = (RuleBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals("R1", bean.getName());
        assertEquals("RULE_1", bean.getOid());
        assertEquals("desc", bean.getDescription());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(20, bean.getOwnerId());
        assertEquals(21, bean.getUpdaterId());
        assertEquals(3, bean.getExpression().getId());
        assertEquals("I_A = 1", bean.getExpression().getValue());
        assertEquals(Context.OC_RULES_V1, bean.getExpression().getContext());
    }

    @Test
    void findByOid_whenMissing_returnsNullLikeLegacyDao() {
        when(ruleRepository.findByOcOid("MISSING")).thenReturn(Optional.empty());

        assertNull(adapter.findByOid("MISSING"));
    }

    @Test
    void findByRuleSet_usesRuleSetJoinRepositoryLookup() {
        RuleSetBean ruleSet = new RuleSetBean();
        ruleSet.setId(12);
        when(ruleRepository.findByRuleSetId(12)).thenReturn(List.of(
                rule(2, "B", "", "B", true, null, 1),
                rule(1, "A", "", "A", false, null, 1)));

        ArrayList<RuleBean> result = adapter.findByRuleSet(ruleSet);

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getId());
        assertEquals("B", result.get(0).getName());
        verify(ruleRepository).findByRuleSetId(12);
    }

    @Test
    void create_savesExpressionThenRule() {
        RuleExpressionEntity savedExpression = expression(15, Context.OC_RULES_V1, "I_A = 1");
        when(expressionRepository.save(argThat(e -> {
            assertEquals(Context.OC_RULES_V1.getCode(), e.getContext());
            assertEquals("I_A = 1", e.getValue());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            return true;
        }))).thenReturn(savedExpression);
        when(expressionRepository.findById(15)).thenReturn(Optional.of(savedExpression));
        when(ruleRepository.save(argThat(e -> {
            assertEquals("Created", e.getName());
            assertEquals("created desc", e.getDescription());
            assertEquals("RULE_CREATED", e.getOcOid());
            assertEquals(true, e.getEnabled());
            assertEquals(15, e.getRuleExpressionId());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            return true;
        }))).thenReturn(rule(22, "Created", "created desc", "RULE_CREATED", true, 15, 1));

        RuleBean input = new RuleBean();
        input.setName("Created");
        input.setDescription("created desc");
        input.setOid("RULE_CREATED");
        input.setEnabled(true);
        input.setOwnerId(9);
        input.setExpression(expressionBean(0, Context.OC_RULES_V1, "I_A = 1"));

        RuleBean result = (RuleBean) adapter.create(input);

        assertEquals(22, result.getId());
        assertEquals(15, result.getExpression().getId());
        verify(ruleRepository).save(argThat(e -> e.getDateCreated() != null));
    }

    @Test
    void update_preservesIdAndMarksResultActive() {
        RuleEntity existing = rule(8, "Old", "old", "RULE_OLD", true, 4, 1);
        RuleExpressionEntity existingExpression = expression(4, Context.OC_RULES_V1, "old");
        when(ruleRepository.findById(8)).thenReturn(Optional.of(existing));
        when(expressionRepository.findById(4)).thenReturn(Optional.of(existingExpression));
        when(expressionRepository.save(argThat(e -> {
            assertEquals(4, e.getRuleExpressionId());
            assertEquals("new", e.getValue());
            return true;
        }))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ruleRepository.save(argThat(e -> {
            assertEquals(8, e.getRuleId());
            assertEquals("Updated", e.getName());
            assertEquals(4, e.getRuleExpressionId());
            assertEquals(99, e.getUpdateId());
            return true;
        }))).thenAnswer(invocation -> invocation.getArgument(0));

        RuleBean input = new RuleBean();
        input.setId(8);
        input.setName("Updated");
        input.setDescription("new desc");
        input.setOid("RULE_OLD");
        input.setEnabled(false);
        input.setUpdaterId(99);
        input.setExpression(expressionBean(4, Context.OC_RULES_V1, "new"));

        RuleBean result = (RuleBean) adapter.update(input);

        assertEquals("Updated", result.getName());
        assertEquals(4, result.getExpression().getId());
        assertEquals(true, result.isActive());
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRows() {
        HashMap row = new HashMap();
        row.put("rule_id", 30);
        row.put("name", "From row");
        row.put("description", "row desc");
        row.put("oc_oid", "RULE_ROW");
        row.put("enabled", true);
        row.put("rule_expression_id", 31);
        row.put("owner_id", 32);
        row.put("update_id", 33);
        row.put("status_id", Status.AVAILABLE.getId());
        when(expressionRepository.findById(31)).thenReturn(Optional.of(expression(31, Context.OC_RULES_V1, "row")));

        RuleBean bean = (RuleBean) adapter.getEntityFromHashMap(row);

        assertEquals(30, bean.getId());
        assertEquals("From row", bean.getName());
        assertEquals(31, bean.getExpression().getId());
    }

    private static RuleEntity rule(Integer id, String name, String description, String oid, Boolean enabled,
                                   Integer expressionId, Integer statusId) {
        RuleEntity entity = new RuleEntity();
        entity.setRuleId(id);
        entity.setName(name);
        entity.setDescription(description);
        entity.setOcOid(oid);
        entity.setEnabled(enabled);
        entity.setRuleExpressionId(expressionId);
        entity.setStatusId(statusId);
        return entity;
    }

    private static RuleExpressionEntity expression(Integer id, Context context, String value) {
        RuleExpressionEntity entity = new RuleExpressionEntity();
        entity.setRuleExpressionId(id);
        entity.setContext(context.getCode());
        entity.setValue(value);
        entity.setStatusId(Status.AVAILABLE.getId());
        return entity;
    }

    private static ExpressionBean expressionBean(int id, Context context, String value) {
        ExpressionBean bean = new ExpressionBean();
        bean.setId(id);
        bean.setContext(context);
        bean.setValue(value);
        return bean;
    }
}
