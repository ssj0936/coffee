package com.timothy.coffee.data

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.util.LonAndLat
import com.timothy.coffee.util.Util
import io.reactivex.Observable

class DataModel {
    val TAG = "[coffee] DataModel"

    val locationiqDataModel = LocationiqDataModel()
    val cafenomadDataModel = CafenomadDataModel()

    fun getLocationObservable(context: Context): Observable<LonAndLat> {
        return Observable.create { emitter ->

            val mLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            val locationRequest = LocationRequest()
            locationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY

//            locationRequest.interval = 1000
//            locationRequest.numUpdates = 1

            val locationCallback = object :LocationCallback() {
                override fun onLocationResult(p0: LocationResult?) {
                    if (p0 == null) {
                        Log.d(TAG, "null")
                        emitter.onError(Throwable("Location fetch Error"))
                    } else {
                        Log.d(TAG, "${p0.lastLocation.longitude},${p0.lastLocation.latitude}")
                        emitter.onNext(
                            LonAndLat(
                                p0.lastLocation.longitude,
                                p0.lastLocation.latitude
                            )
                        )
                        emitter.onComplete()
                    }
                }
            }

            //Exception: Can't create handler inside thread that has not called Looper.prepare()
            //https://www.jianshu.com/p/c9a6c73ed5ce
            mLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            Log.d(TAG,"requestLocationUpdates")
        }
    }

    fun getLocationiqObservable(lat:Double, lon: Double):Observable<Locationiq> = locationiqDataModel.getLocationiqData(lat,lon)

    fun getCafenomadObservable(city:String):Observable<List<Cafenomad>> = cafenomadDataModel.getCafedata(city)
}