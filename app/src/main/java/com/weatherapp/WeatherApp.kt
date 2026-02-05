package com.weatherapp

import android.app.Application
import com.weatherapp.di.AppComponent
import com.weatherapp.di.DaggerAppComponent

class WeatherApp : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(applicationContext)
    }
}
