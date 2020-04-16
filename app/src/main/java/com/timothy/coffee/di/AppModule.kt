package com.timothy.coffee.di

import androidx.room.Room
import com.timothy.coffee.CafeApp
import com.timothy.coffee.api.CafenomadApiService
import com.timothy.coffee.api.LocationiqApiService
import com.timothy.coffee.data.db.CafeDao
import com.timothy.coffee.data.db.CafeDb
import com.timothy.coffee.data.db.CafeDb.Companion.DB_NAME
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

    //API service
    @Provides
    @Singleton
    fun provideCafenomadApiService(): CafenomadApiService{
        return getApi(baseUrlCafenomad)
    }

    @Provides
    @Singleton
    fun provideLocationiqApiService(): LocationiqApiService {
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

    //Room - Cafe db
    @Provides
    @Singleton
    fun provideCafeDao(app:CafeApp): CafeDao{
        val db = Room.databaseBuilder(app,CafeDb::class.java,DB_NAME).build()
        return db.cafeDao()
    }
}