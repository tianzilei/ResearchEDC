package org.researchedc.module.rule.repository;

import org.researchedc.module.rule.entity.RuleSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleSetRepository extends JpaRepository<RuleSetEntity, Integer> {

    List<RuleSetEntity> findByStudyIdOrderByRuleSetId(Integer studyId);
}
