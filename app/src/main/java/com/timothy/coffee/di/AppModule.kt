package com.timothy.coffee.di

import com.timothy.coffee.api.CafenomadApiService
import com.timothy.coffee.api.LocationiqApiService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class AppModule {
    private val baseUrlCafenomad = "https://cafenomad.tw/"
    private val baseUrlLocationiq = "https://us1.locationiq.com/"

    @Provides
    @Singleton
    fun getCafenomadApiService(): CafenomadApiService{
        return getApi(baseUrlCafenomad)
    }

    @Provides
    @Singleton
    fun getLocationiqApiService(): LocationiqApiService {
        return getApi(baseUrlLocationiq)
    }

    private inline fun <reified T> getApi(baseURL:String) : T{
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(T::class.java)
    }
}