package com.timothy.coffee.api

import retrofit2.http.Field
import retrofit2.http.GET

interface LocationiqApiService :ApiService {

//    https://us1.locationiq.com/v1/reverse.php?key=24ba6cb03a267e&lat=25.0392&lon=121.525&format=json
    @GET("v1/reverse.php")
    fun reverseGeocoding(@Field("lat") lat:Double,
                         @Field("lon") lon:Double,
                         @Field("format") format:String="json",
                         @Field("key") key:String = "24ba6cb03a267e")
}