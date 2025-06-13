![PMWeatherAPI Logo](https://cdn.modrinth.com/data/cached_images/f7b06beba44c5d08763305f5a0c3480611610230.png)

## Support
You can get support on my discord server [here](https://discord.gg/B6YKNVB6pW)!

## About
PMWeatherAPI is an **Unofficial** API designed to make interacting with [ProtoManly's Weather Mod](https://modrinth.com/mod/protomanlys-weather/) easier for those wishing to create addons for PMWeather.

Current Features include:
- Nearby Storm Detection
- Nearby Radar Detection
- Radar Overlays

This API is currently experimental (due to PMWeather being in alpha) and bugs are to be expected
The API will try to keep up to date with PMWeather as much as possible.

Please only use the version of the API compatible with your PMWeather version (for example, 0.14.15.0 is compatible with PMWeather 0.14.15-alpha, but not necessarily with other versions)

## Adding to your project
In your `build.gradle`, add the following:
```groovy
repositories {
  maven {
    name = "DU Maven"
    url = "https://maven.digitalunderworlds.com/snapshots/"
  }
}

dependencies {
  implementation "net.nullved:pmweatherapi:<version>"
}
```

To use the features of the mod, check out the [wiki](https://github.com/RealDarkStudios/PMWeatherAPI/wiki/)!