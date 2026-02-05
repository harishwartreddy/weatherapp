package com.weatherapp.di

import android.content.Context
import com.weatherapp.data.LocationProvider
import com.weatherapp.data.PreferencesRepository
import com.weatherapp.data.cache.DiskCache
import com.weatherapp.data.cache.ImageCache
import com.weatherapp.data.cache.MemoryCache
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun providePreferencesRepository(context: Context): PreferencesRepository {
        return PreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideLocationProvider(context: Context): LocationProvider {
        return LocationProvider(context)
    }

    @Provides
    @Singleton
    fun provideMemoryCache(): MemoryCache {
        return MemoryCache()
    }

    @Provides
    @Singleton
    fun provideDiskCache(context: Context): DiskCache {
        return DiskCache(context)
    }

    @Provides
    @Singleton
    fun provideImageCache(
        memoryCache: MemoryCache,
        diskCache: DiskCache
    ): ImageCache {
        return ImageCache(memoryCache, diskCache)
    }
}
