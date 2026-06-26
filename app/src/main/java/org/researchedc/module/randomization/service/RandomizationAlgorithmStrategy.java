package org.researchedc.module.randomization.service;

import java.util.List;
import java.util.Map;
import org.researchedc.module.randomization.entity.RandomizationArm;
import org.researchedc.module.randomization.entity.RandomizationScheme;
import org.researchedc.module.randomization.enums.RandomizationAlgorithm;

/**
 * Strategy interface for randomization algorithms.
 * Inspired by RandIMI's {@code Randomization} interface.
 */
public interface RandomizationAlgorithmStrategy {

    RandomizationAlgorithm getAlgorithm();

    /**
     * Assign a subject to a study arm using the algorithm's logic.
     *
     * @param scheme       the randomization scheme
     * @param arms         the available study arms
     * @param stratumPath  the stratum path (empty for non-stratified)
     * @param currentCounts current assignment counts per arm (armId -> count)
     * @return the selected arm
     */
    RandomizationArm assign(
            RandomizationScheme scheme,
            List<RandomizationArm> arms,
            String stratumPath,
            Map<Long, Long> currentCounts
    );
}
