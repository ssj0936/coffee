package com.timothy.coffee.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.*

fun ViewPager2.reduceDragSensitivity() {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop*4)       // "8" was obtained experimentally
}

class Utils {
    companion object{
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
            return Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$startLat,$startLon&destination=$destName")
            )
        }

    }
}