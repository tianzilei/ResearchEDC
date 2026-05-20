package org.researchedc.module.randomization.algorithms;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.researchedc.module.randomization.entity.RandomizationArm;
import org.researchedc.module.randomization.entity.RandomizationScheme;
import org.researchedc.module.randomization.enums.RandomizationAlgorithm;
import org.researchedc.module.randomization.service.RandomizationAlgorithmStrategy;
import org.springframework.stereotype.Component;

/**
 * Simple/coin-toss randomization algorithm.
 * Each subject is assigned to a random arm with equal probability (weighted by ratio).
 * Based on RandIMI's CoinTossRandomization.
 */
@Component
public class SimpleRandomization implements RandomizationAlgorithmStrategy {

    private final Random random = new SecureRandom();

    @Override
    public RandomizationAlgorithm getAlgorithm() {
        return RandomizationAlgorithm.SIMPLE;
    }

    @Override
    public RandomizationArm assign(
            RandomizationScheme scheme,
            List<RandomizationArm> arms,
            String stratumPath,
            Map<Long, Long> currentCounts) {

        int totalRatio = arms.stream().mapToInt(RandomizationArm::getRatio).sum();
        int roll = random.nextInt(totalRatio);
        int cumulative = 0;

        for (RandomizationArm arm : arms) {
            cumulative += arm.getRatio();
            if (roll < cumulative) {
                return arm;
            }
        }

        return arms.get(arms.size() - 1);
    }
}
