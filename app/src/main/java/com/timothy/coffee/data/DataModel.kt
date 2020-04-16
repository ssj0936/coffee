package com.timothy.coffee.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.timothy.coffee.api.CafenomadApiService
import com.timothy.coffee.api.LocationiqApiService
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.util.LonAndLat
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataModel
@Inject constructor(
    private val locationiqApiService:LocationiqApiService,
    private val cafenomadApiService:CafenomadApiService
){
    val TAG = "[coffee] DataModel"

//    @Inject
//    lateinit var locationiqApiService:LocationiqApiService
//
//    @Inject
//    lateinit var cafenomadApiService:CafenomadApiService

    fun getLocationObservable(context: Context): Observable<LonAndLat> {
        return Observable.create { emitter ->

//            val mLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
//            val locationRequest = LocationRequest()
//            locationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
//
//            val locationCallback = object :LocationCallback() {
//                override fun onLocationResult(p0: LocationResult?) {
//                    if (p0 == null) {
//                        Log.d(TAG, "null")
//                        emitter.onError(Throwable("Location fetch Error"))
//                    } else {
//                        Toast.makeText(context,"${p0.lastLocation.longitude},${p0.lastLocation.latitude}",Toast.LENGTH_SHORT).show()
//                        Log.d(TAG, "${p0.lastLocation.longitude},${p0.lastLocation.latitude}")
//                        emitter.onNext(
//                            LonAndLat(
//                                p0.lastLocation.longitude,
//                                p0.lastLocation.latitude
//                            )
//                        )
////                        emitter.onComplete()
//                    }
//                }
//            }
//
//            //Exception: Can't create handler inside thread that has not called Looper.prepare()
//            //https://www.jianshu.com/p/c9a6c73ed5ce
//            mLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            // Acquire a reference to the system Location Manager
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Define a listener that responds to location updates
            val locationListener = object : LocationListener {

                override fun onLocationChanged(location: Location) {
                    // Called when a new location is found by the network location provider.
                    if (location == null) {
                        Log.d(TAG, "null")
                        emitter.onError(Throwable("Location fetch Error"))
                    } else {
                        Toast.makeText(
                            context,
                            "${location.longitude},${location.latitude}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "${location.longitude},${location.latitude}")
                        emitter.onNext(
                            LonAndLat(
                                location.longitude,
                                location.latitude
                            )
                        )
                    }
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                }

                override fun onProviderEnabled(provider: String) {
                }

                override fun onProviderDisabled(provider: String) {
                }
            }

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5f,locationListener)

            Log.d(TAG,"requestLocationUpdates")
        }
    }

    fun getLocationiqObservable(lat:Double, lon: Double):Observable<Locationiq> = locationiqApiService.reverseGeocoding(lat,lon)

    fun getCafenomadObservable(city:String):Observable<List<Cafenomad>> = cafenomadApiService.searchCafes(city)
}