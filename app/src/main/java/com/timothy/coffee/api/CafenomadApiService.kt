package com.timothy.coffee.api

import com.timothy.coffee.data.model.Cafenomad
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CafenomadApiService :ApiService{

    @GET("api/v1.2/cafes/{city}")
    fun searchCafes (@Path("city") city:String) : Call<List<Cafenomad>>
}