package org.researchedc.module.rule.repository;

import org.researchedc.module.rule.entity.RuleExpressionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleExpressionRepository extends JpaRepository<RuleExpressionEntity, Integer> {
}
