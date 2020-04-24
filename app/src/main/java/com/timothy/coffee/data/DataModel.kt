package com.timothy.coffee.data

import android.content.Context
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.timothy.coffee.api.CafenomadApiService
import com.timothy.coffee.api.LocationiqApiService
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.util.LonAndLat
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataModel
@Inject constructor(
    private val locationiqApiService:LocationiqApiService,
    private val cafenomadApiService:CafenomadApiService
){
    val TAG = "[coffee] DataModel"

    fun getLocationObservable(context: Context): Observable<LonAndLat> {
        return Observable.create { emitter ->
            val mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            val mLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            val locationRequest = LocationRequest()
            locationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY

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

    private val testLonLat = listOf(LonAndLat(121.525,25.0392),//Taipei
        LonAndLat(120.675679,24.123206)//TaiChung
//        LonAndLat(120.4605903,22.6924778)
        )

    fun getLocationObservableTest(context: Context): Observable<LonAndLat> {
        return Observable.interval(10, TimeUnit.SECONDS)
            .flatMap {
                Log.d(TAG,""+testLonLat[it.toInt() % testLonLat.size].toString())
                Observable.just(testLonLat[it.toInt() % testLonLat.size])
            }
    }

    fun getLocationiqObservable(lat:Double, lon: Double):Observable<Locationiq> = locationiqApiService.reverseGeocoding(lat,lon)

    fun getCafenomadObservable(city:String):Observable<List<Cafenomad>> = cafenomadApiService.searchCafes(city)
}