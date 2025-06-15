package net.nullved.pmweatherapi.client.render;

/**
 * Specific rendering data for a pixel on the radar
 * @param canRender {@code true} if either the server doesn't require WSR-88D or a WSR-88D is complete within 4 chunks of the radar
 * @param rdbz The relative reflectivity
 * @param velocity The velocity
 * @param temp The temperature
 * @param x The x-position of the pixel (from {@code -resolution} to {@code resolution})
 * @param z The z-position of the pixel (from {@code -resolution} to {@code resolution})
 * @param resolution The resolution of the radar
 * @param renderData The associated {@link RenderData}
 * @since 0.14.15.6
 */
public record PixelRenderData(boolean canRender, float rdbz, float velocity, float temp, int x, int z, int resolution, RenderData renderData) {
}
