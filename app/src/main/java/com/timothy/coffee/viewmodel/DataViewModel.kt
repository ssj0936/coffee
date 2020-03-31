package com.timothy.coffee.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.timothy.coffee.data.CafenomadDataModel

import com.timothy.coffee.data.DataModel
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.ui.CafeAdapter
import com.timothy.coffee.util.LonAndLat
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

//import io.reactivex.rxjava3.core.Observable

class DataViewModel: ViewModel(){
    val TAG = "[coffee] DataViewModel"
    private val dataModel = DataModel()
    private val counter = MutableLiveData<Int>()

    var loc : MutableLiveData<LonAndLat> = MutableLiveData()
    var cityName: MutableLiveData<String> = MutableLiveData()
    var cafeList: MutableLiveData<List<Cafenomad>> = MutableLiveData()

    fun getCafeList(context: Context) {
        getLocationObservable(context)
            .observeOn(Schedulers.newThread())
            .flatMap{
                it?.let {
                    Log.d(TAG,"first flatmap : ${Thread.currentThread().name}")
                    Log.d(TAG,"longitude:${it.longitude},latitude:${it.latitude}")
                    loc.postValue(it)
                    getLocationiqObservable(it.latitude!!,it.longitude!!)
                }
            }

            .flatMap {
                it?.let {
                    Log.d(TAG,"second flatmap : ${Thread.currentThread().name}")
                    cityName.postValue(it.address?.state)
                    getCafenomadObservable(it.address!!.state!!)
                }

            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object:io.reactivex.Observer<List<Cafenomad>>{
                override fun onComplete() {}

                override fun onSubscribe(d: Disposable?) {}

                override fun onNext(t: List<Cafenomad>) {
                    Log.d(TAG,"subscribe onNext: ${Thread.currentThread().name}")
                    cafeList.value = t
                    Log.d(TAG,"cafe list Changed")
                }

                override fun onError(e: Throwable?) {
                    Log.e(TAG, "Exception: "+Log.getStackTraceString(e))
                }
            })
    }

    private fun getLocationObservable(context: Context): Observable<LonAndLat> {
        Log.d(TAG,"getLocationObservable_DataViewModel")
        return dataModel.getLocationObservable(context)
    }

    private fun getLocationiqObservable(lat: Double, lon:Double): Observable<Locationiq>{
        Log.d(TAG,"getLocationiqObservable_DataViewModel")
        return dataModel.getLocationiqObservable(lat,lon)
    }

    private fun getCafenomadObservable(city:String): Observable<List<Cafenomad>>{
        Log.d(TAG,"getCafenomadObservable_DataViewModel")
        return dataModel.getCafenomadObservable(city)
    }
}