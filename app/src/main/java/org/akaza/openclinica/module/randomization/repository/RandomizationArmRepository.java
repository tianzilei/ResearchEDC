package org.akaza.openclinica.module.randomization.repository;

import java.util.List;
import org.akaza.openclinica.module.randomization.entity.RandomizationArm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomizationArmRepository extends JpaRepository<RandomizationArm, Long> {

    List<RandomizationArm> findBySchemeIdOrderByOrderNumber(Long schemeId);
}
