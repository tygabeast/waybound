package com.tygabeast.waybound.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CoreMathTest {
    private static final Coherence.Affinity HARMONIOUS = (a, b) -> a.equals(Attunement.PURE) || b.equals(Attunement.PURE) ? 0.0 : 1.0;
    private static final Coherence.Affinity OPPOSED = (a, b) -> a.equals(Attunement.PURE) || b.equals(Attunement.PURE) ? 0.0 : -1.0;
    private static final Coherence.Affinity NEUTRAL = (a, b) -> 0.0;

    private static Attunement of(Object... pairs) {
        Map<String, Double> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            m.put((String) pairs[i], ((Number) pairs[i + 1]).doubleValue());
        }
        return Attunement.of(m);
    }

    private static double sum(Attunement a) {
        return a.asMap().values().stream().mapToDouble(Double::doubleValue).sum();
    }

    // --- Coherence: the five sanity checks from the design document ---

    @Test
    void singleAspectGivesOne() {
        assertEquals(1.0, Coherence.compute(of("fire", 100), OPPOSED), 1e-9);
        assertEquals(1.0, Coherence.compute(Attunement.pure(), NEUTRAL), 1e-9);
    }

    @Test
    void evenSplitOfTwoHarmoniousGivesOne() {
        assertEquals(1.0, Coherence.compute(of("fire", 50, "destruction", 50), HARMONIOUS), 1e-9);
    }

    @Test
    void evenSplitOfTwoOpposedGivesHalf() {
        assertEquals(0.5, Coherence.compute(of("fire", 50, "water", 50), OPPOSED), 1e-9);
    }

    @Test
    void threeOpposedGiveOneThird() {
        double c = Coherence.compute(of("fire", 100.0 / 3, "water", 100.0 / 3, "wind", 100.0 / 3), OPPOSED);
        assertEquals(1.0 / 3.0, c, 1e-6);
    }

    @Test
    void mostlyPureGivesPointEightFour() {
        assertEquals(0.84, Coherence.compute(of(Attunement.PURE, 80, "fire", 20), NEUTRAL), 1e-9);
    }

    // --- Attunement ---

    @Test
    void shiftIsZeroSumAndTakesProportionally() {
        Attunement a = of(Attunement.PURE, 60, "fire", 30, "water", 10);
        Attunement b = a.shiftToward("wind", 20);
        assertEquals(100.0, sum(b), 1e-9);
        assertEquals(20.0, b.percent("wind"), 1e-9);
        // The other three shrink by the same factor (80 / 100).
        assertEquals(48.0, b.percent(Attunement.PURE), 1e-9);
        assertEquals(24.0, b.percent("fire"), 1e-9);
        assertEquals(8.0, b.percent("water"), 1e-9);
    }

    @Test
    void pureOnlyEverShrinks() {
        Attunement a = Attunement.pure();
        for (int i = 0; i < 50; i++) {
            Attunement next = a.shiftToward(i % 2 == 0 ? "fire" : "water", 3.0);
            assertTrue(next.percent(Attunement.PURE) <= a.percent(Attunement.PURE) + 1e-9);
            assertEquals(100.0, sum(next), 1e-9);
            a = next;
        }
        assertThrows(IllegalArgumentException.class, () -> Attunement.pure().shiftToward(Attunement.PURE, 5));
    }

    @Test
    void shiftIsClampedAtOneHundred() {
        Attunement a = of(Attunement.PURE, 10, "fire", 90).shiftToward("fire", 50);
        assertEquals(100.0, a.percent("fire"), 1e-9);
        assertEquals(100.0, sum(a), 1e-9);
    }

    @Test
    void rejectsBadVectors() {
        assertThrows(IllegalArgumentException.class, () -> of("fire", 50));
        assertThrows(IllegalArgumentException.class, () -> of("fire", 120, "water", -20));
    }

    // --- Aspecting curve (design: 10% ~70 min, 50% ~3 h, 75% ~4 h, 90% ~5 h at Copper in normal aura) ---

    @Test
    void curveMatchesTheDesignTimes() {
        double k = AspectingCurve.rate(1.0, Rank.COPPER);
        assertEquals(70.0, AspectingCurve.minutesToReach(0, 0.10, k), 3.0);
        assertEquals(180.0, AspectingCurve.minutesToReach(0, 0.50, k), 3.0);
        assertEquals(240.0, AspectingCurve.minutesToReach(0, 0.75, k), 3.0);
        assertEquals(300.0, AspectingCurve.minutesToReach(0, 0.90, k), 6.0);
    }

    @Test
    void advanceAgreesWithMinutesToReach() {
        double k = AspectingCurve.rate(1.0, Rank.COPPER);
        double minutes = AspectingCurve.minutesToReach(0, 0.50, k);
        assertEquals(0.50, AspectingCurve.advance(0, minutes, k), 1e-6);
    }

    @Test
    void advanceIsSplittable() {
        double k = AspectingCurve.rate(1.0, Rank.COPPER);
        double whole = AspectingCurve.advance(0, 200, k);
        double split = AspectingCurve.advance(AspectingCurve.advance(0, 80, k), 120, k);
        assertEquals(whole, split, 1e-9);
    }

    @Test
    void richAuraHalvesTheTimeAndHighRanksSlowDown() {
        double normal = AspectingCurve.minutesToReach(0, 0.5, AspectingCurve.rate(1.0, Rank.COPPER));
        double rich = AspectingCurve.minutesToReach(0, 0.5, AspectingCurve.rate(2.0, Rank.COPPER));
        assertEquals(normal / 2.0, rich, 1e-6);
        assertEquals(normal * 4.0, AspectingCurve.minutesToReach(0, 0.5, AspectingCurve.rate(1.0, Rank.JADE)), 1e-6);
        assertEquals(Double.POSITIVE_INFINITY, AspectingCurve.minutesToReach(0, 0.5, AspectingCurve.rate(1.0, Rank.LOWGOLD)));
    }

    @Test
    void finalFractionSnapsToFull() {
        assertEquals(1.0, AspectingCurve.advance(0.99, 1000, AspectingCurve.BASE_RATE), 0.0);
    }

    // --- Rank ladder ---

    @Test
    void ladderValuesMatchTheDesign() {
        int[] power = {1, 3, 9, 27, 81, 160, 320, 1000, 2000, 4000, 20000, 60000};
        for (Rank r : Rank.values()) {
            assertEquals(power[r.tier()], r.power());
        }
        assertEquals(15_000L, Rank.TRUEGOLD.scaleValue());
        assertEquals(75_000L, Rank.UNDERLORD.scaleValue());
        assertEquals(46_875_000L, Rank.MONARCH.scaleValue());
        assertEquals(300.0, Rank.COPPER.madraCapacity(), 1e-9);
        assertTrue(Rank.JADE.atLeast(Rank.IRON));
        assertEquals(Rank.IRON, Rank.byTier(2));
        assertThrows(IllegalArgumentException.class, () -> Rank.byTier(12));
    }
}
