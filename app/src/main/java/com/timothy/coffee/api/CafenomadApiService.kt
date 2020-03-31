package com.timothy.coffee.api

import com.timothy.coffee.data.model.Cafenomad
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CafenomadApiService{

    @GET("api/v1.2/cafes/{city}")
    fun searchCafes (@Path("city") city:String) : Observable<List<Cafenomad>>
}