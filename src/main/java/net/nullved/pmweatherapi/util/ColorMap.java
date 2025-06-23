package net.nullved.pmweatherapi.util;

import dev.protomanly.pmweather.util.ColorTables;

import java.awt.Color;
import java.util.*;

/**
 * A builder-based representation of {@link ColorTables}.
 * Should hopefully be able to produce identical results.
 * <br><br>
 * You can create a new {@link ColorMap} builder with {@link ColorMap.Builder#of(Color)}.
 * Use {@link Builder#addPoint(Color, float)} to add a new point to lerp between.
 * Use {@link Builder#override(Color, float)} to add a new point to override the color at.
 * @since 0.14.15.6
 */
public class ColorMap {
    private final boolean overrideModeGreater;
    private final NavigableMap<Float, LerpSegment> segments;
    private final NavigableMap<Float, Color> overridePoints;
    private final Color base;

    private final float min;
    private final float max;
    private Color[] lookup;
    private float resolution;

    private ColorMap(Color base, boolean overrideModeGreater, List<LerpSegment> segments, NavigableMap<Float, Color> overridePoints, float resolution) {
        this.min = Math.round(segments.getFirst().start / resolution) * resolution;
        this.max = Math.round(segments.getLast().end / resolution) * resolution;

        this.base = base;
        this.overrideModeGreater = overrideModeGreater;
        this.segments = new TreeMap<>();
        for (LerpSegment seg : segments) {
            this.segments.put(seg.start, seg);
        }
        this.overridePoints = overridePoints;

        recomputeLookups(resolution);
    }

    /**
     * Gets the smallest value a color is defined for
     * @return The minimum value
     * @since 0.14.15.6
     */
    public float minValue() {
        return min;
    }

    /**
     * Gets the largest value a color is defined for
     * @return The maximum value
     * @since 0.14.15.6
     */
    public float maxValue() {
        return max;
    }

    /**
     * Recomputes the lookup table with the given resolution.
     * @param resolution The new resolution
     * @since 0.14.16.1
     */
    public void recomputeLookups(float resolution) {
        this.resolution = resolution;
        int size = (int) (((max - min) / resolution) + 1);
        this.lookup = new Color[size];

        for (int i = 0; i < size; i++) {
            float val = min + i * resolution;
            lookup[i] = getAccurate(val);
        }
    }

    /**
     * Retrieves the color value using the closest value from the lookup table.
     * If you need the accurate value, use {@link #getAccurate(float)} instead
     * @param val The value to get a color for
     * @return The approximate color for this value
     * @since 0.14.16.1
     * @see #getAccurate(float)
     */
    public Color get(float val) {
        float newVal = Math.round(val / resolution) * resolution;
        if (newVal <= min) return lookup[0];
        if (newVal >= max) return lookup[lookup.length - 1];
        int idx = (int) ((newVal - min) / resolution);
        return lookup[idx];
    }

    /**
     * Gets the {@link Color} for the specific value.
     * <br>
     * This method is <strong>SLOWER</strong> and oftentimes the same as {@link #get(float)}.
     * Use that instead if you only need the approximate value
     * @param val The value to get the {@link Color} of
     * @return The {@link Color} for the given value
     * @since 0.14.15.6
     * @see #get(float)
     */
    public Color getAccurate(float val) {
        Color currentColor = base;

        Map.Entry<Float, Color> override = overridePoints.floorEntry(val);
        if (override != null) {
            currentColor = override.getValue();
        }

        Map.Entry<Float, LerpSegment> entry = segments.floorEntry(val);
        if (entry != null) {
            LerpSegment seg = entry.getValue();
            if (overrideModeGreater ? val <= seg.end : val < seg.end) {
                float delta = (val - seg.start) / (seg.end - seg.start);
                delta = delta > 1 ? 1 : delta < 0 ? 0 : delta;
                return lerp(delta, seg.from, seg.to);
            }
        }

        return currentColor;
    }

    /**
     * Lerps between two colors
     * @param delta The t-value from 0 to 1
     * @param c1 The first {@link Color}
     * @param c2 The second {@link Color}
     * @return A {@link Color} lerped between c1 and c2
     * @since 0.14.15.6
     */
    public static Color lerp(float delta, Color c1, Color c2) {
        float r = c1.getRed() + delta * (c2.getRed() - c1.getRed());
        float g = c1.getGreen() + delta * (c2.getGreen() - c1.getGreen());
        float b = c1.getBlue() + delta * (c2.getBlue() - c1.getBlue());
        return new Color((int) r, (int) g, (int) b);
    }

    /**
     * Represents a Lerp Segment
     * @param start The starting value
     * @param from The starting {@link Color}
     * @param end The ending value
     * @param to The ending {@link Color}
     * @since 0.14.15.6
     */
    public record LerpSegment(float start, Color from, float end, Color to) {}

    /**
     * A Builder pattern for creating a {@link ColorMap}
     * @since 0.14.15.6
     */
    public static class Builder {
        private final List<LerpSegment> segments = new ArrayList<>();
        private final NavigableMap<Float, Color> overridePoints = new TreeMap<>();
        private final Color base;
        private boolean overrideModeGreater = false;
        private float lastThreshold = Float.NEGATIVE_INFINITY, resolution = 0.1F;
        private Color lastColor;

        private Builder(Color base) {
            this.base = base;
            this.lastColor = base;
        }

        /**
         * Creates a new {@link Builder} with the given {@link Color} as the base
         * @param base The base {@link Color}
         * @return The created {@link Builder}
         * @since 0.14.15.6
         */
        public static Builder of(Color base) {
            return new Builder(base);
        }

        /**
         * Sets the step size between each value in the lookup table.
         * A value too small may be storing the same color multiple times!
         * @param resolution The resolution of the lookup table. Default 0.1F
         * @return This {@link Builder}
         * @since 0.14.16.1
         */
        public Builder lookupResolution(float resolution) {
            this.resolution = resolution;
            return this;
        }

        /**
         * Use in cases where you want overrides to only apply when the value is greater, but not equal to the threshold.
         * Used in {@link ColorMaps#POSITIVE_VELOCITY} and {@link ColorMaps#NEGATIVE_VELOCITY}
         * @return This {@link Builder}
         * @since 0.14.15.6
         */
        public Builder overrideModeGreater() {
            this.overrideModeGreater = true;
            return this;
        }

        /**
         * Adds a point to lerp between.
         * @param color The color at the end of the last {@link LerpSegment} and the start of this {@link LerpSegment}
         * @param threshold The threshold value
         * @return This {@link Builder}
         * @since 0.14.15.6
         */
        public Builder addPoint(Color color, float threshold) {
            if (lastThreshold != Float.NEGATIVE_INFINITY) {
                segments.add(new LerpSegment(lastThreshold, lastColor, threshold, color));
            }
            lastThreshold = threshold;
            lastColor = color;
            return this;
        }

        /**
         * Adds an override point. The final {@link Color} will get overwritten once it reaches the threshold value specified
         * @param color The {@link Color} to override the final {@link Color} with
         * @param threshold The threshold value
         * @return This {@link Builder}
         * @since 0.14.15.6
         */
        public Builder override(Color color, float threshold) {
            overridePoints.put(threshold, color);
            lastThreshold = threshold;
            lastColor = color;
            return this;
        }

        /**
         * Builds this {@link Builder} into a proper {@link ColorMap}
         * @param finalColor The max {@link Color}
         * @param finalThreshold The max threshold value
         * @return A completed {@link ColorMap}
         * @since 0.14.15.6
         */
        public ColorMap build(Color finalColor, float finalThreshold) {
            if (!segments.isEmpty()) {
                LerpSegment first = segments.get(0);
                if (first.start > 0.0F) {
                    segments.add(0, new LerpSegment(0.0F, base, first.start, first.from));
                }
            }

            if (overrideModeGreater) overridePoints.put(finalThreshold + 0.0001F, finalColor);
            else overridePoints.put(finalThreshold, finalColor);

            segments.add(new LerpSegment(lastThreshold, lastColor, finalThreshold, finalColor));
            return new ColorMap(base, overrideModeGreater, segments, overridePoints, resolution);
        }
    }
}