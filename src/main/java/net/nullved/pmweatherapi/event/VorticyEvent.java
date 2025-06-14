package net.nullved.pmweatherapi.event;

import dev.protomanly.pmweather.weather.Vorticy;
import net.neoforged.bus.api.Event;

/**
 * Base Vorticy Event
 * @see New
 * @see Dead
 * @since 0.14.15.4
 */
public abstract class VorticyEvent extends Event {
    private final Vorticy vorticy;

    public VorticyEvent(Vorticy vorticy) {
        this.vorticy = vorticy;
    }

    public Vorticy getVorticy() {
        return vorticy;
    }

    /**
     * Called when a new {@link Vorticy} is created
     * @since 0.14.15.4
     */
    public static class New extends VorticyEvent {
        public New(Vorticy vorticy) {
            super(vorticy);
        }
    }

    /**
     * Called when a {@link Vorticy} dies
     * @since 0.14.15.4
     */
    public static class Dead extends VorticyEvent {
        public Dead(Vorticy vorticy) {
            super(vorticy);
        }
    }
}
