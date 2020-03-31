package com.timothy.coffee.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.timothy.coffee.api.RetrofitManager
import com.timothy.coffee.data.model.Cafenomad
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CafenomadDataModel {
    val TAG = "[coffee] CafenomadDataModel"
    private val cafenomadService = RetrofitManager.apiCafenomad

    fun getCafedata(city:String):Observable<List<Cafenomad>> = cafenomadService.searchCafes(city)
}