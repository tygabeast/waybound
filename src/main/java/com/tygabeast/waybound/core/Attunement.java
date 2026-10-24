package com.tygabeast.waybound.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An immutable attunement vector: a percentage per aspect id, always summing to 100.
 * Zero-sum: raising one aspect lowers the others proportionally. Nothing can be moved back into Pure.
 * Pure (the {@link #PURE} id) can only shrink.
 */
public final class Attunement {
    /** Id of the Pure aspect. */
    public static final String PURE = "unattuned";
    public static final double TOTAL = 100.0;
    private static final double EPSILON = 1.0e-6;

    private final Map<String, Double> shares;

    private Attunement(Map<String, Double> shares) {
        this.shares = Collections.unmodifiableMap(shares);
    }

    /** A fresh core: 100% Pure. */
    public static Attunement pure() {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put(PURE, TOTAL);
        return new Attunement(m);
    }

    /** Build from a map; rejects negative shares and totals that are not 100. */
    public static Attunement of(Map<String, Double> source) {
        Map<String, Double> m = new LinkedHashMap<>();
        double sum = 0.0;
        for (Map.Entry<String, Double> e : source.entrySet()) {
            double v = e.getValue();
            if (v < 0.0 || Double.isNaN(v)) {
                throw new IllegalArgumentException("Negative or invalid share for " + e.getKey());
            }
            if (v > EPSILON) {
                m.put(e.getKey(), v);
                sum += v;
            }
        }
        if (Math.abs(sum - TOTAL) > 1.0e-3) {
            throw new IllegalArgumentException("Attunement must sum to 100, got " + sum);
        }
        return new Attunement(m);
    }

    public double percent(String aspect) {
        return shares.getOrDefault(aspect, 0.0);
    }

    public Set<String> aspects() {
        return shares.keySet();
    }

    public Map<String, Double> asMap() {
        return shares;
    }

    /**
     * Raise {@code target} by {@code delta} percentage points, taking the points proportionally from every
     * other aspect (Pure included). The target cannot be Pure. The result is clamped so that the target never
     * exceeds 100. A delta of zero or less returns this vector unchanged.
     */
    public Attunement shiftToward(String target, double delta) {
        if (PURE.equals(target)) {
            throw new IllegalArgumentException("Nothing can be moved back into Pure");
        }
        if (delta <= 0.0) {
            return this;
        }
        double current = percent(target);
        double others = TOTAL - current;
        double amount = Math.min(delta, others);
        if (amount <= 0.0) {
            return this;
        }
        double keep = (others - amount) / others;
        Map<String, Double> m = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : shares.entrySet()) {
            if (!e.getKey().equals(target)) {
                double v = e.getValue() * keep;
                if (v > EPSILON) {
                    m.put(e.getKey(), v);
                }
            }
        }
        m.put(target, current + amount);
        return normalized(m);
    }

    /** Remove tiny rounding drift so the vector sums to exactly 100. */
    private static Attunement normalized(Map<String, Double> m) {
        double sum = 0.0;
        for (double v : m.values()) {
            sum += v;
        }
        if (sum <= 0.0) {
            return pure();
        }
        double scale = TOTAL / sum;
        Map<String, Double> out = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : m.entrySet()) {
            out.put(e.getKey(), e.getValue() * scale);
        }
        return new Attunement(out);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Attunement other && shares.equals(other.shares);
    }

    @Override
    public int hashCode() {
        return shares.hashCode();
    }

    @Override
    public String toString() {
        return "Attunement" + shares;
    }
}
