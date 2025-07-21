package net.nullved.pmweatherapi.storm;

import dev.protomanly.pmweather.PMWeather;
import dev.protomanly.pmweather.config.ServerConfig;
import dev.protomanly.pmweather.event.GameBusEvents;
import dev.protomanly.pmweather.weather.Storm;
import dev.protomanly.pmweather.weather.WeatherHandler;
import dev.protomanly.pmweather.weather.WeatherHandlerServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.nullved.pmweatherapi.util.StormType;

/**
 * A builder for {@link Storm}s that makes it easy to create and spawn them.
 * Create a StormBuilder with any constructor or {@link #atPlayer(StormType, Player)}
 * <br>
 * To spawn the storm in the world, use {@link #buildAndSpawn()}. To only create the storm instance, use {@link #build()}
 * @since 0.14.15.5
 */
public class StormBuilder {
    private final WeatherHandler weatherHandler;
    private final StormType type;
    private final Vec3 position;
    private float risk = 0.5f, rankineFactor = 4.5F, width = 15.0F, smoothWidth = 15.0F, cycloneWindspeed = 0.0F, smoothWindspeed = 0.0F;
    private int windspeed = 0, maxWindspeed = 0, stage = 0, maxStage = 2, energy = 0, coldEnergy = 0, maxColdEnergy = 300, maxWidth = 15;
    private Vec3 velocity = Vec3.ZERO;
    private boolean visualOnly = false, aimAtAnyPlayer = false;
    private Player aimAtPlayer;

    /**
     * Create a new {@link StormBuilder}
     * @param weatherHandler The {@link WeatherHandler} to use
     * @param type The {@link StormType} of storm
     * @param position The {@link Vec3} representing the position of the {@link Storm}
     * @since 0.14.15.5
     */
    public StormBuilder(WeatherHandler weatherHandler, StormType type, Vec3 position) {
        this.weatherHandler = weatherHandler;
        this.type = type;
        this.position = position;
    }

    /**
     * Create a new {@link StormBuilder}
     * @param level The {@link Level} to spawn in
     * @param type The {@link StormType} of storm
     * @param position The {@link Vec3} representing the position of the {@link Storm}
     * @since 0.14.15.5
     */
    public StormBuilder(Level level, StormType type, Vec3 position) {
        this(GameBusEvents.MANAGERS.get(level.dimension()), type, position);
    }

    /**
     * Create a new {@link StormBuilder}
     * @param dimension The {@link ResourceKey} of the dimension to spawn in
     * @param type The {@link StormType} of storm
     * @param position The {@link Vec3} representing the position of the {@link Storm}
     * @since 0.14.15.5
     */
    public StormBuilder(ResourceKey<Level> dimension, StormType type, Vec3 position) {
        this(GameBusEvents.MANAGERS.get(dimension), type, position);
    }

    /**
     * Creates a {@link StormBuilder} at a {@link Player}'s dimension and position
     * @param type The {@link StormType} of storm
     * @param player The {@link Player} to grab the dimension and position from
     * @return A new {@link StormBuilder}
     * @since 0.14.15.5
     */
    public static StormBuilder atPlayer(StormType type, Player player) {
        return new StormBuilder(player.level(), type, player.position());
    }

    /**
     * Aims the {@link Storm} at the given player.
     * Overrides {@link #velocity(Vec3)}.
     * Overriden by {@link #aimAtAnyPlayer()}
     * @param player The {@link Player} to aim the {@link Storm} at
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder aimAtPlayer(Player player) {
        this.aimAtPlayer = player;
        return this;
    }

    /**
     * Aims the {@link Storm} at a random player.
     * Overrides {@link #velocity(Vec3)} and {@link #aimAtPlayer(Player)}
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder aimAtAnyPlayer() {
        this.aimAtAnyPlayer = true;
        return this;
    }

    /**
     * Set the {@link Storm} to be visual only
     * @param visualOnly Whether the storm should be visual only
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder visualOnly(boolean visualOnly) {
        this.visualOnly = visualOnly;
        return this;
    }

    /**
     * Sets the risk of the {@link Storm}
     * @param risk The risk
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder risk(float risk) {
        this.risk = risk;
        return this;
    }

    /**
     * Sets the rankine factor of the {@link Storm}
     * @param rankineFactor The rankine factor
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder rankineFactor(float rankineFactor) {
        this.rankineFactor = rankineFactor;
        return this;
    }

    /**
     * Sets the width of the {@link Storm}
     * @param width The width
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder width(float width) {
        this.width = width;
        return this;
    }

    /**
     * Sets the smooth width of the {@link Storm}
     * @param smoothWidth The smooth width
     * @return The {@link StormBuilder} instance
     * @since 0.15.0.0
     */
    public StormBuilder smoothWidth(float smoothWidth) {
        this.smoothWidth = smoothWidth;
        return this;
    }

    /**
     * Sets the max width of the {@link Storm}
     * @param maxWidth The max width
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    /**
     * Sets the stage of the {@link Storm}
     * @param stage The stage
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder stage(int stage) {
        this.stage = stage;
        return this;
    }

    /**
     * Sets the max stage of the {@link Storm}
     * @param maxStage The max stage
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder maxStage(int maxStage) {
        this.maxStage = maxStage;
        return this;
    }

    /**
     * Sets the energy of the {@link Storm}
     * @param energy The energy
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder energy(int energy) {
        this.energy = energy;
        return this;
    }

    /**
     * Sets the cold energy of the {@link Storm}
     * @param coldEnergy The cold energy
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder coldEnergy(int coldEnergy) {
        this.coldEnergy = coldEnergy;
        return this;
    }

    /**
     * Sets the max cold energy of the {@link Storm}
     * @param maxColdEnergy The max cold energy
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder maxColdEnergy(int maxColdEnergy) {
        this.maxColdEnergy = maxColdEnergy;
        return this;
    }

    /**
     * Sets the windspeed of the {@link Storm}
     * @param windspeed The windspeed
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder windspeed(int windspeed) {
        this.windspeed = windspeed;
        return this;
    }

    /**
     * Sets the cyclone windspeed of the {@link Storm}
     * @param cycloneWindspeed The cyclone windspeed
     * @return The {@link StormBuilder} instance
     * @since 0.15.0.0
     */
    public StormBuilder cycloneWindspeed(float cycloneWindspeed) {
        this.cycloneWindspeed = cycloneWindspeed;
        return this;
    }

    /**
     * Sets the smooth windspeed of the {@link Storm}
     * @param smoothWindspeed The smooth windspeed
     * @return The {@link StormBuilder} instance
     * @since 0.15.0.0
     */
    public StormBuilder smoothWindspeed(float smoothWindspeed) {
        this.smoothWindspeed = smoothWindspeed;
        return this;
    }

    /**
     * Sets the max windspeed of the {@link Storm}
     * @param maxWindspeed The max windspeed
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder maxWindspeed(int maxWindspeed) {
        this.maxWindspeed = maxWindspeed;
        return this;
    }

    /**
     * Sets the velocity of the {@link Storm}.
     * Overriden by {@link #aimAtPlayer(Player)} and {@link #aimAtAnyPlayer()}
     * @param velocity The velocity of the storm
     * @return The {@link StormBuilder} instance
     * @since 0.14.15.5
     */
    public StormBuilder velocity(Vec3 velocity) {
        this.velocity = velocity;
        return this;
    }

    /**
     * Builds the final storm, but does NOT spawn it.
     * To spawn in, use {@link #buildAndSpawn()} instead
     * @return The built {@link Storm} instance
     * @since 0.14.15.5
     */
    public Storm build() {
        Storm storm = new Storm(weatherHandler, weatherHandler.getWorld(), risk, type.idx());
        storm.initFirstTime();
        storm.visualOnly = visualOnly;
        storm.velocity = velocity;
        if (aimAtPlayer != null) {
            if (storm.stormType != StormType.SQUALL.idx()) {
                Vec3 aimPos = aimAtPlayer.position().add(new Vec3((double)(PMWeather.RANDOM.nextFloat() - 0.5F) * ServerConfig.aimAtPlayerOffset, 0.0F, (double)(PMWeather.RANDOM.nextFloat() - 0.5F) * ServerConfig.aimAtPlayerOffset));
                if (storm.position.distanceTo(aimPos) >= ServerConfig.aimAtPlayerOffset) {
                    Vec3 toward = storm.position.subtract(new Vec3(aimPos.x, storm.position.y, aimPos.z)).multiply(1.0F, 0.0F, 1.0F).normalize();
                    double speed = PMWeather.RANDOM.nextDouble() * (double)5.0F + (double)1.0F;
                    storm.velocity = toward.multiply(-speed, 0.0F, -speed);
                }

                storm.aimedAtPlayer = true;
            }
        }
        if (aimAtAnyPlayer) {
            if (storm.stormType != StormType.SQUALL.idx()) {
                Player nearest = storm.level.getNearestPlayer(this.position.x, this.position.y, this.position.z, 4096.0F, false);
                if (nearest != null) {
                    Vec3 aimPos = aimAtPlayer.position().add(new Vec3((double) (PMWeather.RANDOM.nextFloat() - 0.5F) * ServerConfig.aimAtPlayerOffset, 0.0F, (double) (PMWeather.RANDOM.nextFloat() - 0.5F) * ServerConfig.aimAtPlayerOffset));
                    if (storm.position.distanceTo(aimPos) >= ServerConfig.aimAtPlayerOffset) {
                        Vec3 toward = storm.position.subtract(new Vec3(aimPos.x, storm.position.y, aimPos.z)).multiply(1.0F, 0.0F, 1.0F).normalize();
                        double speed = PMWeather.RANDOM.nextDouble() * (double) 5.0F + (double) 1.0F;
                        storm.velocity = toward.multiply(-speed, 0.0F, -speed);
                    }

                    storm.aimedAtPlayer = true;
                }

            }
        }
        storm.position = position;
        storm.stage = Math.max(stage, type.stage());
        storm.maxStage = maxStage;
        storm.energy = energy;
        storm.width = width;
        storm.smoothWidth = smoothWidth;
        storm.maxWidth = maxWidth;
        storm.rankineFactor = rankineFactor;
        storm.coldEnergy = coldEnergy;
        storm.maxColdEnergy = maxColdEnergy;
        storm.windspeed = windspeed;
        storm.cycloneWindspeed = cycloneWindspeed;
        storm.smoothWindspeed = smoothWindspeed;
        storm.maxWindspeed = maxWindspeed;
        return storm;
    }

    /**
     * Builds the final storm and spawns it.
     * To NOT spawn it, use {@link #build()} instead
     * @return The built {@link Storm} instance
     * @since 0.14.15.5
     */
    public Storm buildAndSpawn() {
        Storm storm = build();
        weatherHandler.addStorm(storm);
        if (weatherHandler instanceof WeatherHandlerServer whs) {
            whs.syncStormNew(storm);
        }
        return storm;
    }
}
