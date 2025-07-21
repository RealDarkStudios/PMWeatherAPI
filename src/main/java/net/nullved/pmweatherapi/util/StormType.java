package net.nullved.pmweatherapi.util;

import dev.protomanly.pmweather.weather.Storm;

/**
 * An enum holding different storm types
 * @since 0.15.0.0
 */
public enum StormType {
    SUPERCELL(0),
    TORNADO(0, 3),
    SQUALL(1),
    CYCLONE(2);

    public final int idx, stage;

    StormType(int idx, int stage) {
        this.idx = idx;
        this.stage = stage;
    }

    StormType(int idx) {
        this.idx = idx;
        this.stage = -1;
    }

    /**
     * Gets the index of the {@link StormType}
     * @return The {@link StormType} index
     * @since 0.15.0.0
     */
    public int idx() {
        return idx;
    }

    /**
     * Gets the minimum stage of the {@link StormType}
     * @return The minimum stage of the {@link StormType}
     * @since 0.15.0.0
     */
    public int stage() {
        return stage;
    }

    /**
     * Determines if the {@link Storm} meets this {@link StormType}'s specification.
     * If this {@link StormType} defines a `stage` (such as {@link StormType#TORNADO}), the {@link Storm} must be equal to or above that stage
     * @param storm The {@link Storm} to check
     * @return {@code true} if this {@link Storm} meets the {@link StormType} specification
     * @since 0.15.0.0
     */
    public boolean matches(Storm storm) {
        return storm.stormType == idx && storm.stage >= stage;
    }
}
