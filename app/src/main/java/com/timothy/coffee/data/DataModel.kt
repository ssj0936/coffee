package com.timothy.coffee.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.timothy.coffee.api.CafenomadApiService
import com.timothy.coffee.api.LocationiqApiService
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.util.LonAndLat
import io.reactivex.Maybe
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataModel
@Inject constructor(
    private val locationiqApiService:LocationiqApiService,
    private val cafenomadApiService:CafenomadApiService
){

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private fun getFusedLocationClient(context: Context):FusedLocationProviderClient{
        if(!::fusedLocationClient.isInitialized)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return fusedLocationClient
    }

    private fun getLastKnownLocation(context: Context):Observable<LonAndLat>{
        return Maybe.create<LonAndLat> { emitter ->
            getFusedLocationClient(context).lastLocation
                .addOnSuccessListener {
//                    Timber.d("getLastKnownLocation success")
                    if(it!=null){
                        emitter.onSuccess(LonAndLat(
                            it.longitude,
                            it.latitude
                        ))
                    }else{
                        Timber.d("get location null")
                        emitter.onComplete()
                    }
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
            getLastKnownLocation(context)
//            getCurrentLocation(context)
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