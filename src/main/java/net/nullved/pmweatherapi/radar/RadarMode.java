package net.nullved.pmweatherapi.radar;

import dev.protomanly.pmweather.PMWeather;
import dev.protomanly.pmweather.block.entity.RadarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.nullved.pmweatherapi.client.render.PixelRenderData;
import net.nullved.pmweatherapi.client.render.RadarOverlays;
import net.nullved.pmweatherapi.data.PMWExtras;
import net.nullved.pmweatherapi.util.ColorMap;
import net.nullved.pmweatherapi.util.ColorMaps;
import net.nullved.pmweatherapi.util.StringValue;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Function;

/**
 * A class representing a Radar Mode.
 * <br><br>
 * To register your own radar mode, you must call {@link #create} somewhere from your MAIN MOD CLASS CONSTRUCTOR.
 * This is to ensure the radar mode gets added to {@link PMWExtras#RADAR_MODE} so that it will be recognized by Minecraft as a valid property value
 * <br><br>
 * For each radar mode, you must define a function taking in a {@link PixelRenderData} and returning a {@link Color}.
 * This function is run for every pixel on the radar, so try to make it performant.
 * <br><br>
 * You can also define a custom dot color with {@link #create(ResourceLocation, Function, Color)} (this supports transparency)
 * <br><br>
 * You may also be looking to overlay something on top of the radar instead.
 * If this is what you are looking to do, checkout {@link RadarOverlays} instead
 *
 * @since 0.14.15.6
 * @see RadarOverlays
 */
public class RadarMode implements StringRepresentable, Comparable<RadarMode> {
    private static final LinkedHashMap<ResourceLocation, RadarMode> MODES = new LinkedHashMap<>();
    private static boolean disableBaseRendering = false;

    /**
     * A "Null" Radar Mode mimicking Minecraft's missing texture.
     * This radar mode is not accessible by normal means, and if you see it that is an error.
     * @since 0.14.15.6
     */
    public static final RadarMode NULL = new RadarMode(ResourceLocation.parse("null"), prd -> {
        if ((prd.x() > 0 && prd.z() > 0) || (prd.x() <= 0 && prd.z() <= 0)) return new Color(1.0F, 0, 1.0F);
        else return Color.BLACK;
    }, new Color(0, 0, 0, 0));

    /**
     * A Radar Mode that is a copy of PMWeather's Reflectivity
     * @since 0.14.15.6
     */
    public static final RadarMode REFLECTIVITY = create(PMWeather.getPath("reflectivity"), prd -> {
        Holder<Biome> biome = ((RadarBlockEntity) prd.renderData().blockEntity()).getNearestBiome(new BlockPos((int) prd.worldPos().x, (int) prd.worldPos().y, (int) prd.worldPos().z));
        if (biome != null) return ColorMaps.REFLECTIVITY.getWithBiome(prd.rdbz(), biome, prd.worldPos());
        else return ColorMaps.REFLECTIVITY.get(prd.rdbz());
    });

    /**
     * A Radar Mode that is a copy of PMWeather's Velocity
     * @since 0.14.15.6
     */
    public static final RadarMode VELOCITY = create(PMWeather.getPath("velocity"), prd -> {
        Color velCol = prd.velocity() >= 0.0F ? ColorMaps.POSITIVE_VELOCITY.get(prd.velocity() / 1.75F) : ColorMaps.NEGATIVE_VELOCITY.get(-prd.velocity() / 1.75F);

        return ColorMap.lerp(Mth.clamp(Math.max(prd.rdbz(), (Mth.abs(prd.velocity() / 1.75F) - 18.0F) / 0.65F) / 12.0F, 0.0F, 1.0F), Color.BLACK, velCol);
    });

    /**
     * A Radar Mode that is a copy of PMWeather's IR
     * @since 0.15.0.0
     */
    public static final RadarMode IR = create(PMWeather.getPath("ir"), prd -> {
        float rdbz = prd.rdbz();
        float ir = rdbz * 10.0F;

        if (rdbz > 10.0F) {
            ir = 100.0F + (rdbz - 10.0F) * 2.5F;
        }

        if (rdbz > 50.0F) {
            ir += (rdbz - 50.0F) * 5.0F;
        }

        return ColorMaps.IR.get(ir);
    });

    private final ResourceLocation id;
    private final Function<PixelRenderData, Color> colorFunction;
    private final Color dotColor;
    private RadarMode(ResourceLocation id, Function<PixelRenderData, Color> colorFunction, Color dotColor) {
        this.id = id;
        this.colorFunction = colorFunction;
        this.dotColor = dotColor;
    }

    /**
     * Disables all rendering of pixels from any radar mode
     * @param disable Whether to disable rendering or not
     * @since 0.15.3.3
     */
    public static void disableBaseRendering(boolean disable) {
        disableBaseRendering = disable;
    }

    /**
     * Returns whether base rendering is disabled or not
     * @return Base rendering disable state
     * @since 0.15.3.3
     */
    public static boolean isBaseRenderingDisabled() {
        return disableBaseRendering;
    }

    /**
     * Create a new {@link RadarMode}
     * @param id The {@link ResourceLocation} of this radar mode
     * @param colorFunction The {@link Function} mapping a {@link PixelRenderData} to a {@link Color}. Runs for every pixel
     * @param renderDotColor The {@link Color} of the dot. Supports transparency
     * @return A new {@link RadarMode}
     * @since 0.14.15.6
     */
    public static RadarMode create(ResourceLocation id, Function<PixelRenderData, Color> colorFunction, Color renderDotColor) {
        return MODES.computeIfAbsent(id, nm -> new RadarMode(id, colorFunction, renderDotColor));
    }

    /**
     * Create a new {@link RadarMode} with a red dot at the center.
     * To set a custom dot color, use {@link #create(ResourceLocation, Function, Color)}
     * @param id The {@link ResourceLocation} of this radar mode
     * @param colorFunction The {@link Function} mapping a {@link PixelRenderData} to a {@link Color}. Runs for every pixel
     * @return A new {@link RadarMode}
     * @since 0.14.15.6
     */
    public static RadarMode create(ResourceLocation id, Function<PixelRenderData, Color> colorFunction) {
        return create(id, colorFunction, Color.RED);
    }

    /**
     * Returns a {@link Collection} of {@link RadarMode}s
     * @return All Radar Modes
     * @since 0.14.15.6
     */
    public static Collection<RadarMode> values() {
        return MODES.values();
    }

    /**
     * Gets a specific {@link RadarMode} based on ID
     * @param id The {@link ResourceLocation} of the {@link RadarMode}
     * @return The associated {@link RadarMode}, or {@link #NULL} if not found
     * @since 0.14.15.6
     */
    public static RadarMode get(ResourceLocation id) {
        return MODES.getOrDefault(id, NULL);
    }

    /**
     * Gets a specific {@link RadarMode} based on ID in string format
     * @param id The ID of the {@link RadarMode}
     * @return The associated {@link RadarMode}, or {@link #NULL} if not found
     * @since 0.14.15.6
     */
    public static RadarMode get(String id) {
        RadarMode radarMode = MODES.get(ResourceLocation.tryParse(id.replaceFirst("_", ":")));
        if (radarMode != null) return radarMode;
        for (RadarMode mode: MODES.values()) {
            if (id.equals(mode.id.toString().replace(":", "_"))) return mode;
        }
        return NULL;
    }

    /**
     * Gets the next {@link RadarMode} in the cycle
     * @return The next {@link RadarMode}
     * @since 0.14.15.6
     */
    public RadarMode cycle() {
        RadarMode[] values = MODES.values().toArray(RadarMode[]::new);
        int idx = Arrays.binarySearch(values, this);
        return values[(idx + 1) % values.length];
    }

    /**
     * Gets the {@link ResourceLocation} of the radar mode
     * @return The {@link ResourceLocation} of this radar mode
     */
    public ResourceLocation getId() {
        return id;
    }

    /**
     * Gets the color of the dot at the center of the radar
     * @return The dot {@link Color}
     * @since 0.14.15.6
     */
    public Color getDotColor() {
        return dotColor;
    }

    /**
     * Gets the color for a certain pixel. Applies the function from the {@link #create} method
     * @param pixelRenderData The {@link PixelRenderData} of the pixel
     * @return The {@link Color} to draw to the pixel
     * @since 0.14.15.6
     */
    public Color getColorForPixel(PixelRenderData pixelRenderData) {
        return colorFunction.apply(pixelRenderData);
    }

    /**
     * Gets this {@link RadarMode} expressed as a {@link StringValue}
     * @return A {@link StringValue}
     * @since 0.14.15.6
     */
    public StringValue stringValue() {
        return new StringValue(getSerializedName());
    }

    /**
     * Gets the serialized name of the {@link RadarMode}
     * @return The serialized name
     * @since 0.14.15.6
     */
    @Override
    public String getSerializedName() {
        return id.toString().replace(":", "_");
    }

    /**
     * Compares this {@link RadarMode} to the given {@link RadarMode}.
     * Used to efficiently search for the next {@link RadarMode} in the cycle
     * @param o The other {@link RadarMode}
     * @return 0 if same position in the cycle, 1 if later, -1 if earlier
     * @since 0.14.15.6
     */
    @Override
    public int compareTo(@NotNull RadarMode o) {
        ArrayList<ResourceLocation> modeKeys = new ArrayList<>(MODES.keySet());
        return Integer.compare(modeKeys.indexOf(id), modeKeys.indexOf(o.id));
    }
}
