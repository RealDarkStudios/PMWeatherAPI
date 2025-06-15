package net.nullved.pmweatherapi.util;


import dev.protomanly.pmweather.util.ColorTables;

import java.awt.*;

/**
 * A collection of {@link ColorMap}s, mainly ones transferred from {@link ColorTables}
 * @since 0.14.15.6
 */
public class ColorMaps {
    /**
     * A {@link ColorMap} equivalent to {@link ColorTables#getReflectivity(float)}
     * @since 0.14.15.6
     */
    public static final ColorMap REFLECTIVITY = ColorMap.Builder.of(Color.BLACK)
        .addPoint(new Color(0x5C9DAE), 19.0F)
        .addPoint(new Color(0x0B6409), 27.0F)
        .addPoint(new Color(0xC5B300), 40.0F)
        .override(new Color(0xFA9400), 40.0F)
        .addPoint(new Color(0xB2590C), 50.0F)
        .override(new Color(0xF9230B), 50.0F)
        .addPoint(new Color(0x822820), 60.0F)
        .override(new Color(0xCA99B4), 60.0F)
        .addPoint(new Color(0xC21C72), 70.0F)
        .build(Color.WHITE, 70.0F);

    /**
     * A {@link ColorMap} equivalent to {@link ColorTables#getVelocity(float)} for values >= 0
     * @since 0.14.15.6
     */
    public static final ColorMap POSITIVE_VELOCITY = ColorMap.Builder.of(new Color(0x969696))
        .overrideModeGreater()
        .override(new Color(9074294), 0)
        .addPoint(new Color(8665153), 12.0F)
        .override(new Color(7208960), 12.0F)
        .addPoint(new Color(15925255), 39.0F)
        .override(new Color(16398161), 39.0F)
        .addPoint(new Color(16771235), 69.0F)
        .build(new Color(6751746), 140);

    /**
     * A {@link ColorMap} equivalent to {@link ColorTables#getVelocity(float)} for values <= 0
     * @since 0.14.15.6
     */
    public static final ColorMap NEGATIVE_VELOCITY = ColorMap.Builder.of(new Color(0x969696))
        .overrideModeGreater()
        .override(new Color(7505264), 0)
        .addPoint(new Color(5142860), 12.0F)
        .override(new Color(353795), 12.0F)
        .addPoint(new Color(3203299), 81.0F)
        .addPoint(new Color(1442457), 106.0F)
        .build(new Color(16711812), 140.0F);

    /**
     * A {@link ColorMap} equivalent to {@link ColorTables#getWindspeed(float)}
     * @since 0.14.15.6
     */
    public static final ColorMap WINDSPEED = ColorMap.Builder.of(Color.BLACK)
        .addPoint(Color.BLACK, 40.0F)
        .addPoint(new Color(106, 128, 241), 65.0F)
        .addPoint(new Color(117, 243, 224), 85.0F)
        .addPoint(new Color(116, 241, 81), 110.0F)
        .addPoint(new Color(246, 220, 53), 135.0F)
        .addPoint(new Color(246, 127, 53), 165.0F)
        .addPoint(new Color(246, 53, 53), 200.0F)
        .addPoint(new Color(240, 53, 246), 250.0F)
        .build(new Color(255, 255, 255), 300.0F);
}
