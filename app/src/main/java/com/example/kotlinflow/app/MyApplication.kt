package com.example.kotlinflow.app

import android.app.Application
import com.example.kotlinflow.BuildConfig
import com.example.kotlinflow.di.appModule
import com.example.kotlinflow.di.databaseModule
import com.example.kotlinflow.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule, databaseModule, networkModule)
        }

        setupTimber()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }
}