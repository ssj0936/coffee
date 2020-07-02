package com.timothy.coffee.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.timothy.coffee.R

import com.timothy.coffee.data.DataModel
import com.timothy.coffee.data.DataSource
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.util.LonAndLat
import com.timothy.coffee.util.Movement
import com.timothy.coffee.util.Utils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val dataModel:DataModel,
    private val dataSource: DataSource
): ViewModel(){

    var loc : MutableLiveData<LonAndLat> = MutableLiveData()
    val chosenCafe: MutableLiveData<Cafenomad> = MutableLiveData()
    val cafeList:MutableLiveData<List<Cafenomad>> = MutableLiveData()
    var lastMove = Movement(isClickMap = false, isClickList = false)

    private var cityName: MutableLiveData<String> = MutableLiveData()

    @SuppressLint("ResourceType")
    fun getCafeList(context: Context, isForce:Boolean):Observable<List<Cafenomad>> {
        return getLocationObservable(context)
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .flatMap{lonlat ->
//                Timber.d("first flatmap : ${Thread.currentThread().name} : ${Thread.currentThread().id}")
//                Timber.d("longitude:${lonlat.longitude},latitude:${lonlat.latitude}")

                if(lonlat != loc.value || isForce){
//                    Timber.d("different lon && lat:${lonlat.latitude},${lonlat.longitude}")
                    loc.postValue(lonlat)
                    getLocationiqObservable(lonlat.latitude,lonlat.longitude)
                }else{
//                    Timber.d("same lon && lat")
                    Observable.empty<Locationiq>()
                }
            }
            .flatMap {locationiq ->
//                Timber.d("second flatmap : ${Thread.currentThread().name} : ${Thread.currentThread().id}")

                if(locationiq.address?.state != cityName.value || isForce){
                    cityName.postValue(locationiq.address?.state)
                    dataSource.query(locationiq.address!!.state!!)
                }else{
//                    Timber.d("same city")
                    Observable.empty<List<Cafenomad>>()
                }
            }
            .map { cafes ->
//                Timber.d("cafes:${cafes}")
                cafes.stream().forEach {cafe->
//                    Timber.d("cafe:${cafe}")
                    loc.value?.let{
                        cafe.distance = Utils.distance(it.latitude,cafe.latitude.toDouble(),
                            it.longitude,cafe.longitude.toDouble()).toInt()
                    }
                }

                cafes.stream()
                    .filter{cafe->
                        val range = PreferenceManager.getDefaultSharedPreferences(context)
                            .getInt(context.getString(R.string.preference_key_search_range)
                                ,context.resources.getInteger(R.dimen.range_cafe_nearby_min))*1000
                        cafe.distance < range
                    }
                    .sorted{
                        cafe1,cafe2->cafe1.distance.compareTo(cafe2.distance)
                    }
                    .collect(Collectors.toList())
            }
    }

    private fun getLocationObservable(context: Context): Observable<LonAndLat> {
//        Timber.d("get Test Location")
        return dataModel.getLocationObservable(context)
    }

    private fun getLocationiqObservable(lat: Double, lon:Double): Observable<Locationiq>{
//        Timber.d("get Locationiq")
        return dataModel.getLocationiqObservable(lat,lon)
    }

    private fun getCafenomadObservable(city:String): Observable<List<Cafenomad>>{
//        Timber.d("get Cafenomad")
        return dataModel.getCafenomadObservable(city)
    }

    fun saveFavorite(cafeId:String){
        Observable.just(cafeId)
            .subscribeOn(Schedulers.io())
            .subscribe {
                dataSource.insertFavorite(it)
            }
    }
}