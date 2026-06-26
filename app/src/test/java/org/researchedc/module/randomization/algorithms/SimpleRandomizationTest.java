package org.researchedc.module.randomization.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.researchedc.module.randomization.entity.RandomizationArm;
import org.researchedc.module.randomization.entity.RandomizationScheme;
import org.researchedc.module.randomization.enums.RandomizationAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleRandomizationTest {

    private SimpleRandomization algorithm;
    private RandomizationScheme scheme;

    @BeforeEach
    void setUp() {
        algorithm = new SimpleRandomization();

        scheme = new RandomizationScheme();
        scheme.setId(1L);
        scheme.setName("Test Scheme");
        scheme.setAlgorithm(RandomizationAlgorithm.SIMPLE);
    }

    @Test
    void getAlgorithm_returnsSIMPLE() {
        assertEquals(RandomizationAlgorithm.SIMPLE, algorithm.getAlgorithm());
    }

    @Test
    void assign_withEqualRatio_returnsOneOfArms() {
        RandomizationArm armA = createArm(1L, "Control", 1, 1);
        RandomizationArm armB = createArm(2L, "Treatment", 1, 2);
        List<RandomizationArm> arms = List.of(armA, armB);

        RandomizationArm result = algorithm.assign(scheme, arms, "", Map.of());

        assertNotNull(result);
        assertTrue(result.getName().equals("Control") || result.getName().equals("Treatment"));
    }

    @Test
    void assign_withUnequalRatio_returnsBothArmsOverManyCalls() {
        RandomizationArm armA = createArm(1L, "Control", 1, 1);
        RandomizationArm armB = createArm(2L, "Treatment", 2, 2);
        List<RandomizationArm> arms = List.of(armA, armB);

        int controlCount = 0;
        int treatmentCount = 0;
        int trials = 1000;

        for (int i = 0; i < trials; i++) {
            Map<Long, Long> counts = new HashMap<>();
            counts.put(1L, (long) controlCount);
            counts.put(2L, (long) treatmentCount);

            RandomizationArm result = algorithm.assign(scheme, arms, "", counts);
            if (result.getName().equals("Control")) {
                controlCount++;
            } else {
                treatmentCount++;
            }
        }

        // With ratio 1:2, Treatment should be picked roughly twice as often
        assertTrue(treatmentCount > controlCount, "Treatment (ratio 2) should be picked more than Control (ratio 1)");
    }

    @Test
    void assign_withThreeArms_returnsOneOfThem() {
        RandomizationArm armA = createArm(1L, "Arm A", 1, 1);
        RandomizationArm armB = createArm(2L, "Arm B", 1, 2);
        RandomizationArm armC = createArm(3L, "Arm C", 1, 3);
        List<RandomizationArm> arms = List.of(armA, armB, armC);

        RandomizationArm result = algorithm.assign(scheme, arms, "", Map.of());

        assertNotNull(result);
        assertTrue(List.of("Arm A", "Arm B", "Arm C").contains(result.getName()));
    }

    @Test
    void assign_ignoresStratumPath() {
        RandomizationArm arm = createArm(1L, "Only Arm", 1, 1);
        List<RandomizationArm> arms = List.of(arm);

        RandomizationArm result = algorithm.assign(scheme, arms, "gender=M|age=30", Map.of());

        assertNotNull(result);
        assertEquals("Only Arm", result.getName());
    }

    @Test
    void assign_withoutArms_throwsException() {
        assertThrows(IllegalStateException.class, () -> algorithm.assign(scheme, List.of(), "", Map.of()));
    }

    @Test
    void assign_withoutPositiveRatio_throwsException() {
        RandomizationArm arm = createArm(1L, "Control", 0, 1);

        assertThrows(IllegalStateException.class, () -> algorithm.assign(scheme, List.of(arm), "", Map.of()));
    }

    private static RandomizationArm createArm(Long id, String name, int ratio, int order) {
        RandomizationArm arm = new RandomizationArm();
        arm.setId(id);
        arm.setName(name);
        arm.setRatio(ratio);
        arm.setOrderNumber(order);
        return arm;
    }
}
