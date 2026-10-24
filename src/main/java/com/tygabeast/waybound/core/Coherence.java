package com.tygabeast.waybound.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Path coherence, exactly as written in the design document.
 *
 * <pre>
 * F = sum(p_i^2)
 * S = sum_{i&lt;j} 2 p_i p_j a_ij / (1 - F)
 * C = F + (1 - F) (S + 1) / 2
 * </pre>
 *
 * p_i is the share of aspect i (summing to 1) and a_ij the affinity between aspects i and j.
 * A single-aspect Path has F = 1 and is defined as C = 1. Pure counts as a neutral aspect with
 * affinity 0 toward everything, which the affinity function supplies.
 */
public final class Coherence {
    private Coherence() {
    }

    /** Affinity between two aspect ids, from -1 (opposed) to +1 (harmonious). */
    @FunctionalInterface
    public interface Affinity {
        double between(String a, String b);
    }

    public static double compute(Attunement attunement, Affinity affinity) {
        List<String> ids = new ArrayList<>(attunement.aspects());
        int n = ids.size();
        double[] p = new double[n];
        double f = 0.0;
        for (int i = 0; i < n; i++) {
            p[i] = attunement.percent(ids.get(i)) / Attunement.TOTAL;
            f += p[i] * p[i];
        }
        double rest = 1.0 - f;
        if (rest <= 1.0e-9) {
            return 1.0;
        }
        double cross = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                cross += 2.0 * p[i] * p[j] * affinity.between(ids.get(i), ids.get(j));
            }
        }
        double s = cross / rest;
        return f + rest * (s + 1.0) / 2.0;
    }
}
