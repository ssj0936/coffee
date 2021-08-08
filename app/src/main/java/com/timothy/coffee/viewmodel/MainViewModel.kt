package com.timothy.coffee.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.timothy.coffee.CafeApp.Companion.cafeApplicationContext
import com.timothy.coffee.R
import com.timothy.coffee.data.DataModel
import com.timothy.coffee.data.DataSource
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.util.FILTER_NO_TIME_LIMIT
import com.timothy.coffee.util.Movement
import com.timothy.coffee.util.*
import com.timothy.coffee.util.Utils.Companion.getFilterSetting
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper.dispose
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import java.util.stream.Collectors
import javax.inject.Inject
import kotlin.math.floor

const val FILTER_FAVORITE_ONLY = 0
const val FILTER_ALL = 1
const val FILTER_DISTANCE_FARTHEST = 2
const val FILTER_STAR = 3

class MainViewModel @Inject constructor(
    private val dataModel:DataModel,
    private val dataSource: DataSource
): ViewModel(){

    lateinit var disposable:Disposable

    //目前user所在
    var screenCenterLoc : MutableLiveData<LatLng> = MutableLiveData()
    private var userLoc : LatLng? = null
    //用來控制目前顯示的cafe頁面所binding的cafenomad data
    val chosenCafe: MutableLiveData<CafenomadDisplay> = MutableLiveData()
    //抓下來的所以cafenomad資料
    val cafeListAll:MutableLiveData<List<CafenomadDisplay>> = MutableLiveData()
    //經過排序與filter後要顯示出來的cafenomad資料
    val cafeListDisplay:MutableLiveData<List<CafenomadDisplay>> = MutableLiveData()
    //紀錄目前使用的顯示模式(sort/filter)
//    val sortType:MutableLiveData<String> = MutableLiveData()

    var isFavoriteOnly:MutableLiveData<Boolean> = MutableLiveData(false)
    val isReSearchable:MutableLiveData<Boolean> = MutableLiveData()

    var lastSortType:Int? = null
    var lastMove = Movement(isClickMap = false, isClickList = false)
    var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)

    fun onMainFragmentReady(){
        //starting fetching data
        disposable = getCafeList()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                initialLocalCafeData(it)
            },{error-> Timber.e(error)})
    }

    fun getCafeList(isForceFromApi:Boolean = false, isFetchFavOnly:Boolean = false):Single<List<CafenomadDisplay>> {
        return when(isFetchFavOnly) {
            true -> getCafeListFromFavorite()
            false -> getCafeList(isForceFromApi)
        }
    }
    @SuppressLint("CheckResult")
    fun getCafeListFromFavorite():Single<List<CafenomadDisplay>> {
        return dataSource.queryFromDBV2AllFavorite()
            .observeOn(Schedulers.computation())
            .map { cafes ->
                //distance assignment
                cafes.stream().forEach { cafe ->
                    screenCenterLoc.value?.let {
                        cafe.cafenomad.distanceFromCenterOfScreen = Utils.distance(
                            it.latitude, cafe.cafenomad.latitude.toDouble(),
                            it.longitude, cafe.cafenomad.longitude.toDouble()
                        ).toInt()

                        Timber.d("$cafe")
                    }
                }
                cafes
            }
    }

    fun getCafeList(isForceFromApi:Boolean = false):Single<List<CafenomadDisplay>> {
        val context = cafeApplicationContext
        return Single.just("")
            .subscribeOn(Schedulers.io())
            //get current location
            .flatMap {
                if (userLoc == null) {
                    getLocationObservable(context)
                        .doOnSuccess {
                            userLoc = it
                        }
                }
                else{
                    Single.just(screenCenterLoc.value)
                }
            }
            //get cafeList in currentLocation
            .flatMap{lonlat ->
                Timber.d("first flatmap : ${Thread.currentThread().name} : ${Thread.currentThread().id}")
                Timber.d("longitude:${lonlat.longitude},latitude:${lonlat.latitude}")

                screenCenterLoc.postValue(lonlat)
                getCafeListFromLocation(context,lonlat,isForceFromApi)
            }
    }

    fun getCafeListFromLocation(context: Context, latLng: LatLng, isForceFromApi:Boolean):Single<List<CafenomadDisplay>> {
        return Single.just(latLng)
            .subscribeOn(Schedulers.io())
            .flatMap {lonlat->
                val range = context.resources.getInteger(R.integer.range_cafe_nearby_max)
                dataSource.queryV2(lonlat.latitude,lonlat.longitude,range,isForceFromApi)
                    .observeOn(Schedulers.computation())
                    .map { cafes ->
                        //distance assignment
                        cafes.stream().forEach {cafe->
                            //calculation of distance from cafe to screen center
                            screenCenterLoc.value?.let{
                                cafe.cafenomad.distanceFromCenterOfScreen =
                                    Utils.distance(
                                        it.latitude,cafe.cafenomad.latitude.toDouble(),
                                        it.longitude,cafe.cafenomad.longitude.toDouble()
                                    ).toInt()
                            }

                            //calculation of distance from cafe to current location
                            cafe.cafenomad.distanceFromCurrentLoc =
                                if(userLoc != null)
                                    Utils.distance(
                                        userLoc!!.latitude,cafe.cafenomad.latitude.toDouble(),
                                        userLoc!!.longitude,cafe.cafenomad.longitude.toDouble()
                                    ).toInt()
                                else 0
                        }

                        cafes.stream()
                            //double filtering cafe out of range
                            .filter{cafe->
                                cafe.cafenomad.distanceFromCenterOfScreen < range*1000
                            }
                            //sort by distance from nearest to farest
                            .sorted{
                                    cafe1,cafe2->cafe1.cafenomad.distanceFromCenterOfScreen.compareTo(cafe2.cafenomad.distanceFromCenterOfScreen)
                            }
                            .collect(Collectors.toList())
                    }
            }
    }

    private fun getLocationObservable(context: Context): Single<LatLng> {
        return dataModel.getLastKnownLocation(context)
    }

    //refetch button on map
    //refetch button on settings preference
    //mainFragment request cafe(first time fetch)
    fun initialLocalCafeData(list: List<CafenomadDisplay>){
        //update cafe list
        if(cafeListAll.value == null || cafeListAll.value != list) {
            cafeListAll.postValue(list)
        }

        //update cafelist for display
        val sortedConditionalCafeList = getSortedCafeList(
            list,
            if(isFavoriteOnly.value!!) FILTER_FAVORITE_ONLY else FILTER_ALL
        )
        cafeListDisplay.postValue(sortedConditionalCafeList)

        //for favorite showing
        //若chosenCafe有賦值的狀況下，一併更新。以ID為基準在cafelist中找出該object
        //理論上cafelist是被綁在RX流程上已經被更新了，但ChosenCafe是只有在click的時候才會去更新
        val currentCafe = chosenCafe.value
        if(currentCafe != null){
            val newCafe = sortedConditionalCafeList.stream()
                .filter { cafe -> cafe.cafenomad.id == currentCafe.cafenomad.id}
                .findAny()
                .orElse(null)

            //new displayCafeList doesn't contain chosenCafe -> assign to sortedConditionalCafeList.first
            if(newCafe == null && sortedConditionalCafeList.isNotEmpty()){
                chosenCafe.postValue(sortedConditionalCafeList.first())
            }
            //new displayCafeList contains chosenCafe, but different value(ex. favorite) -> re-assign
            else if(currentCafe != newCafe) {
                chosenCafe.postValue(newCafe)
            }
            else {
                chosenCafe.postValue(currentCafe)
            }
        }
        //first time launch
        //chosenCafe == value
        else{
            chosenCafe.postValue(
                if(sortedConditionalCafeList.isNotEmpty()) sortedConditionalCafeList.first()
                else null
            )
        }
    }

    //filter apply
    //max cafe display on settings preference
    @SuppressLint("CheckResult")
    fun updateDisplayCafeData(){
        Single.just("")
            .observeOn(Schedulers.computation())
            .subscribe({
                isLoading.postValue(true)

                //update cafelist for display
                cafeListAll.value?.let {

                    //update cafelist for display
                    val sortedConditionalCafeList = getSortedCafeList(
                        it,
                        if(isFavoriteOnly.value!!) FILTER_FAVORITE_ONLY else FILTER_ALL
                    )
                    cafeListDisplay.postValue(sortedConditionalCafeList)

                    //update chosenCafe
                    val currentCafe = chosenCafe.value
                    if(currentCafe != null){
                        val newCafe = it.stream()
                            .filter { cafe -> cafe.cafenomad.id == currentCafe.cafenomad.id}
                            .findAny()
                            .orElse(null)

                        //new displayCafeList doesn't contain chosenCafe -> assign to sortedConditionalCafeList.first
                        if(newCafe == null && sortedConditionalCafeList.isNotEmpty()){
                            chosenCafe.postValue(sortedConditionalCafeList.first())
                        }
                        //new displayCafeList contains chosenCafe, but different value(ex. favorite) -> re-assign
                        else if(currentCafe != newCafe) {
                            chosenCafe.postValue(newCafe)
                        }
                        else {
                            chosenCafe.postValue(currentCafe)
                        }

                    }
                    else{
                        chosenCafe.postValue(
                            if(sortedConditionalCafeList.isNotEmpty()) sortedConditionalCafeList.first()
                            else null
                        )
                    }
                }
                isLoading.postValue(false)
            },{error ->
                Timber.d("Update max cafe return number error: $error")
                isLoading.postValue(false)
            })
    }

    private fun getSortedCafeList(list:List<CafenomadDisplay>, type:Int):List<CafenomadDisplay> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(cafeApplicationContext)
        val maxCafeNumShowing = sharedPreferences.getString(
            cafeApplicationContext.getString(R.string.preference_key_max_cafe_return_number),
            cafeApplicationContext.resources.getStringArray(R.array.preference_cafe_number_option)[0])!!.toLong()

        val isFullRange = isFavoriteOnly.value!!

        val filter = getFilterSetting(cafeApplicationContext)
        val isFilterNoTimeLimit = (1 and (filter shr FILTER_NO_TIME_LIMIT)) == 1
        val isFilterSocket = (1 and (filter shr FILTER_SOCKET)) == 1
        val isFilterStandingDesk = (1 and (filter shr FILTER_STANDING_DESK)) == 1

        val isNoStar = (1 and (filter shr FILTER_TASTY_RATE_0)) == 1
        val isOneStar = (1 and (filter shr FILTER_TASTY_RATE_1)) == 1
        val isTwoStar = (1 and (filter shr FILTER_TASTY_RATE_2)) == 1
        val isThreeStar = (1 and (filter shr FILTER_TASTY_RATE_3)) == 1
        val isFourStar = (1 and (filter shr FILTER_TASTY_RATE_4)) == 1
        val isFiveStar = (1 and (filter shr FILTER_TASTY_RATE_5)) == 1

        return list.stream()
            .filter { cafe ->
                if(isFullRange) true
                else {
                    val range = cafeApplicationContext.resources.getInteger(R.integer.range_cafe_nearby_min) * 1000
                    val isSetFavoriteOnly =
                        (type == FILTER_FAVORITE_ONLY)
                    (cafe.cafenomad.distanceFromCenterOfScreen < range) && (if (isSetFavoriteOnly) cafe.isFavorite else true)
                }
            }.sorted{cafe1,cafe2 ->
                when(type){
                    FILTER_DISTANCE_FARTHEST ->{
                        if(cafe1.cafenomad.distanceFromCenterOfScreen.compareTo(cafe2.cafenomad.distanceFromCenterOfScreen) != 0)
                            cafe1.cafenomad.distanceFromCenterOfScreen.compareTo(cafe2.cafenomad.distanceFromCenterOfScreen)*-1
                        else
                            cafe1.cafenomad.tastyLevel.compareTo(cafe2.cafenomad.tastyLevel)*-1
                    }
                    FILTER_STAR ->{
                        if(cafe1.cafenomad.tastyLevel.compareTo(cafe2.cafenomad.tastyLevel) != 0)
                            cafe1.cafenomad.tastyLevel.compareTo(cafe2.cafenomad.tastyLevel)*-1
                        else
                            cafe1.cafenomad.distanceFromCenterOfScreen.compareTo(cafe2.cafenomad.distanceFromCenterOfScreen)
                    }
                    else -> {
                        if(cafe1.cafenomad.distanceFromCenterOfScreen.compareTo(cafe2.cafenomad.distanceFromCenterOfScreen) !=0)
                            cafe1.cafenomad.distanceFromCenterOfScreen.compareTo(cafe2.cafenomad.distanceFromCenterOfScreen)
                        else
                            cafe1.cafenomad.tastyLevel.compareTo(cafe2.cafenomad.tastyLevel)*-1
                    }
                }
            }
            .limit(maxCafeNumShowing)
            .filter {
                //filter都沒勾選 -> 無限制 全部顯示
                if(!isFilterNoTimeLimit && !isFilterSocket && !isFilterStandingDesk) true
                //
                else {
                    (if(isFilterNoTimeLimit) it.cafenomad.isTimeLimited == cafeApplicationContext.getString(R.string.info_value_no) else true)
                            && (if(isFilterSocket) it.cafenomad.isSocketProvided == cafeApplicationContext.getString(R.string.info_value_yes) else true)
                            && (if(isFilterStandingDesk) it.cafenomad.isStandingDeskAvailable == cafeApplicationContext.getString(R.string.info_value_yes) else true)
                }
            }
            .filter {
                if(!isNoStar && !isOneStar && !isTwoStar && !isThreeStar && !isFourStar && !isFiveStar) true
                else{
                    ((isNoStar) && floor(it.cafenomad.tastyLevel) == 0.0)
                            || ((isOneStar) && floor(it.cafenomad.tastyLevel) == 1.0 )
                            || ((isTwoStar) && floor(it.cafenomad.tastyLevel) == 2.0 )
                            || ((isThreeStar) && floor(it.cafenomad.tastyLevel) == 3.0 )
                            || ((isFourStar) && floor(it.cafenomad.tastyLevel) == 4.0 )
                            || ((isFiveStar) && floor(it.cafenomad.tastyLevel) == 5.0 )
                }
            }
            .collect(Collectors.toList())
    }

    @SuppressLint("CheckResult")
    fun setFavorite(cafeId:String):Single<Long> =
        dataSource.insertFavoriteV2(cafeId)
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
                                        lastSortType!!
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


    @SuppressLint("CheckResult")
    fun deleteFavorite(cafeId:String):Single<Int> =
        dataSource.deleteFavoriteV2(cafeId)
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
                                    lastSortType!!
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


    override fun onCleared() {
        super.onCleared()
        if(::disposable.isInitialized){
            disposable.dispose()
        }
    }

}