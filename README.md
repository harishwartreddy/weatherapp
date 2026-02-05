# Weather App

Android weather application built with Kotlin and Jetpack Compose.

## Features

- Search US cities and view current weather conditions
- Automatic location-based weather on first launch
- Weather icons from OpenWeatherMap
- Offline icon caching (memory + disk)
- Remembers last searched city

## Setup

1. Clone the repository
2. Create `local.properties` in the project root:
   ```
   OPEN_WEATHER_API_KEY=your_api_key_here
   ```
3. Get a free API key from [OpenWeatherMap](https://openweathermap.org/api)
4. Build and run

## Architecture

```
app/src/main/java/com/weatherapp/
├── data/           # Repositories, API, caching
├── domain/         # Use cases and models
├── ui/             # Compose screens and components
├── di/             # Dagger modules
└── navigation/     # App navigation
```

**Tech Stack:**
- Kotlin + Jetpack Compose
- MVVM with Clean Architecture
- Dagger 2 for DI
- Retrofit for networking
- DataStore for preferences
- Coroutines + StateFlow

## API

Uses [OpenWeatherMap API](https://openweathermap.org/api):
- Geocoding API for city search
- Weather API for current conditions
- Weather icons

## Testing

```bash
./gradlew test
```

Unit tests cover:
- JSON parsing
- ViewModel logic
