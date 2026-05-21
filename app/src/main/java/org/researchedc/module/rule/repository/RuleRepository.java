package org.researchedc.module.rule.repository;

import org.researchedc.module.rule.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Integer> {
}
