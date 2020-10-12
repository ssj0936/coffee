package com.timothy.coffee.data

import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Maybe
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataModel
@Inject constructor()
{
    private val locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(10*1000)
        .setFastestInterval(5*1000)
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
                    Timber.d("getLastKnownLocation success")
                    if(it!=null){
                        emitter.onSuccess(LatLng(
                            it.latitude,it.longitude)
                        )
                    }else{
                        Timber.d("get location null, starting to get location from Location updates")
                        getFusedLocationClient(context).requestLocationUpdates(locationRequest,
                            object :LocationCallback(){
                                override fun onLocationResult(locationResult : LocationResult?) {
                                    if(locationResult == null) return

                                    var getResult = false
                                    for(location in locationResult.locations){
                                        if(location != null){
                                            Timber.d("get location successfully from Location updates")
                                            getResult = true
                                            emitter.onSuccess(LatLng(
                                                location.latitude,location.longitude)
                                            )
                                            getFusedLocationClient(context).removeLocationUpdates(this)
                                            break
                                        }
                                    }

                                    if(!getResult)
                                        emitter.onComplete()
                                }
                            },Looper.getMainLooper())
                    }
                }
                .addOnFailureListener {
                    Timber.d("getLastKnownLocation error")
                    emitter.onComplete()
                }
        }.toSingle()
    }
}