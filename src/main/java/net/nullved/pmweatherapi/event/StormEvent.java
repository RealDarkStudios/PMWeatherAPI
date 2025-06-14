package net.nullved.pmweatherapi.event;

import dev.protomanly.pmweather.weather.Storm;
import net.neoforged.bus.api.Event;

/**
 * Base Storm Event
 * @see New
 * @see Dying
 * @see Dead
 * @see StageChanged
 * @since 0.14.15.4
 */
public abstract class StormEvent extends Event {
    private final Storm storm;

    public StormEvent(Storm storm) {
        this.storm = storm;
    }

    public Storm getStorm() {
        return storm;
    }

    public long getStormID() {
        return storm.ID;
    }

    /**
     * Called when a new {@link Storm} is created
     * @since 0.14.15.4
     */
    public static class New extends StormEvent {
        public New(Storm storm) {
            super(storm);
        }
    }

    /**
     * Called every tick a {@link Storm} is dying
     * @since 0.14.15.4
     */
    public static class Dying extends StormEvent {
        public Dying(Storm storm) {
            super(storm);
        }
    }

    /**
     * Called when a {@link Storm} is dead and removed
     * @since 0.14.15.4
     */
    public static class Dead extends StormEvent {
        public Dead(Storm storm) {
            super(storm);
        }
    }

    /**
     * Called when a {@link Storm}'s stage changes
     * @since 0.14.15.4
     */
    public static class StageChanged extends StormEvent {
        int newStage;

        public StageChanged(Storm storm, int newStage) {
            super(storm);
            this.newStage = newStage;
        }

        public int getNewStage() {
            return newStage;
        }
    }
}
