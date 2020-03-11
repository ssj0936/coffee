package com.timothy.coffee.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class DataModel {
    val TAG = "[coffee] DataModel"

    fun getLocation(context: Context): LiveData<List<Double>>?{
        lateinit var result:MutableLiveData<List<Double>>

        val mLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest()
        locationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY

        locationRequest.interval = 1000
        locationRequest.numUpdates = 1

        mLocationProviderClient.requestLocationUpdates(locationRequest, object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                if(p0 == null) return

                Log.d(TAG,"${p0.lastLocation.longitude},${p0.lastLocation.latitude}")
                result.value = listOf(p0.lastLocation.longitude,p0.lastLocation.latitude)
            }
        },null)

        return result
    }
}