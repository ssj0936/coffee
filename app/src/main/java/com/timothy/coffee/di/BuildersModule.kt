package com.timothy.coffee.di

import com.timothy.coffee.CafeListFragment
import com.timothy.coffee.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeMainActivity():MainActivity

    @ContributesAndroidInjector
    abstract fun contributeCafeListFragment():CafeListFragment
}