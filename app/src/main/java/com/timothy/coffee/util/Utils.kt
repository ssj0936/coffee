package com.timothy.coffee.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.core.app.ActivityCompat
import kotlin.math.*

//fun ViewPager2.reduceDragSensitivity() {
//    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
//    recyclerViewField.isAccessible = true
//    val recyclerView = recyclerViewField.get(this) as RecyclerView
//
//    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
//    touchSlopField.isAccessible = true
//    val touchSlop = touchSlopField.get(recyclerView) as Int
//    touchSlopField.set(recyclerView, touchSlop*4)       // "8" was obtained experimentally
//}
val FILTER_TASTY_RATE_0 = 0
val FILTER_TASTY_RATE_1 = 1
val FILTER_TASTY_RATE_2 = 2
val FILTER_TASTY_RATE_3 = 3
val FILTER_TASTY_RATE_4 = 4
val FILTER_TASTY_RATE_5 = 5
val FILTER_NO_TIME_LIMIT = 6
val FILTER_SOCKET = 7
val FILTER_STANDING_DESK = 8

val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()


class Utils {
    companion object{
        private val SP_FILTER_OPTION_TYPE = "SP_FILTER_OPTION_TYPE"
        private val SP_FILTER_OPTION_VALUE = "SP_FILTER_OPTION_VALUE"

        val needPermissions =
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        fun isLocationPermissionGet(context : Context):Boolean{
            val needPermissions =
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            val PERMISSION_REQUEST_CODE = 487

            val permissionMissingList = arrayListOf<String>()

            for(p in needPermissions){
                context.checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED
                permissionMissingList.add(p)
            }

            if(permissionMissingList.size<1)
                return true

            ActivityCompat.requestPermissions(context as Activity, permissionMissingList.toTypedArray(), PERMISSION_REQUEST_CODE)
            return false
        }

        fun distance(
            lat1: Double, lat2: Double, lon1: Double,
            lon2: Double
        ): Double = distance(lat1,lat2,lon1,lon2,0.0,0.0)

        private fun distance(
            lat1: Double, lat2: Double, lon1: Double,
            lon2: Double, el1: Double, el2: Double
        ): Double {
            val R = 6371 // Radius of the earth
            val latDistance = Math.toRadians(lat2 - lat1)
            val lonDistance = Math.toRadians(lon2 - lon1)
            val a =(sin(latDistance / 2) * sin(latDistance / 2)
                        + (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
                            * sin(lonDistance / 2) * sin(lonDistance / 2))
                    )
            val c =2 * atan2(sqrt(a), sqrt(1 - a))
            var distance = R * c * 1000 // convert to meters
            val height = el1 - el2
            distance = distance.pow(2.0) + height.pow(2.0)
            return sqrt(distance)
        }

        fun getGoogleMapDirectionIntent(startLat:Double, startLon:Double, destName:String):Intent{
            return getURLIntent("https://www.google.com/maps/dir/?api=1&origin=$startLat,$startLon&destination=$destName")
        }

        fun getCafenomadURLIntent(id:String):Intent{
            return getURLIntent("https://cafenomad.tw/shop/${id}")
        }

        fun getURLIntent(url:String):Intent{
            return Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        }

        fun isNetworkAvailable(context: Context):Boolean{
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when{
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else ->false
            }
        }

        fun resetFilter(context: Context){
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SP_FILTER_OPTION_TYPE,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().putInt(SP_FILTER_OPTION_VALUE,0).apply()
        }

        fun getFilterSetting(context: Context):Int{
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SP_FILTER_OPTION_TYPE,
                Context.MODE_PRIVATE
            )

            return sharedPreferences.getInt(SP_FILTER_OPTION_VALUE, 0)
        }

        fun setFilterSetting(context: Context, filter:Int){
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SP_FILTER_OPTION_TYPE,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().putInt(SP_FILTER_OPTION_VALUE,filter).apply()
        }

    }
}