package com.timothy.coffee.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.timothy.coffee.data.CafenomadDataModel

import com.timothy.coffee.data.DataModel
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.util.LonAndLat
import io.reactivex.Observable

//import io.reactivex.rxjava3.core.Observable

class DataViewModel(application: Application) :AndroidViewModel(application){
    val TAG = "[coffee] DataViewModel"
    private val mAppContext:Context = application.applicationContext
    private val dataModel = DataModel()
    private val cafenomadDataModel = CafenomadDataModel()
    private val counter = MutableLiveData<Int>()

//    var loc : LiveData<LonAndLat>
//    val cityName: MutableLiveData<String> = MutableLiveData()
//    var cafeList:LiveData<List<Cafenomad>>

    init {
//        loc = dataModel.getLocation(mAppContext)
//        loc = Transformations.switchMap(counter,object :Function<Int,LiveData<LonAndLat>>{
//            override fun apply(input: Int?): LiveData<LonAndLat> {
//                return dataModel.getLocation(mAppContext)
//            }
//        })

//        cafeList = Transformations.switchMap(loc,object :Function<LonAndLat,LiveData<List<Cafenomad>>>{
//            override fun apply(input: LonAndLat): LiveData<List<Cafenomad>> {
//                Log.d(TAG,"switchMap_cafelist")
//                Log.d(TAG,"input.city:${input.city}")
//                cityName.value = input.city
//                return cafenomadDataModel.getCafeData(input.city)
//            }
//        })

    }

    fun fetchLocation(i: Int) {
        counter.value = i
    }

    fun getLocationObservable(): Observable<LonAndLat> {
        Log.d(TAG,"getLocationObservable_DataViewModel")
        return dataModel.getLocationObservable(mAppContext)
    }

    fun getLocationiqObservable(lat: Double, lon:Double): Observable<Locationiq>{
        Log.d(TAG,"getLocationObservable_DataViewModel")
        return dataModel.getLocationiqObservable(lat,lon)
    }

    fun getCafenomadObservable(city:String): Observable<List<Cafenomad>>{
        Log.d(TAG,"getLocationObservable_DataViewModel")
        return dataModel.getCafenomadObservable(city)
    }
}