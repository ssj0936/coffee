package com.timothy.coffee.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng
import com.timothy.coffee.R

import com.timothy.coffee.data.DataModel
import com.timothy.coffee.data.DataSource
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.data.model.Locationiq
import com.timothy.coffee.util.Movement
import com.timothy.coffee.util.Utils
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.stream.Collectors
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val dataModel:DataModel,
    private val dataSource: DataSource
): ViewModel(){

    var loc : MutableLiveData<LatLng> = MutableLiveData()
    val chosenCafe: MutableLiveData<CafenomadDisplay> = MutableLiveData()
    val cafeListAll:MutableLiveData<List<CafenomadDisplay>> = MutableLiveData()
    val cafeListDisplay:MutableLiveData<List<CafenomadDisplay>> = MutableLiveData()

    val favoriteOnly:MutableLiveData<Boolean> = MutableLiveData(false)
    val sortType:MutableLiveData<String> = MutableLiveData()
    var lastSortType:String? = null

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
                    val range = context.resources.getInteger(R.dimen.range_cafe_nearby_max)
                    loc.postValue(lonlat)
                    dataSource.queryV2(lonlat.latitude,lonlat.longitude,range)
                }else{
//                    Timber.d("same lon && lat")
                    Observable.empty<List<CafenomadDisplay>>()
                }
            }
            .observeOn(Schedulers.computation())
            .map { cafes ->
//                Timber.d("cafes:${cafes}")

                cafes.stream().forEach {cafe->
                    loc.value?.let{
                        cafe.cafenomad.distance = Utils.distance(it.latitude,cafe.cafenomad.latitude.toDouble(),
                            it.longitude,cafe.cafenomad.longitude.toDouble()).toInt()
                    }
                }

                cafes.stream()
                    .filter{cafe->
                        val range = context.resources.getInteger(R.dimen.range_cafe_nearby_max)*1000
                        cafe.cafenomad.distance < range
                    }
                    .sorted{
                            cafe1,cafe2->cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance)
                    }
                    .collect(Collectors.toList())
            }
    }

    private fun getLocationObservable(context: Context): Observable<LatLng> {
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

    @SuppressLint("ResourceType")
    fun setCafeDataFavoriteOnly(favoriteOnly:Boolean, context: Context){
        if(cafeListAll.value == null) return

        cafeListDisplay.value = cafeListAll.value?.let {
            it.stream()
                .filter { cafe ->
                    val range = PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt(
                            context.getString(R.string.preference_key_search_range)
                            , context.resources.getInteger(R.dimen.range_cafe_nearby_min)
                        ) * 1000

                    (cafe.cafenomad.distance < range) && (if(favoriteOnly) cafe.isFavorite else true)
                }.collect(Collectors.toList())
        }
    }

    @SuppressLint("ResourceType")
    fun setCafeViaSortType(type:String, context: Context){
        if(cafeListAll.value == null || type == lastSortType) return
        Timber.d("SORT TYPE:${type}")

        cafeListDisplay.value = cafeListAll.value?.let {
            it.stream()
                .filter { cafe ->
                    val range = PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt(
                            context.getString(R.string.preference_key_search_range)
                            , context.resources.getInteger(R.dimen.range_cafe_nearby_min)
                        ) * 1000

                    val isSetFavoriteOnly = type == context.resources.getString(R.string.filter_label_favorite_only)
                    (cafe.cafenomad.distance < range) && (if(isSetFavoriteOnly) cafe.isFavorite else true)
                }.sorted{cafe1,cafe2 ->
                    when(type){
                        context.resources.getString(R.string.filter_label_distance_farthest) ->{
                                cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance)*-1
                        }
                        context.resources.getString(R.string.filter_label_star) ->{
                                cafe1.cafenomad.tastyLevel.compareTo(cafe2.cafenomad.tastyLevel)*-1
                        }
                        else -> cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance)
                    }
                }.collect(Collectors.toList())
        }
        lastSortType = type
//        cafeListDisplay.value = cafeListAll.value?.let {
//            it.stream()
//                .filter { cafe ->
//                    val range = PreferenceManager.getDefaultSharedPreferences(context)
//                        .getInt(
//                            context.getString(R.string.preference_key_search_range)
//                            , context.resources.getInteger(R.dimen.range_cafe_nearby_min)
//                        ) * 1000
//
//                    (cafe.cafenomad.distance < range) && (if(favoriteOnly) cafe.isFavorite else true)
//                }.collect(Collectors.toList())
//        }
    }


    @SuppressLint("CheckResult")
    fun setFavorite(cafeId:String){
        Single.just(cafeId)
            .observeOn(Schedulers.io())
            .subscribe { id ->
                if(dataSource.insertFavorite(id) > 0){
                    //cafeList update
                    cafeListAll.value?.let{
                        val updatedItem = it.stream().filter{cafe->
                            cafe.cafenomad.id == id
                        }.findAny().orElse(null)

                        if(updatedItem != null){
                            updatedItem.isFavorite = true
                            cafeListAll.postValue(cafeListAll.value)
                        }
                    }

                    //cafeDisplay update
                    cafeListDisplay.value?.let{
                        val updatedItem = it.stream().filter{cafe->
                            cafe.cafenomad.id == id
                        }.findAny().orElse(null)

                        if(updatedItem != null){
                            updatedItem.isFavorite = true
                            cafeListDisplay.postValue(cafeListDisplay.value)
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
                    cafeListAll.value?.let{
                        val updatedItem = it.stream().filter{cafe->
                            cafe.cafenomad.id == id
                        }.findAny().orElse(null)

                        if(updatedItem != null){
                            updatedItem.isFavorite = false
                            cafeListAll.postValue(cafeListAll.value)
                        }
                    }

                    //cafeDisplay update
                    cafeListDisplay.value?.let{
                        val updatedItem = it.stream().filter{cafe->
                            cafe.cafenomad.id == id
                        }.findAny().orElse(null)

                        if(updatedItem != null){
                            updatedItem.isFavorite = false
                            cafeListDisplay.postValue(cafeListDisplay.value)
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