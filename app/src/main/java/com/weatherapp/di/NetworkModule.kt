package com.weatherapp.di

import com.weatherapp.data.api.OpenWeatherApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OpenWeatherApi.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenWeatherApi(retrofit: Retrofit): OpenWeatherApi {
        return retrofit.create(OpenWeatherApi::class.java)
    }
}
