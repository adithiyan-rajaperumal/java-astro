package org.vedic.astro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LifeAnchorsSyntheticChartFactoryTest {

    @Test
    @DisplayName("Should generate exactly 90 diverse synthetic test cases covering all 12 Lagnas and modalities")
    void testGenerate90Cases() {
        List<LifeAnchorsSyntheticChartFactory.TestCase> cases = LifeAnchorsSyntheticChartFactory.generate90SyntheticCases();
        assertNotNull(cases, "Test cases list must not be null");
        assertEquals(90, cases.size(), "Must generate exactly 90 test cases");

        long uniqueLagnas = cases.stream().map(LifeAnchorsSyntheticChartFactory.TestCase::lagnaSign).distinct().count();
        assertEquals(12, uniqueLagnas, "Must cover all 12 Lagnas");

        // Verify individual test case integrity
        for (LifeAnchorsSyntheticChartFactory.TestCase tc : cases) {
            assertNotNull(tc.id(), "Case id must not be null");
            assertFalse(tc.id().isBlank(), "Case id must not be blank");
            assertNotNull(tc.description(), "Description must not be null");
            assertTrue(tc.lagnaSign() >= 1 && tc.lagnaSign() <= 12, "Lagna sign must be 1..12");
            assertTrue(tc.lagnaDegree() >= 0.0 && tc.lagnaDegree() <= 30.0, "Lagna degree must be in [0, 30]");
            assertNotNull(tc.planetMap(), "Planet map must not be null");
            assertTrue(tc.planetMap().size() >= 9, "Planet map must contain at least 9 planetary bodies");
            assertNotNull(tc.shadbalaRupas(), "Shadbala rupas map must not be null");
            assertNotNull(tc.expectedSynthesisRule(), "Expected synthesis rule must not be null");
        }

        // Verify breakdown of 4 categories:
        long modalityCases = cases.stream().filter(c -> c.id().startsWith("MOD_")).count();
        assertEquals(36, modalityCases, "Must contain exactly 36 modality permutation cases (12 Lagnas x 3)");

        long visheshaCases = cases.stream().filter(c -> c.id().startsWith("VISH_") || c.id().startsWith("ASAMVADA_")).count();
        assertEquals(24, visheshaCases, "Must contain exactly 24 Vishesha Sutra override cases");

        long dualLordCases = cases.stream().filter(c -> c.id().startsWith("DUAL_")).count();
        assertEquals(10, dualLordCases, "Must contain exactly 10 Dual Lord resolution cases");

        long kakshyaCases = cases.stream().filter(c -> c.id().startsWith("KAKSHYA_")).count();
        assertEquals(20, kakshyaCases, "Must contain exactly 20 Kakshya Vriddhi & Hrasa edge cases");
    }
}
