package com.tygabeast.waybound.core;

/**
 * The twelve ranks, with the rank power ladder and the scale value ladder from the design document.
 * Pure Java: no Minecraft imports, so it can be unit tested without the game.
 */
public enum Rank {
    FOUNDATION(1, 1L, 1.0),
    COPPER(3, 5L, 1.0),
    IRON(9, 25L, 0.5),
    JADE(27, 125L, 0.25),
    LOWGOLD(81, 600L, 0.0),
    HIGHGOLD(160, 3_000L, 0.0),
    TRUEGOLD(320, 15_000L, 0.0),
    UNDERLORD(1_000, 75_000L, 0.0),
    OVERLORD(2_000, 375_000L, 0.0),
    ARCHLORD(4_000, 1_875_000L, 0.0),
    HERALD(20_000, 9_375_000L, 0.0),
    MONARCH(60_000, 46_875_000L, 0.0);

    /** Foundation madra capacity in units; higher ranks follow the power ladder. */
    public static final double FOUNDATION_CAPACITY = 100.0;

    private static final Rank[] VALUES = values();

    private final int power;
    private final long scaleValue;
    private final double driftMultiplier;

    Rank(int power, long scaleValue, double driftMultiplier) {
        this.power = power;
        this.scaleValue = scaleValue;
        this.driftMultiplier = driftMultiplier;
    }

    /** Technique power relative to Foundation. */
    public int power() {
        return power;
    }

    /** Madra capacity follows the power ladder, so costs can stay percentage based. */
    public double madraCapacity() {
        return FOUNDATION_CAPACITY * power;
    }

    /** Value of one scale of this tier, in scale units. */
    public long scaleValue() {
        return scaleValue;
    }

    /**
     * Attunement drift rate multiplier: 1 at Copper, 0.5 at Iron, 0.25 at Jade, and zero from Lowgold up
     * (the set of aspects is locked in). Foundation has no attunement at all, so it is 1 here only because
     * the core is 100% Pure and nothing can move.
     */
    public double driftMultiplier() {
        return driftMultiplier;
    }

    public int tier() {
        return ordinal();
    }

    public boolean atLeast(Rank other) {
        return ordinal() >= other.ordinal();
    }

    public static Rank byTier(int tier) {
        if (tier < 0 || tier >= VALUES.length) {
            throw new IllegalArgumentException("No rank with tier " + tier);
        }
        return VALUES[tier];
    }
}
