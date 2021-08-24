package com.timothy.coffee.di

import com.timothy.coffee.MainActivity
import com.timothy.coffee.MainFragment
import com.timothy.coffee.view.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun contributeCafeInfoV2Fragment(): CafeInfoV2Fragment

    @ContributesAndroidInjector
    abstract fun contributeMapFragment(): MapFragment

    @ContributesAndroidInjector
    abstract fun contributeFilterDialogFragment(): FilterDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsPreferenceFragment(): SettingsPreferenceFragment

}