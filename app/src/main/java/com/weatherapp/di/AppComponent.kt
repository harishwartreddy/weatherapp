package com.weatherapp.di

import android.content.Context
import com.weatherapp.MainActivity
import com.weatherapp.ui.viewmodel.WeatherViewModelFactory
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        RepositoryModule::class,
        UseCaseModule::class,
        ViewModelModule::class
    ]
)
interface AppComponent {

    fun inject(activity: MainActivity)

    fun weatherViewModelFactory(): WeatherViewModelFactory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}
