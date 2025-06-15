package net.nullved.pmweatherapi.util;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * An immutable wrapper around {@link String}s that can be used with {@link StringProperty}.
 * @param value The {@link String} to store.
 * @since 0.14.15.6
 */
public record StringValue(String value) implements StringRepresentable, Comparable<StringValue> {
    /**
     * Gets the serialized value
     * @return The value
     * @since 0.14.15.6
     */
    @Override
    public String getSerializedName() {
        return value;
    }

    /**
     * Compares this {@link StringValue}'s value to another {@link StringValue}'s value
     * @param o The other {@link StringValue}
     * @return 0 if the strings are equal, 1 if this string is greater, -1 if less than the other
     */
    @Override
    public int compareTo(@NotNull StringValue o) {
        return value.compareTo(o.value);
    }
}
