![PMWeatherAPI Logo](https://cdn.modrinth.com/data/cached_images/f7b06beba44c5d08763305f5a0c3480611610230.png)

## Support

Need help? Join the Discord server [here](https://discord.gg/B6YKNVB6pW)!

## About

![pmweatherapi](https://img.shields.io/maven-metadata/v?label=Latest%20Snapshot&metadataUrl=https://maven.digitalunderworlds.com/snapshots/net/nullved/pmweatherapi/maven-metadata.xml)

**PMWeatherAPI** is an **unofficial** API designed to simplify interacting with [ProtoManly's Weather Mod](https://modrinth.com/mod/protomanlys-weather/) for those who want to create addons.

### Current features:

* Nearby Storm Detection
* Nearby Radar Detection
* Custom Radar Modes
* Radar Overlays
* Storm Builder
* Color Maps
* Events

> ⚠️ This API is currently experimental due to PMWeather still being in alpha. Bugs should be expected.
> The API will aim to stay updated with PMWeather as closely as possible.

Make sure you only use the API version that matches your PMWeather version (e.g., `0.14.15.X` works with `PMWeather 0.14.15-alpha` but may not work with other versions).

## Adding to Your Project

In your `build.gradle`, add the following:

```groovy
repositories {
  maven {
      name = "Modrinth"
      url = "https://api.modrinth.com/maven"
  }
  maven {
    name = "DU Maven"
    url = "https://maven.digitalunderworlds.com/snapshots/"
  }
}

dependencies {
  implementation "maven.modrinth:protomanlys-weather:${pmweather_ver}-alpha"
  implementation "net.nullved:pmweatherapi:${pmweatherapi_ver}"
}
```

In your `gradle.properties`, add:

```properties
pmweather_ver=0.15.1
pmweatherapi_ver=0.15.1.0
```

For usage examples and more details, check out the [wiki](https://github.com/RealDarkStudios/PMWeatherAPI/wiki/).
