package org.researchedc.module.rule.repository;

import java.util.List;
import java.util.Optional;

import org.researchedc.module.rule.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Integer> {

    Optional<RuleEntity> findByOcOid(String ocOid);

    @Query(value = """
            SELECT r.*
            FROM module_rule r
            JOIN module_rule_set_rule rsr ON rsr.rule_id = r.rule_id
            WHERE rsr.rule_set_id = ?1
            ORDER BY r.rule_id
            """, nativeQuery = true)
    List<RuleEntity> findByRuleSetId(Integer ruleSetId);
}
