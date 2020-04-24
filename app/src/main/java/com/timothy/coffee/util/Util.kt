package com.timothy.coffee.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class Util {
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
    }
}