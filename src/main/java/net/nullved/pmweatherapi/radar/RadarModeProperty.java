package net.nullved.pmweatherapi.radar;

import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.Optional;

/**
 * A {@link Property} for a {@link RadarMode}
 * @since 0.14.16.2
 */
public class RadarModeProperty extends Property<RadarMode> {
    /**
     * Creates a new radar mode property with the given name
     *
     * @param name The name of the property
     * @since 0.14.16.2
     */
    public RadarModeProperty(String name) {
        super(name, RadarMode.class);
    }

    /**
     * Gets the possible radar mode values
     * @return {@link RadarMode#values()}
     * @since 0.14.16.2
     */
    @Override
    public Collection<RadarMode> getPossibleValues() {
        return RadarMode.values();
    }

    /**
     * Gets the serialized name of the given {@link RadarMode}
     * @param radarMode The {@link RadarMode}
     * @return The serialized name
     * @since 0.14.16.2
     */
    @Override
    public String getName(RadarMode radarMode) {
        return radarMode.getSerializedName();
    }

    /**
     * Gets a radar mode from the property string
     * @param s The radar mode string
     * @return A {@link RadarMode} ({@link RadarMode#NULL} if not a valid ID)
     * @since 0.14.16.2
     */
    @Override
    public Optional<RadarMode> getValue(String s) {
        return Optional.of(RadarMode.get(s));
    }
}
