package org.akaza.openclinica.module.randomization.algorithms;

import java.util.List;
import java.util.Map;
import org.akaza.openclinica.module.randomization.entity.RandomizationArm;
import org.akaza.openclinica.module.randomization.entity.RandomizationScheme;
import org.akaza.openclinica.module.randomization.enums.RandomizationAlgorithm;
import org.akaza.openclinica.module.randomization.repository.RandomizationBlockRepository;
import org.akaza.openclinica.module.randomization.service.RandomizationAlgorithmStrategy;
import org.springframework.stereotype.Component;

/**
 * Stratified block randomization.
 * Combines stratification with block randomization.
 * Each stratum combination has its own block sequence.
 * Based on RandIMI's stratified block design.
 */
@Component
public class StratifiedBlockRandomization implements RandomizationAlgorithmStrategy {

    private final BlockRandomization blockRandomization;

    public StratifiedBlockRandomization(RandomizationBlockRepository blockRepository) {
        this.blockRandomization = new BlockRandomization(blockRepository);
    }

    @Override
    public RandomizationAlgorithm getAlgorithm() {
        return RandomizationAlgorithm.STRATIFIED_BLOCK;
    }

    @Override
    public RandomizationArm assign(
            RandomizationScheme scheme,
            List<RandomizationArm> arms,
            String stratumPath,
            Map<Long, Long> currentCounts) {

        if (stratumPath == null || stratumPath.isEmpty()) {
            throw new IllegalStateException(
                    "Stratum path required for STRATIFIED_BLOCK algorithm");
        }

        // Filter counts for this specific stratum
        // For stratified block, each stratum has its own block sequence
        return blockRandomization.assign(scheme, arms, stratumPath, currentCounts);
    }
}
