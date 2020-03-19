package com.timothy.coffee.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitManager private constructor(){

    companion object{
        private const val baseUrlCafenomad = "https://cafenomad.tw/"
        private const val baseUrlLocationiq = "https://us1.locationiq.com/"

        val apiCafenomad by lazy { invoke(baseUrlCafenomad) as CafenomadApiService }
        val apiLocationiq by lazy { invoke(baseUrlLocationiq) as LocationiqApiService}

//        operator fun invoke(baseUrl: String): ApiService {
//            return Retrofit.Builder()
//                .baseUrl(baseUrl)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//                .create(ApiService::class.java)
//        }


        fun <T> getApi(baseURL:String) : T{
            return Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(T::class.java)
        }
//
////        cafenomadService = retrofit.create(CafenomadService::class.java)
    }





}