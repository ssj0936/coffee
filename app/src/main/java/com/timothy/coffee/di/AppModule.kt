package com.timothy.coffee.di

import androidx.room.Room
import com.timothy.coffee.BuildConfig
import com.timothy.coffee.CafeApp
import com.timothy.coffee.api.CafenomadApiService
import com.timothy.coffee.api.LocationiqApiService
import com.timothy.coffee.data.db.CafeDao
import com.timothy.coffee.data.db.CafeDb
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule {
    private val baseUrlCafenomad = "https://cafenomad.tw/"
    private val baseUrlLocationiq = "https://us1.locationiq.com/"

    @Provides
    @Singleton
    fun provideCafenomadApiService(httpClient:OkHttpClient): CafenomadApiService{
        return getApi(baseUrlCafenomad,httpClient)
    }

    @Provides
    @Singleton
    fun provideLocationiqApiService(httpClient:OkHttpClient): LocationiqApiService {
        return getApi(baseUrlLocationiq,httpClient)
    }

    @Provides
    @Singleton
    fun provideHttpClient():OkHttpClient{
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
        if (BuildConfig.DEBUG) {
            logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
//            .addInterceptor(logInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    private inline fun <reified T> getApi(baseURL:String,httpClient:OkHttpClient) : T{
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient)
            .build()
            .create(T::class.java)
    }

    @Provides
    @Singleton
    fun provideCafeDao(app:CafeApp): CafeDao {
        val dbName = "cafeapp_db"
        val db = Room.databaseBuilder(app,CafeDb::class.java,dbName).build()
        return db.cafeDao()
    }
}