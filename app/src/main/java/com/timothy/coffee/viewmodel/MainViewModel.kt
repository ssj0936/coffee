package com.timothy.coffee.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
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
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val dataModel:DataModel,
    private val dataSource: DataSource
): ViewModel(){

    var loc : MutableLiveData<LonAndLat> = MutableLiveData()
    val chosenCafe: MutableLiveData<CafenomadDisplay> = MutableLiveData()
    val cafeList:MutableLiveData<List<CafenomadDisplay>> = MutableLiveData()
    var lastMove = Movement(isClickMap = false, isClickList = false)

    private var cityName: MutableLiveData<String> = MutableLiveData()

    @SuppressLint("ResourceType")
    fun getCafeList(context: Context, isForce:Boolean):Observable<List<CafenomadDisplay>> {
        return getLocationObservable(context)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
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
                    Observable.empty<List<CafenomadDisplay>>()
                }
            }
            .observeOn(Schedulers.computation())
            .map { cafes ->
//                Timber.d("cafes:${cafes}")
                cafes.stream().forEach {cafe->
//                    Timber.d("cafe:${cafe}")
                    loc.value?.let{
                        cafe.cafenomad.distance = Utils.distance(it.latitude,cafe.cafenomad.latitude.toDouble(),
                            it.longitude,cafe.cafenomad.longitude.toDouble()).toInt()
                    }
                }

                cafes.stream()
                    .filter{cafe->
                        val range = PreferenceManager.getDefaultSharedPreferences(context)
                            .getInt(context.getString(R.string.preference_key_search_range)
                                ,context.resources.getInteger(R.dimen.range_cafe_nearby_min))*1000
                        cafe.cafenomad.distance < range
                    }
                    .sorted{
                        cafe1,cafe2->cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance)
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

    @SuppressLint("CheckResult")
    fun setFavorite(cafeId:String){
        Single.just(cafeId)
            .observeOn(Schedulers.io())
            .subscribe { id ->
                if(dataSource.insertFavorite(id) > 0){
                    //cafeList update
                    cafeList.value?.let{
                        val updatedItem = it.stream().filter{cafe->
                            cafe.cafenomad.id == id
                        }.findAny().orElse(null)

                        if(updatedItem != null){
                            updatedItem.isFavorite = true
                            cafeList.postValue(cafeList.value)
                        }
                    }

                    //chosen Cafe update
                    if(id == chosenCafe.value?.cafenomad?.id){
                        chosenCafe.value?.let {
                            it.isFavorite = true
                        }
                        chosenCafe.postValue(chosenCafe.value)
                    }
                }
            }
    }

    @SuppressLint("CheckResult")
    fun deleteFavorite(cafeId:String){
        Single.just(cafeId)
            .observeOn(Schedulers.io())
            .subscribe { id ->
                if(dataSource.deleteFavorite(id)>0){
                    //cafeList update
                    cafeList.value?.let{
                        val updatedItem = it.stream().filter{cafe->
                            cafe.cafenomad.id == id
                        }.findAny().orElse(null)

                        if(updatedItem != null){
                            updatedItem.isFavorite = false
                            cafeList.postValue(cafeList.value)
                        }
                    }

                    //chosen Cafe update
                    if(id == chosenCafe.value?.cafenomad?.id){
                        chosenCafe.value?.let {
                            it.isFavorite = false
                        }
                        chosenCafe.postValue(chosenCafe.value)
                    }
                }
            }
    }
}