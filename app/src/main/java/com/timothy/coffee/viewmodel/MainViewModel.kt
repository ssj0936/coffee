package com.timothy.coffee.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.timothy.coffee.data.DataModel
import com.timothy.coffee.data.DataSource
import com.timothy.coffee.data.db.CafeDao
import com.timothy.coffee.data.model.CafeSearchResult
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.util.LonAndLat
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.stream.Collectors
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val dataModel:DataModel,
    private val dataSource: DataSource
): ViewModel(){
    val TAG = "[coffee] DataViewModel"

    var loc : MutableLiveData<LonAndLat> = MutableLiveData()
    var cityName: MutableLiveData<String> = MutableLiveData()
    var cafeList: MutableLiveData<List<Cafenomad>> = MutableLiveData()

    fun getCafeList(context: Context) {
        getLocationObservable(context)
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .flatMap{lonlat ->
                Log.d(TAG,"first flatmap : ${Thread.currentThread().name} : ${Thread.currentThread().id}")
                Log.d(TAG,"longitude:${lonlat.longitude},latitude:${lonlat.latitude}")

                if(lonlat != loc.value){
                    loc.postValue(lonlat)
                    getLocationiqObservable(lonlat.latitude,lonlat.longitude)
                }else{
                    Log.d(TAG,"same lon && lat")
                    Observable.empty<Locationiq>()
                }
            }
            .flatMap {locationiq ->
                locationiq?.let {
                    Log.d(TAG,"second flatmap : ${Thread.currentThread().name} : ${Thread.currentThread().id}")
                    if(it.address?.state != cityName.value){
                        cityName.postValue(it.address?.state)
                        dataSource.query(it.address!!.state!!)
                    }else{
                        Log.d(TAG,"same city")
                        Observable.empty<List<Cafenomad>>()
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object:Observer<List<Cafenomad>>{
                override fun onComplete() {
                    Log.d(TAG,"subscribe onComplete")
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onNext(t: List<Cafenomad>) {
                    cafeList.value = t
                    Log.d(TAG,"onNext")
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Exception: "+Log.getStackTraceString(e))
                }
            })
    }

    private fun getLocationObservable(context: Context): Observable<LonAndLat> {
        Log.d(TAG,"get Test Location")
        return dataModel.getLocationObservableTest(context)
    }

    private fun getLocationiqObservable(lat: Double, lon:Double): Observable<Locationiq>{
        Log.d(TAG,"get Locationiq")
        return dataModel.getLocationiqObservable(lat,lon)
    }

    private fun getCafenomadObservable(city:String): Observable<List<Cafenomad>>{
        Log.d(TAG,"get Cafenomad")
        return dataModel.getCafenomadObservable(city)
    }
}