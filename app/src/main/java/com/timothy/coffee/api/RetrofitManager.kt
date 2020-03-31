package com.timothy.coffee.api

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitManager private constructor(){

    companion object{
        private const val baseUrlCafenomad = "https://cafenomad.tw/"
        private const val baseUrlLocationiq = "https://us1.locationiq.com/"

        val apiCafenomad by lazy { getApi<CafenomadApiService>(baseUrlCafenomad)}
        val apiLocationiq by lazy { getApi<LocationiqApiService>(baseUrlLocationiq)}

        private inline fun <reified T> getApi(baseURL:String) : T{
            return Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(T::class.java)
        }
    }





}