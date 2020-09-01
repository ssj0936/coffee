package com.timothy.coffee.viewmodel

import android.annotation.SuppressLint
import android.app.Application
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

    val sortType:MutableLiveData<String> = MutableLiveData()
    var lastSortType:String? = null
    var lastMove = Movement(isClickMap = false, isClickList = false)
    var isDataFetching = false

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

                //distance assignment
                cafes.stream().forEach {cafe->
                    loc.value?.let{
                        cafe.cafenomad.distance = Utils.distance(it.latitude,cafe.cafenomad.latitude.toDouble(),
                            it.longitude,cafe.cafenomad.longitude.toDouble()).toInt()
                    }
                }

                cafes.stream()
                    //double filtering cafe out of range
                    .filter{cafe->
                        val range = context.resources.getInteger(R.dimen.range_cafe_nearby_max)*1000
                        cafe.cafenomad.distance < range
                    }
                    //sort by distance from nearest to farest
                    .sorted{
                        cafe1,cafe2->cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance)
                    }
                    .collect(Collectors.toList())
            }
    }

    @SuppressLint("ResourceType")
    fun refetchCafeData(context: Context):Observable<List<CafenomadDisplay>>{
        val range = context.resources.getInteger(R.dimen.range_cafe_nearby_max)
        if(loc.value != null) {
            return dataSource.queryV2(loc.value!!.latitude, loc.value!!.longitude, range, true)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map{cafes ->
                    //distance assignment
                    cafes.stream().forEach {cafe->
                        loc.value?.let{
                            cafe.cafenomad.distance = Utils.distance(it.latitude,cafe.cafenomad.latitude.toDouble(),
                                it.longitude,cafe.cafenomad.longitude.toDouble()).toInt()
                        }
                    }

                    cafes.stream()
                        //double filtering cafe out of range
                        .filter{cafe->
                            val range = context.resources.getInteger(R.dimen.range_cafe_nearby_max)*1000
                            cafe.cafenomad.distance < range
                        }
                        //sort by distance from nearest to farest
                        .sorted{
                                cafe1,cafe2->cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance)
                        }
                        .collect(Collectors.toList())
                }
        }else{
            return Observable.empty<List<CafenomadDisplay>>()
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
    fun setCafeViaSortType(type:String, context: Context){
        if(cafeListAll.value == null || type == lastSortType) return
        Timber.d("SORT TYPE:${type}")

        cafeListDisplay.value = cafeListAll.value?.let {
            getSortedCafeList(it,type,context)
        }
        lastSortType = type
    }

    fun updateLocalCafeData(list: List<CafenomadDisplay>, context: Context){
        //update cafe list
        if(cafeListAll.value == null || cafeListAll.value != list) {
            cafeListAll.postValue(list)
        }

        //update cafelist for display
        val sortedConditionalCafeList = getSortedCafeList(
            list,
            sortType.value ?: context.getString(R.string.filter_label_all),
            context
        )
        cafeListDisplay.postValue(sortedConditionalCafeList)

        //for favorite showing
        //若chosenCafe有賦值的狀況下，一併更新。以ID為基準在cafelist中找出該object
        //理論上cafelist是被綁在RX流程上已經被更新了，但ChosenCafe是只有在click的時候才會去更新
        val currentCafe = chosenCafe.value
        if(currentCafe != null){
            val newCafe = list.let { cafelist ->
                cafelist.stream()
                    .filter { cafe -> cafe.cafenomad.id == currentCafe.cafenomad.id}
                    .findAny()
                    .orElse(null)
            }

            if(currentCafe != newCafe)
                chosenCafe.postValue(newCafe)
        }
        else{
            if(sortedConditionalCafeList.isNotEmpty())
                chosenCafe.postValue(sortedConditionalCafeList.first())
        }
    }

    @SuppressLint("ResourceType")
    fun getSortedCafeList(list:List<CafenomadDisplay>, type:String, context: Context):List<CafenomadDisplay> {
        return list.stream()
            .filter { cafe ->
                val range = PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt(
                        context.getString(R.string.preference_key_search_range)
                        , context.resources.getInteger(R.dimen.range_cafe_nearby_min)
                    ) * 1000

                val isSetFavoriteOnly = (type == context.resources.getString(R.string.filter_label_favorite_only))
                (cafe.cafenomad.distance < range) && (if(isSetFavoriteOnly) cafe.isFavorite else true)
            }.sorted{cafe1,cafe2 ->
                when(type){
                    context.resources.getString(R.string.filter_label_distance_farthest) ->{
                        if(cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance) != 0)
                            cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance)*-1
                        else
                            cafe1.cafenomad.tastyLevel.compareTo(cafe2.cafenomad.tastyLevel)*-1
                    }
                    context.resources.getString(R.string.filter_label_star) ->{
                        if(cafe1.cafenomad.tastyLevel.compareTo(cafe2.cafenomad.tastyLevel) != 0)
                            cafe1.cafenomad.tastyLevel.compareTo(cafe2.cafenomad.tastyLevel)*-1
                        else
                            cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance)
                    }
                    else -> {
                        if(cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance) !=0)
                            cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance)
                        else
                            cafe1.cafenomad.tastyLevel.compareTo(cafe2.cafenomad.tastyLevel)*-1
                    }
                }
            }.limit(10).collect(Collectors.toList())
    }

    @SuppressLint("CheckResult")
    fun setFavorite(cafeId:String, context: Context):Single<Long>{
        return dataSource.insertFavoriteV2(cafeId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnSuccess {inserID ->
                Timber.d("setFavorite")
                if(inserID>0) {
                    cafeListAll.value?.let {
                        val updatedItem = it.stream().filter { cafe ->
                            cafe.cafenomad.id == cafeId
                        }.findAny().orElse(null)

                        if (updatedItem != null) {
                            updatedItem.isFavorite = true
                            cafeListAll.postValue(it)
                        }
                    }

                    //cafeDisplay update
                    cafeListDisplay.value?.let {
                        val updatedItem = it.stream().filter { cafe ->
                            cafe.cafenomad.id == cafeId
                        }.findAny().orElse(null)

                        if (updatedItem != null) {
                            updatedItem.isFavorite = true

                            // 更改完內容後，依據目前的display type決定是否需要做刪減
                            if (lastSortType != null)
                                cafeListDisplay.postValue(
                                    getSortedCafeList(
                                        it,
                                        lastSortType!!,
                                        context
                                    )
                                )
                            else
                                cafeListDisplay.postValue(it)
                        }
                    }

                    //chosen Cafe update
                    if (cafeId == chosenCafe.value?.cafenomad?.id) {
                        chosenCafe.value?.let {
                            it.isFavorite = true
                            chosenCafe.postValue(it)
                        }
                    }
                }
            }


//        Single.just(cafeId)
//            .observeOn(Schedulers.io())
//            .subscribe { id ->
//                if(dataSource.insertFavorite(id) > 0){
//                    //cafeList update
//                    cafeListAll.value?.let{
//                        val updatedItem = it.stream().filter{cafe->
//                            cafe.cafenomad.id == id
//                        }.findAny().orElse(null)
//
//                        if(updatedItem != null){
//                            updatedItem.isFavorite = true
//                            cafeListAll.postValue(it)
//                        }
//                    }
//
//                    //cafeDisplay update
//                    cafeListDisplay.value?.let{
//                        val updatedItem = it.stream().filter{cafe->
//                            cafe.cafenomad.id == id
//                        }.findAny().orElse(null)
//
//                        if(updatedItem != null){
//                            updatedItem.isFavorite = true
//
//                            // 更改完內容後，依據目前的display type決定是否需要做刪減
//                            if(lastSortType!=null)
//                                cafeListDisplay.postValue(getSortedCafeList(it,lastSortType!!,context))
//                            else
//                                cafeListDisplay.postValue(it)
//                        }
//                    }
//
//                    //chosen Cafe update
//                    if(id == chosenCafe.value?.cafenomad?.id){
//                        chosenCafe.value?.let {
//                            it.isFavorite = true
//                            chosenCafe.postValue(it)
//                        }
//                    }
//                }
//            }
    }

    @SuppressLint("CheckResult")
    fun deleteFavorite(cafeId:String, context: Context):Single<Int>{
        return dataSource.deleteFavoriteV2(cafeId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnSuccess {deleteCnt->
                Timber.d("deleteFavorite")
                if(deleteCnt>0) {
                    //cafeList update
                    cafeListAll.value?.let {
                        val updatedItem = it.stream().filter { cafe ->
                            cafe.cafenomad.id == cafeId
                        }.findAny().orElse(null)

                        if (updatedItem != null) {
                            updatedItem.isFavorite = false
                            cafeListAll.postValue(it)
                        }
                    }

                    //cafeDisplay update
                    cafeListDisplay.value?.let {
                        val updatedItem = it.stream().filter { cafe ->
                            cafe.cafenomad.id == cafeId
                        }.findAny().orElse(null)

                        if (updatedItem != null) {
                            updatedItem.isFavorite = false

                            // 更改完內容後，依據目前的display type決定是否需要做刪減
                            if (lastSortType != null)
                                cafeListDisplay.postValue(
                                    getSortedCafeList(
                                        it,
                                        lastSortType!!,
                                        context
                                    )
                                )
                            else
                                cafeListDisplay.postValue(it)
                        }
                    }

                    //chosen Cafe update
                    if (cafeId == chosenCafe.value?.cafenomad?.id) {
                        chosenCafe.value?.let {
                            it.isFavorite = false
                            chosenCafe.postValue(it)
                        }
                    }
                }
            }

//        Single.just(cafeId)
//            .observeOn(Schedulers.io())
//            .subscribe { id ->
//                if(dataSource.deleteFavorite(id)>0){
//                    //cafeList update
//                    cafeListAll.value?.let{
//                        val updatedItem = it.stream().filter{cafe->
//                            cafe.cafenomad.id == id
//                        }.findAny().orElse(null)
//
//                        if(updatedItem != null){
//                            updatedItem.isFavorite = false
//                            cafeListAll.postValue(it)
//                        }
//                    }
//
//                    //cafeDisplay update
//                    cafeListDisplay.value?.let{
//                        val updatedItem = it.stream().filter{cafe->
//                            cafe.cafenomad.id == id
//                        }.findAny().orElse(null)
//
//                        if(updatedItem != null){
//                            updatedItem.isFavorite = false
//
//                            // 更改完內容後，依據目前的display type決定是否需要做刪減
//                            if(lastSortType!=null)
//                                cafeListDisplay.postValue(getSortedCafeList(it,lastSortType!!,context))
//                            else
//                                cafeListDisplay.postValue(it)
//                        }
//                    }
//
//                    //chosen Cafe update
//                    if(id == chosenCafe.value?.cafenomad?.id){
//                        chosenCafe.value?.let {
//                            it.isFavorite = false
//                            chosenCafe.postValue(it)
//                        }
//                    }
//                }
//            }
    }
}