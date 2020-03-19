package com.timothy.coffee.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.timothy.coffee.api.RetrofitManager
import com.timothy.coffee.data.model.Cafenomad
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CafenomadDataModel {
    val TAG = "[coffee] CafenomadDataModel"
    private val cafenomadService = RetrofitManager.apiCafenomad

    fun getCafeData(query:String):LiveData<List<Cafenomad>>{
        val cafes = MutableLiveData<List<Cafenomad>>()
        cafenomadService.searchCafes(query)
            .enqueue(object : Callback<List<Cafenomad>>{
                override fun onResponse(
                    call: Call<List<Cafenomad>>,
                    response: Response<List<Cafenomad>>
                ) {
                    Log.d(TAG,"onResponse")
                    cafes.value = response.body()
                }

                override fun onFailure(call: Call<List<Cafenomad>>, t: Throwable) {
                    Log.d(TAG,"onFailure:${t}")
                }
            })

        return cafes
    }
}