package com.timothy.coffee.api

import com.timothy.coffee.data.model.Locationiq
import io.reactivex.Observable

import retrofit2.http.GET
import retrofit2.http.Query

interface LocationiqApiService {

//    https://us1.locationiq.com/v1/reverse.php?key=24ba6cb03a267e&lat=25.0392&lon=121.525&format=json
    @GET("v1/reverse.php")
    fun reverseGeocoding(@Query("lat") lat:Double,
                         @Query("lon") lon:Double,
                         @Query("format") format:String="json",
                         @Query("key") key:String = "24ba6cb03a267e"): Observable<Locationiq>
}