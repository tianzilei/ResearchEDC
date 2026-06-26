package org.researchedc.module.randomization.algorithms;

import java.security.SecureRandom;
import java.util.*;
import org.researchedc.module.randomization.entity.RandomizationArm;
import org.researchedc.module.randomization.entity.RandomizationBlock;
import org.researchedc.module.randomization.entity.RandomizationScheme;
import org.researchedc.module.randomization.enums.RandomizationAlgorithm;
import org.researchedc.module.randomization.repository.RandomizationBlockRepository;
import org.researchedc.module.randomization.service.RandomizationAlgorithmStrategy;
import org.springframework.stereotype.Component;

/**
 * Block randomization algorithm.
 * Subjects are assigned in blocks to ensure balanced allocation within each stratum.
 * Based on RandIMI's BlockedRandomization.
 */
@Component
public class BlockRandomization implements RandomizationAlgorithmStrategy {

    private final RandomizationBlockRepository blockRepository;
    private final Random random = new SecureRandom();

    public BlockRandomization(RandomizationBlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    @Override
    public RandomizationAlgorithm getAlgorithm() {
        return RandomizationAlgorithm.BLOCK;
    }

    @Override
    public RandomizationArm assign(
            RandomizationScheme scheme,
            List<RandomizationArm> arms,
            String stratumPath,
            Map<Long, Long> currentCounts) {

        if (arms.isEmpty()) {
            throw new IllegalStateException("No arms configured for scheme " + scheme.getId());
        }

        int totalRatio = arms.stream().mapToInt(RandomizationArm::getRatio).sum();
        if (totalRatio <= 0) {
            throw new IllegalStateException("Total arm ratio must be positive for scheme " + scheme.getId());
        }
        int blockSize = scheme.getMinBlockSize() != null ? scheme.getMinBlockSize() : totalRatio;

        // Find current block for this stratum path
        RandomizationBlock currentBlock = findOrCreateBlock(scheme, arms, stratumPath, blockSize);

        // Calculate remaining assignments in current block per arm
        Map<Long, Long> remainingInBlock = calculateRemaining(currentBlock, arms, currentCounts);

        // Filter arms with remaining slots
        List<RandomizationArm> availableArms = arms.stream()
                .filter(a -> remainingInBlock.getOrDefault(a.getId(), 0L) > 0)
                .toList();

        if (availableArms.isEmpty()) {
            // Current block is full, create a new one
            currentBlock = createNewBlock(scheme, stratumPath, blockSize);
            // Reset remaining for all arms
            return assign(scheme, arms, stratumPath, currentCounts);
        }

        // Pick randomly from available arms
        return availableArms.get(random.nextInt(availableArms.size()));
    }

    private RandomizationBlock findOrCreateBlock(
            RandomizationScheme scheme, List<RandomizationArm> arms,
            String stratumPath, int blockSize) {

        Optional<RandomizationBlock> existing = blockRepository
                .findTopBySchemeIdAndStratumPathOrderByBlockIndexDesc(scheme.getId(), stratumPath);

        if (existing.isPresent()) {
            return existing.get();
        }
        return createNewBlock(scheme, stratumPath, blockSize);
    }

    private RandomizationBlock createNewBlock(RandomizationScheme scheme, String stratumPath, int blockSize) {
        long blockCount = blockRepository.countBySchemeIdAndStratumPath(scheme.getId(), stratumPath);
        RandomizationBlock block = new RandomizationBlock();
        block.setScheme(scheme);
        block.setBlockSize(blockSize);
        block.setBlockIndex((int) blockCount);
        block.setStratumPath(stratumPath);
        return blockRepository.save(block);
    }

    private Map<Long, Long> calculateRemaining(
            RandomizationBlock block, List<RandomizationArm> arms,
            Map<Long, Long> currentCounts) {

        Map<Long, Long> remaining = new HashMap<>();
        int totalRatio = arms.stream().mapToInt(RandomizationArm::getRatio).sum();
        int assignmentsPerArm = block.getBlockSize() / totalRatio;

        for (RandomizationArm arm : arms) {
            long assignedInBlock = currentCounts.getOrDefault(arm.getId(), 0L)
                    % (long) (arms.size() * assignmentsPerArm);
            remaining.put(arm.getId(), (long) (assignmentsPerArm * arm.getRatio()) - assignedInBlock);
        }

        return remaining;
    }
}
