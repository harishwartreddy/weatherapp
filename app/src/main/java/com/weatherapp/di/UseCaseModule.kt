package com.weatherapp.di

import com.weatherapp.data.OpenWeatherRepository
import com.weatherapp.domain.GetWeatherByCityUseCase
import com.weatherapp.domain.GetWeatherByLocationUseCase
import com.weatherapp.domain.SearchCitiesUseCase
import dagger.Module
import dagger.Provides

@Module
class UseCaseModule {

    @Provides
    fun provideGetWeatherByCityUseCase(
        repository: OpenWeatherRepository
    ): GetWeatherByCityUseCase {
        return GetWeatherByCityUseCase(repository)
    }

    @Provides
    fun provideGetWeatherByLocationUseCase(
        repository: OpenWeatherRepository
    ): GetWeatherByLocationUseCase {
        return GetWeatherByLocationUseCase(repository)
    }

    @Provides
    fun provideSearchCitiesUseCase(
        repository: OpenWeatherRepository
    ): SearchCitiesUseCase {
        return SearchCitiesUseCase(repository)
    }
}
