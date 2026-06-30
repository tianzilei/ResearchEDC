package org.researchedc.module.rule.repository;

import org.researchedc.module.rule.entity.RuleSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RuleSetRepository extends JpaRepository<RuleSetEntity, Integer> {

    List<RuleSetEntity> findByStudyIdOrderByRuleSetId(Integer studyId);

    List<RuleSetEntity> findByStudyIdInOrderByStudyIdAscRuleSetIdAsc(Set<Integer> studyIds);

    List<RuleSetEntity> findByCrfIdAndStudyId(Integer crfId, Integer studyId);

    List<RuleSetEntity> findByCrfVersionIdAndStudyIdAndStudyEventDefinitionId(
            Integer crfVersionId, Integer studyId, Integer studyEventDefinitionId);

    List<RuleSetEntity> findByCrfIdAndStudyIdAndStudyEventDefinitionId(
            Integer crfId, Integer studyId, Integer studyEventDefinitionId);

    Optional<RuleSetEntity> findByRuleExpressionId(Integer ruleExpressionId);

    List<RuleSetEntity> findByStudyEventDefinitionId(Integer studyEventDefinitionId);

    @Query(value = """
            SELECT rs.*
            FROM module_rule_set rs
            WHERE (rs.crf_version_id = ?1 OR rs.crf_id = ?2)
              AND rs.study_id = ?3
              AND rs.study_event_definition_id = ?4
            ORDER BY rs.rule_set_id
            """, nativeQuery = true)
    List<RuleSetEntity> findByCrfVersionIdOrCrfIdAndStudyIdAndStudyEventDefinitionId(
            Integer crfVersionId, Integer crfId, Integer studyId, Integer studyEventDefinitionId);

    @Query(value = """
            SELECT rs.*
            FROM module_rule_set rs
            JOIN module_rule_expression re ON re.rule_expression_id = rs.rule_expression_id
            WHERE re.value = ?1 AND rs.study_id = ?2
            """, nativeQuery = true)
    Optional<RuleSetEntity> findByExpressionValueAndStudyId(String expressionValue, Integer studyId);
}
