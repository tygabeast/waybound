package com.tygabeast.waybound.core;

/**
 * The logistic aspecting model. With s the aspected share (0 to 1):
 * <pre>ds/dt = k (s + a) (1 - s)   per minute of cycling in the focus, a = 0.05</pre>
 * It has a closed form, so the server never simulates it tick by tick:
 * <pre>ln((s + a) / (1 - s))  grows linearly at rate k (1 + a)</pre>
 */
public final class AspectingCurve {
    /** Offset that lets aspecting start from zero. */
    public static final double OFFSET = 0.05;
    /** Starting benchmark at Copper in normal aura, per minute. */
    public static final double BASE_RATE = 0.0164;
    /** Aura multipliers on the rate: rich aura up to 2x, thin aura 0.5x. */
    public static final double RICH_AURA = 2.0;
    public static final double THIN_AURA = 0.5;
    /** The final fraction of a percent snaps to fully aspected. */
    public static final double SNAP = 0.995;

    private AspectingCurve() {
    }

    /** Aspected share after {@code minutes} of cycling, starting from {@code s0}. */
    public static double advance(double s0, double minutes, double rate) {
        if (minutes <= 0.0 || rate <= 0.0) {
            return clamp(s0);
        }
        double s = clamp(s0);
        if (s >= SNAP) {
            return 1.0;
        }
        double u = Math.log((s + OFFSET) / (1.0 - s)) + rate * (1.0 + OFFSET) * minutes;
        double e = Math.exp(u);
        double next = (e - OFFSET) / (1.0 + e);
        return next >= SNAP ? 1.0 : clamp(next);
    }

    /** Minutes of cycling needed to go from {@code s0} to {@code s1}. */
    public static double minutesToReach(double s0, double s1, double rate) {
        if (rate <= 0.0) {
            return Double.POSITIVE_INFINITY;
        }
        double a = Math.log((clamp(s0) + OFFSET) / (1.0 - clamp(s0)));
        double b = Math.log((Math.min(s1, SNAP) + OFFSET) / (1.0 - Math.min(s1, SNAP)));
        return Math.max(0.0, (b - a) / (rate * (1.0 + OFFSET)));
    }

    /** Effective rate: base rate times the aura multiplier times the rank drift multiplier. */
    public static double rate(double auraMultiplier, Rank rank) {
        return BASE_RATE * auraMultiplier * rank.driftMultiplier();
    }

    private static double clamp(double s) {
        return Math.max(0.0, Math.min(1.0, s));
    }
}
