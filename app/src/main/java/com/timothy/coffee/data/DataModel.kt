package com.timothy.coffee.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.timothy.coffee.util.LonAndLat

class DataModel {
    val TAG = "[coffee] DataModel"

    val mapToken = "24ba6cb03a267e"

    val cityList = listOf("taipei","taichung","tainan","chiayi","kaohsiung","taoyuan","yilan","changhua","hualien","hsinchu")

    fun getLocation(context: Context): LiveData<LonAndLat>{
        var result:MutableLiveData<LonAndLat> = MutableLiveData()

        val mLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest()
        locationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY

        locationRequest.interval = 1000
        locationRequest.numUpdates = 1

        mLocationProviderClient.requestLocationUpdates(locationRequest, object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                if(p0 == null) {
                    Log.d(TAG,"null")
                    result.value = LonAndLat(null,null,"")
                    return
                }

                Log.d(TAG,"${p0.lastLocation.longitude},${p0.lastLocation.latitude}")
                val returnCity = cityList[(cityList.indices).random()]
                result.value = LonAndLat(p0.lastLocation.longitude,p0.lastLocation.latitude,returnCity)
            }
        },null)
        return result
    }
}