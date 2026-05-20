package org.researchedc.module.randomization.repository;

import java.util.List;
import org.researchedc.module.randomization.entity.RandomizationArm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomizationArmRepository extends JpaRepository<RandomizationArm, Long> {

    List<RandomizationArm> findBySchemeIdOrderByOrderNumber(Long schemeId);
}
