package com.timothy.coffee

import android.app.Application
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

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)

    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
}