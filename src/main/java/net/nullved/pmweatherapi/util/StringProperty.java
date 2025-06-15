package net.nullved.pmweatherapi.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * A Property that can store {@link String}s on a {@link net.minecraft.world.level.block.state.BlockState} using a {@link StringValue} wrapper
 * @since 0.14.15.6
 */
public class StringProperty extends Property<StringValue> {
    private final ImmutableSet<StringValue> values;
    private final Map<String, StringValue> names = Maps.newHashMap();

    /**
     * Creates a new string property with the given name and values
     * @param name The name of the property
     * @param values A {@link Collection} of possible {@link StringValue}s
     * @since 0.14.15.6
     */
    public StringProperty(String name, Collection<StringValue> values) {
        super(name, StringValue.class);
        this.values = ImmutableSet.copyOf(values);

        for(StringValue t : values) {
            String s = t.getSerializedName();
            if (this.names.containsKey(s)) {
                throw new IllegalArgumentException("Multiple values have the same name '" + s + "'");
            }

            this.names.put(s, t);
        }

    }

    /**
     * Gets the possible values for this property
     * @return The {@link Collection} of possible {@link StringValue}s
     * @since 0.14.15.6
     */
    public Collection<StringValue> getPossibleValues() {
        return this.values;
    }

    /**
     * Gets the associated {@link StringValue} for the {@link String} value
     * @param value The value in string format
     * @return An {@link Optional} {@link StringValue}
     * @since 0.14.15.6
     */
    public Optional<StringValue> getValue(String value) {
        return Optional.ofNullable(this.names.get(value));
    }

    /**
     * Gets the name of the given {@link StringValue}.
     * In this case, returns {@link StringValue#getSerializedName()}
     * @param value The {@link StringValue}
     * @return The serialized name of the {@link StringValue}
     * @since 0.14.15.6
     */
    @Override
    public String getName(StringValue value) {
        return value.getSerializedName();
    }

    /**
     * Tests whether this {@link StringProperty} is equal to another {@link Object}
     * @param other The reference object with which to compare
     * @return {@code true} if the other {@link Object} is equal to this {@link StringProperty}, {@code false} otherwise
     * @since 0.14.15.6
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            if (other instanceof StringProperty otherSP) {
                if (super.equals(other)) {
                    return this.values.equals(otherSP.values) && this.names.equals(otherSP.names);
                }
            }

            return false;
        }
    }

    /**
     * Generates a hashcode for this {@link StringProperty}
     * @return The generated hashcode
     * @since 0.14.15.6
     */
    public int generateHashCode() {
        int i = super.generateHashCode();
        i = 31 * i + this.values.hashCode();
        return 31 * i + this.names.hashCode();
    }

    /**
     * Creates a new {@link StringProperty} from the given name and values
     * @param name The property name
     * @param values The accepted {@link StringValue}s
     * @return A new {@link StringProperty}
     * @since 0.14.15.6
     */
    public static StringProperty create(String name, StringValue... values) {
        return create(name, Lists.newArrayList(values));
    }

    /**
     * Creates a new {@link StringProperty} from the given name and values
     * @param name The property name
     * @param values A {@link Collection} of accepted {@link StringValue}s
     * @return A new {@link StringProperty}
     * @since 0.14.15.6
     */
    public static  StringProperty create(String name, Collection<StringValue> values) {
        return new StringProperty(name, values);
    }
}