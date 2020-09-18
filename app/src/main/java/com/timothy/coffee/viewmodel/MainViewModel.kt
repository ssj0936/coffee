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
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.util.Movement
import com.timothy.coffee.util.Utils
import io.reactivex.Maybe
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

    //目前user所在
    var loc : MutableLiveData<LatLng> = MutableLiveData()
    //用來控制目前顯示的cafe頁面所binding的cafenomad data
    val chosenCafe: MutableLiveData<CafenomadDisplay> = MutableLiveData()
    //抓下來的所以cafenomad資料
    val cafeListAll:MutableLiveData<List<CafenomadDisplay>> = MutableLiveData()
    //經過排序與filter後要顯示出來的cafenomad資料
    val cafeListDisplay:MutableLiveData<List<CafenomadDisplay>> = MutableLiveData()
    //紀錄目前使用的顯示模式(sort/filter)
    val sortType:MutableLiveData<String> = MutableLiveData()

    val isReSearchable:MutableLiveData<Boolean> = MutableLiveData()

    var lastSortType:String? = null
    var lastMove = Movement(isClickMap = false, isClickList = false)
    var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)

    @SuppressLint("ResourceType")
    fun getCafeList(context: Context, isForce:Boolean):Observable<List<CafenomadDisplay>> {
        return Observable.just("")
            .flatMap {
                if (loc.value == null) {
                    getLocationObservable(context)
                }
                else{
                    Observable.just(loc.value)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMap{lonlat ->
//                Timber.d("first flatmap : ${Thread.currentThread().name} : ${Thread.currentThread().id}")
//                Timber.d("longitude:${lonlat.longitude},latitude:${lonlat.latitude}")

                if(lonlat != loc.value || isForce){
//                    Timber.d("different lon && lat:${lonlat.latitude},${lonlat.longitude}")
//                    Timber.d("loc:${loc.value}")
                    loc.postValue(lonlat)
                    getCafeListFromLocation(context,lonlat,false)
                }else{
//                    Timber.d("same lon && lat")
                    Observable.empty<List<CafenomadDisplay>>()
                }
            }
    }

    @SuppressLint("ResourceType")
    fun getCafeListFromLocation(context: Context, latLng: LatLng, isForce:Boolean):Observable<List<CafenomadDisplay>> {
        return Observable.just(latLng)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMap {lonlat->
                val range = context.resources.getInteger(R.dimen.range_cafe_nearby_max)
                dataSource.queryV2(lonlat.latitude,lonlat.longitude,range,isForce)
                    .observeOn(Schedulers.computation())
                    .map { cafes ->
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
                                cafe.cafenomad.distance < range*1000
                            }
                            //sort by distance from nearest to farest
                            .sorted{
                                    cafe1,cafe2->cafe1.cafenomad.distance.compareTo(cafe2.cafenomad.distance)
                            }
                            .collect(Collectors.toList())
                    }
            }
    }

    @SuppressLint("ResourceType")
    fun refetchCafeData(context: Context):Observable<List<CafenomadDisplay>>{
        loc.value?.let {
            return@refetchCafeData getCafeListFromLocation(context,it,true)
        }
        return Observable.empty<List<CafenomadDisplay>>()
    }

    private fun getLocationObservable(context: Context): Observable<LatLng> {
//        Timber.d("get Test Location")
        return dataModel.getLocationObservable(context)
    }

    @SuppressLint("ResourceType")
    fun setCafeViaSortType(type:String, context: Context){
        if(cafeListAll.value == null || type == lastSortType) return
        Timber.d("SORT TYPE:${type}")

        cafeListAll.value?.let {
            val sortedConditionalCafeList = getSortedCafeList(it,type,context)

            //cafeListDisplay
            cafeListDisplay.value = sortedConditionalCafeList

            //chosenCafe
            chosenCafe.postValue(
                if(sortedConditionalCafeList.isNotEmpty()) sortedConditionalCafeList.first()
                else null
            )
        }
        lastSortType = type
    }

    fun initialLocalCafeData(list: List<CafenomadDisplay>, context: Context){
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
//        val currentCafe = chosenCafe.value
//        if(currentCafe != null){
//            val newCafe = list.stream()
//                .filter { cafe -> cafe.cafenomad.id == currentCafe.cafenomad.id}
//                .findAny()
//                .orElse(null)
//
//            if(currentCafe != newCafe)
//                chosenCafe.postValue(newCafe)
//        }
//        else{
//            if(sortedConditionalCafeList.isNotEmpty())
//                chosenCafe.postValue(sortedConditionalCafeList.first())
//        }
        if(sortedConditionalCafeList.isNotEmpty())
            chosenCafe.postValue(sortedConditionalCafeList.first())
    }

    @SuppressLint("CheckResult")
    fun updateDisplayCafeData(context: Context){
        Single.just("")
            .observeOn(Schedulers.computation())
            .subscribe({
                isLoading.postValue(true)

                //update cafelist for display
                cafeListAll.value?.let {

                    //update cafelist for display
                    val sortedConditionalCafeList = getSortedCafeList(
                        it,
                        sortType.value ?: context.getString(R.string.filter_label_all),
                        context
                    )
                    cafeListDisplay.postValue(sortedConditionalCafeList)

                    //update chosenCafe
                    val currentCafe = chosenCafe.value
                    if(currentCafe != null){
                        val newCafe = it.stream()
                            .filter { cafe -> cafe.cafenomad.id == currentCafe.cafenomad.id}
                            .findAny()
                            .orElse(null)

                        if(currentCafe != newCafe)
                            chosenCafe.postValue(newCafe)
                    }
                    else{
                        if(sortedConditionalCafeList.isNotEmpty())
                            chosenCafe.postValue(sortedConditionalCafeList.first())
                    }
                }
                isLoading.postValue(false)
            },{error ->
                Timber.d("Update max cafe return number error: $error")
                isLoading.postValue(false)
            })
    }

    @SuppressLint("ResourceType")
    fun getSortedCafeList(list:List<CafenomadDisplay>, type:String, context: Context):List<CafenomadDisplay> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val maxCafeNumShowing = sharedPreferences.getString(
            context.getString(R.string.preference_key_max_cafe_return_number),
            context.resources.getStringArray(R.array.preference_cafe_number_option)[0])!!.toLong()

        return list.stream()
            .filter { cafe ->
                val range = context.resources.getInteger(R.dimen.range_cafe_nearby_min) * 1000

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
            }.limit(maxCafeNumShowing).collect(Collectors.toList())
    }

    @SuppressLint("CheckResult")
    fun setFavorite(cafeId:String, context: Context):Single<Long>{
        return dataSource.insertFavoriteV2(cafeId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnSuccess {inserID ->
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
    }

    @SuppressLint("CheckResult")
    fun deleteFavorite(cafeId:String, context: Context):Single<Int>{
        return dataSource.deleteFavoriteV2(cafeId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnSuccess {deleteCnt->
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
                    var currentIndex = 0
                    lateinit var newCafeListDisplay:List<CafenomadDisplay>

                    cafeListDisplay.value?.let {
                        val updatedItem = it.stream().filter { cafe ->
                            cafe.cafenomad.id == cafeId
                        }.findAny().orElse(null)
                        currentIndex = it.indexOf(updatedItem)

                        if (updatedItem != null) {
                            updatedItem.isFavorite = false

                            // 更改完內容後，依據目前的display type決定是否需要做刪減
                            if (lastSortType != null){
                                val sortedlist = getSortedCafeList(
                                    it,
                                    lastSortType!!,
                                    context
                                )

                                newCafeListDisplay = sortedlist
                                cafeListDisplay.postValue(sortedlist)
                            }
                            else {
                                newCafeListDisplay = it
                                cafeListDisplay.postValue(it)
                            }
                        }
                    }

                    //chosen Cafe update
                    if (cafeId == chosenCafe.value?.cafenomad?.id) {
                        chosenCafe.value?.let {
                            it.isFavorite = false

                            //if current chosenCafe is inappropriate to be shown on cafeListDisplay
                            //then set chosenCafe to cafeListDisplay[0]
                            if(newCafeListDisplay
                                    .stream()
                                    .filter { cafe -> cafe.cafenomad.id == it.cafenomad.id }
                                    .count() == 0L){

                                if(newCafeListDisplay.isNotEmpty()){
                                    if(currentIndex<0 || currentIndex >= newCafeListDisplay.size) {
                                        val t = newCafeListDisplay[0].copy()
                                        chosenCafe.postValue(t)
                                    }else {
                                        val t = newCafeListDisplay[currentIndex].copy()
                                        chosenCafe.postValue(t)
                                    }
                                }else{
                                    chosenCafe.postValue(null)

                                }
                            }else{
                                chosenCafe.postValue(it)
                            }
                        }
                    }
                }
            }
    }
}