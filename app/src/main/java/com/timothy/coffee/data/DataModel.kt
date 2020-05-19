package com.timothy.coffee.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.timothy.coffee.api.CafenomadApiService
import com.timothy.coffee.api.LocationiqApiService
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.util.LonAndLat
import com.timothy.coffee.util.Util
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataModel
@Inject constructor(
    private val locationiqApiService:LocationiqApiService,
    private val cafenomadApiService:CafenomadApiService
){
    private fun getLastKnownLocation(context: Context):Observable<LonAndLat>{
        return Maybe.create<LonAndLat> { emitter ->
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener {
//                    Timber.d("getLastKnownLocation success")
                    emitter.onSuccess(LonAndLat(
                        it.longitude,
                        it.latitude
                    ))
                }
                .addOnFailureListener {
//                    Timber.d("getLastKnownLocation error")
                    emitter.onComplete()
                }
        }.toObservable()

    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(context: Context): Observable<LonAndLat> {
        return Observable.create { emitter ->
            // Acquire a reference to the system Location Manager
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Define a listener that responds to location updates
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
//                    Timber.d( "${location.longitude},${location.latitude}")
                    emitter.onNext(
                        LonAndLat(
                            location.longitude,
                            location.latitude
                        )
                    )
                    emitter.onComplete()
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {}
            }

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5f,locationListener,Looper.getMainLooper())

//            Timber.d("requestLocationUpdates")

        }
    }

    private val testLonLat = listOf(LonAndLat(121.525,25.0392),//Taipei
        LonAndLat(120.675679,24.123206)//TaiChung
//        LonAndLat(120.4605903,22.6924778)
        )

    //get last location first and show it
    //then fetch current location
    fun getLocationObservable(context: Context):Observable<LonAndLat>{
        return Observable.concatArray(
            getLastKnownLocation(context),
            getCurrentLocation(context)
        )
    }

    fun getLocationObservableTest(context: Context): Observable<LonAndLat> {
//        return Observable.interval(5, TimeUnit.SECONDS)
//            .flatMap {
//                Timber.d(""+testLonLat[it.toInt() % testLonLat.size].toString())
//                Observable.just(testLonLat[it.toInt() % testLonLat.size])
//            }

        return Observable.just(testLonLat[0])
    }

    fun getLocationiqObservable(lat:Double, lon: Double):Observable<Locationiq> = locationiqApiService.reverseGeocoding(lat,lon)

    fun getCafenomadObservable(city:String):Observable<List<Cafenomad>> = cafenomadApiService.searchCafes(city)
}