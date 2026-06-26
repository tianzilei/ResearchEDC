package org.researchedc.module.rule.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.researchedc.module.rule.entity.RuleEntity;
import org.researchedc.module.rule.entity.RuleExpressionEntity;
import org.researchedc.module.rule.entity.RuleSetEntity;
import org.researchedc.module.rule.entity.RuleSetRuleEntity;
import org.researchedc.module.rule.repository.RuleExpressionRepository;
import org.researchedc.module.rule.repository.RuleRepository;
import org.researchedc.module.rule.repository.RuleSetRepository;
import org.researchedc.module.rule.repository.RuleSetRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock private RuleSetRepository ruleSetRepository;
    @Mock private RuleSetRuleRepository ruleSetRuleRepository;
    @Mock private RuleRepository ruleRepository;
    @Mock private RuleExpressionRepository ruleExpressionRepository;

    private RuleService service;

    @BeforeEach
    void setUp() {
        service = new RuleService(ruleSetRepository, ruleSetRuleRepository,
                ruleRepository, ruleExpressionRepository);
    }

    // --- listAllRuleSets ---

    @Test
    void listAllRuleSets_returnsAll() {
        RuleSetEntity rs1 = createRuleSetEntity(1, 10);
        RuleSetEntity rs2 = createRuleSetEntity(2, 10);
        when(ruleSetRepository.findAll()).thenReturn(List.of(rs1, rs2));

        List<RuleSetEntity> result = service.listAllRuleSets();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getRuleSetId());
        assertEquals(2, result.get(1).getRuleSetId());
    }

    // --- listRuleSetsByStudy ---

    @Test
    void listRuleSetsByStudy_returnsFiltered() {
        RuleSetEntity rs = createRuleSetEntity(1, 10);
        when(ruleSetRepository.findByStudyIdOrderByRuleSetId(10)).thenReturn(List.of(rs));

        List<RuleSetEntity> result = service.listRuleSetsByStudy(10);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getRuleSetId());
        verify(ruleSetRepository).findByStudyIdOrderByRuleSetId(10);
    }

    @Test
    void listRuleSetsByStudy_noMatches_returnsEmpty() {
        when(ruleSetRepository.findByStudyIdOrderByRuleSetId(999)).thenReturn(List.of());

        assertTrue(service.listRuleSetsByStudy(999).isEmpty());
    }

    // --- getRuleSet ---

    @Test
    void getRuleSet_whenFound_returnsRuleSet() {
        RuleSetEntity rs = createRuleSetEntity(1, 10);
        when(ruleSetRepository.findById(1)).thenReturn(Optional.of(rs));

        RuleSetEntity result = service.getRuleSet(1);

        assertEquals(1, result.getRuleSetId());
        assertEquals(10, result.getStudyId());
    }

    @Test
    void getRuleSet_whenNotFound_throwsException() {
        when(ruleSetRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getRuleSet(99));
    }

    // --- listRuleSetRules ---

    @Test
    void listRuleSetRules_returnsRules() {
        RuleSetRuleEntity r1 = createRuleSetRuleEntity(1, 10, 100);
        RuleSetRuleEntity r2 = createRuleSetRuleEntity(2, 10, 200);
        when(ruleSetRuleRepository.findByRuleSetId(10)).thenReturn(List.of(r1, r2));

        List<RuleSetRuleEntity> result = service.listRuleSetRules(10);

        assertEquals(2, result.size());
        assertEquals(100, result.get(0).getRuleId());
        assertEquals(200, result.get(1).getRuleId());
    }

    // --- getRule ---

    @Test
    void getRule_whenFound_returnsRule() {
        RuleEntity rule = createRuleEntity(1, "Check Age", "Validate age", true, 10);
        when(ruleRepository.findById(1)).thenReturn(Optional.of(rule));

        RuleEntity result = service.getRule(1);

        assertEquals(1, result.getRuleId());
        assertEquals("Check Age", result.getName());
    }

    @Test
    void getRule_whenNotFound_throwsException() {
        when(ruleRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getRule(99));
    }

    // --- getRuleExpression ---

    @Test
    void getRuleExpression_whenFound_returnsExpression() {
        RuleExpressionEntity expr = createRuleExpressionEntity(1, "SE_AGE > 18", 10);
        when(ruleExpressionRepository.findById(1)).thenReturn(Optional.of(expr));

        RuleExpressionEntity result = service.getRuleExpression(1);

        assertEquals(1, result.getRuleExpressionId());
        assertEquals("SE_AGE > 18", result.getValue());
    }

    @Test
    void getRuleExpression_whenNotFound_throwsException() {
        when(ruleExpressionRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getRuleExpression(99));
    }

    // --- createRule ---

    @Test
    void createRule_savesExpressionThenRule() {
        when(ruleExpressionRepository.save(any(RuleExpressionEntity.class)))
                .thenAnswer(i -> {
                    RuleExpressionEntity e = i.getArgument(0);
                    e.setRuleExpressionId(50);
                    return e;
                });
        when(ruleRepository.save(any(RuleEntity.class)))
                .thenAnswer(i -> {
                    RuleEntity e = i.getArgument(0);
                    e.setRuleId(1);
                    return e;
                });

        RuleEntity result = service.createRule("Check BMI", "BMI validation", true,
                "SE_BMI >= 18.5", 10, 42);

        assertEquals(1, result.getRuleId());
        assertEquals("Check BMI", result.getName());
        assertEquals("BMI validation", result.getDescription());
        assertEquals(true, result.getEnabled());
        assertEquals(50, result.getRuleExpressionId());
        verify(ruleExpressionRepository).save(any(RuleExpressionEntity.class));
        verify(ruleRepository).save(any(RuleEntity.class));
    }

    // --- updateRule ---

    @Test
    void updateRule_updatesRuleAndExpression() {
        RuleEntity existing = createRuleEntity(1, "Old Name", "Old desc", true, 10);
        RuleExpressionEntity existingExpr = createRuleExpressionEntity(10, "OLD_EXPR", 5);
        when(ruleRepository.findById(1)).thenReturn(Optional.of(existing));
        when(ruleExpressionRepository.findById(10)).thenReturn(Optional.of(existingExpr));
        when(ruleRepository.save(any(RuleEntity.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(ruleExpressionRepository.save(any(RuleExpressionEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        RuleEntity result = service.updateRule(1, "New Name", "New desc", false,
                "NEW_EXPR", 20, 42);

        assertEquals("New Name", result.getName());
        assertEquals("New desc", result.getDescription());
        assertEquals(false, result.getEnabled());
        assertEquals("NEW_EXPR", existingExpr.getValue());
        assertEquals(20, existingExpr.getContext());
        verify(ruleRepository).save(existing);
        verify(ruleExpressionRepository).save(existingExpr);
    }

    @Test
    void updateRule_whenRuleNotFound_throwsException() {
        when(ruleRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.updateRule(99, "n", "d", true, "v", 1, 1));
    }

    @Test
    void updateRule_whenExpressionNotFound_throwsException() {
        RuleEntity existing = createRuleEntity(1, "Name", "desc", true, 10);
        when(ruleRepository.findById(1)).thenReturn(Optional.of(existing));
        when(ruleExpressionRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.updateRule(1, "n", "d", true, "v", 1, 1));
    }

    // --- deleteRule ---

    @Test
    void deleteRule_deletesExisting() {
        RuleEntity existing = createRuleEntity(1, "ToDelete", "desc", true, 10);
        when(ruleRepository.findById(1)).thenReturn(Optional.of(existing));

        service.deleteRule(1);

        verify(ruleRepository).delete(existing);
    }

    @Test
    void deleteRule_whenNotFound_throwsException() {
        when(ruleRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.deleteRule(99));
    }

    // --- factory methods ---

    private static RuleSetEntity createRuleSetEntity(Integer id, Integer studyId) {
        RuleSetEntity e = new RuleSetEntity();
        e.setRuleSetId(id);
        e.setStudyId(studyId);
        return e;
    }

    private static RuleSetRuleEntity createRuleSetRuleEntity(Integer id, Integer ruleSetId, Integer ruleId) {
        RuleSetRuleEntity e = new RuleSetRuleEntity();
        e.setRuleSetRuleId(id);
        e.setRuleSetId(ruleSetId);
        e.setRuleId(ruleId);
        return e;
    }

    private static RuleEntity createRuleEntity(Integer id, String name, String description,
                                                Boolean enabled, Integer expressionId) {
        RuleEntity e = new RuleEntity();
        e.setRuleId(id);
        e.setName(name);
        e.setDescription(description);
        e.setEnabled(enabled);
        e.setRuleExpressionId(expressionId);
        return e;
    }

    private static RuleExpressionEntity createRuleExpressionEntity(Integer id, String value,
                                                                    Integer context) {
        RuleExpressionEntity e = new RuleExpressionEntity();
        e.setRuleExpressionId(id);
        e.setValue(value);
        e.setContext(context);
        return e;
    }
}
