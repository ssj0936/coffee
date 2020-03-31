package com.timothy.coffee.data

import com.timothy.coffee.api.LocationiqApiService
import com.timothy.coffee.api.RetrofitManager
import com.timothy.coffee.data.model.Locationiq
import io.reactivex.Observable
//import io.reactivex.rxjava2.core.Observable

class LocationiqDataModel{
    val TAG = "[coffee] LocationiqDataModel"
    val apiservice = RetrofitManager.apiLocationiq

    fun getLocationiqData(lat:Double,lon: Double): Observable<Locationiq> = apiservice.reverseGeocoding(lat,lon)
}