package net.nullved.pmweatherapi.client.event;

import dev.protomanly.pmweather.weather.Lightning;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event;

/**
 * This event only exists on the client!
 * Called when a lightning strike happens
 * @since 0.14.15.3
 */
@OnlyIn(Dist.CLIENT)
public class LightningEvent extends Event {
    private final Lightning lightning;

    public LightningEvent(Lightning lightning) {
        this.lightning = lightning;
    }

    public Lightning getLightning() {
        return lightning;
    }
}
