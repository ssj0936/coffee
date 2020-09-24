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
import com.google.android.gms.maps.model.LatLng
import com.timothy.coffee.api.CafenomadApiService
import com.timothy.coffee.api.LocationiqApiService
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.Locationiq
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
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

    fun getLastKnownLocation(context: Context):Single<LatLng>{
        return Maybe.create<LatLng> { emitter ->
            getFusedLocationClient(context).lastLocation
                .addOnSuccessListener {
//                    Timber.d("getLastKnownLocation success")
                    if(it!=null){
                        emitter.onSuccess(LatLng(
                            it.latitude,it.longitude)
                        )
                    }else{
                        Timber.d("get location null")
                        emitter.onComplete()
                    }
                }
                .addOnFailureListener {
//                    Timber.d("getLastKnownLocation error")
                    emitter.onComplete()
                }
        }.toSingle()

    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(context: Context): Observable<LatLng> {
        return Observable.create { emitter ->
            // Acquire a reference to the system Location Manager
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Define a listener that responds to location updates
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
//                    Timber.d( "${location.longitude},${location.latitude}")
                    emitter.onNext(
                        LatLng(location.latitude,location.longitude)
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
}