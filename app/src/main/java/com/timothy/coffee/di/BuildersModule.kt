package com.timothy.coffee.di

import com.timothy.coffee.MainActivity
import com.timothy.coffee.view.CafeInfoFragment
import com.timothy.coffee.view.CafeListFragment
import com.timothy.coffee.MainFragment
import com.timothy.coffee.view.MapFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun contributeCafeListFragment(): CafeListFragment

    @ContributesAndroidInjector
    abstract fun contributeCafeInfoFragment(): CafeInfoFragment

    @ContributesAndroidInjector
    abstract fun contributeMapFragment(): MapFragment

}