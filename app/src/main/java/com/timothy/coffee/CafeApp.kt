package com.timothy.coffee

import android.app.Application
import android.content.Context
import com.timothy.coffee.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject


class CafeApp:Application(),HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector:DispatchingAndroidInjector<Any>

    companion object {
        lateinit var cafeApplicationContext: Context
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)

        cafeApplicationContext = applicationContext
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
}