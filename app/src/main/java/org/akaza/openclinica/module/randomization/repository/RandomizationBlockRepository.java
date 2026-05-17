package org.akaza.openclinica.module.randomization.repository;

import java.util.List;
import java.util.Optional;
import org.akaza.openclinica.module.randomization.entity.RandomizationBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomizationBlockRepository extends JpaRepository<RandomizationBlock, Long> {

    List<RandomizationBlock> findBySchemeIdOrderByBlockIndex(Long schemeId);

    Optional<RandomizationBlock> findTopBySchemeIdAndStratumPathOrderByBlockIndexDesc(Long schemeId, String stratumPath);

    long countBySchemeIdAndStratumPath(Long schemeId, String stratumPath);
}
