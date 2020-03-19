package com.timothy.coffee.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.arch.core.util.Function
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.timothy.coffee.data.CafenomadDataModel

import com.timothy.coffee.data.DataModel
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.util.LonAndLat

class DataViewModel(application: Application) :AndroidViewModel(application){
    val TAG = "[coffee] DataViewModel"
    private val mAppContext:Context = application.applicationContext
    private val dataModel = DataModel()
    private val cafenomadDataModel = CafenomadDataModel()
    private val counter = MutableLiveData<Int>()

    var loc : LiveData<LonAndLat>
    val cityName: MutableLiveData<String> = MutableLiveData()
    var cafeList:LiveData<List<Cafenomad>>

    init {
//        loc = dataModel.getLocation(mAppContext)
        loc = Transformations.switchMap(counter,object :Function<Int,LiveData<LonAndLat>>{
            override fun apply(input: Int?): LiveData<LonAndLat> {
                return dataModel.getLocation(mAppContext)
            }
        })

        cafeList = Transformations.switchMap(loc,object :Function<LonAndLat,LiveData<List<Cafenomad>>>{
            override fun apply(input: LonAndLat): LiveData<List<Cafenomad>> {
                Log.d(TAG,"switchMap_cafelist")
                Log.d(TAG,"input.city:${input.city}")
                cityName.value = input.city
                return cafenomadDataModel.getCafeData(input.city)
            }
        })

    }

    fun fetchLocation(i: Int) {
        counter.value = i
    }
}