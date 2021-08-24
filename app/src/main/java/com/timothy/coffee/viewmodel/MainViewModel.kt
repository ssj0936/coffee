package com.timothy.coffee.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng
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
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
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

    private lateinit var disposable:Disposable

    //目前user所在
    val screenCenterLoc : LiveData<LatLng>
        get() = _screenCenterLoc
    private val _screenCenterLoc = MutableLiveData<LatLng>()

    private var userLoc : LatLng? = null
    //用來控制目前顯示的cafe頁面所binding的cafenomad data
    val chosenCafe: MutableLiveData<CafenomadDisplay> = MutableLiveData()
    //抓下來的所以cafenomad資料
    private val cafeListAll:MutableLiveData<List<CafenomadDisplay>> = MutableLiveData()
    //經過排序與filter後要顯示出來的cafenomad資料
    val cafeListDisplay : MutableLiveData<List<CafenomadDisplay>> = MutableLiveData()

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

    fun getCafeList(
        isForceFromApi:Boolean = false,
        isFetchFavOnly:Boolean = false
    ):Single<List<CafenomadDisplay>> {
        return when(isFetchFavOnly) {
            true -> getCafeListFromFavorite()
            false -> getCafeList(isForceFromApi)
        }
    }

    fun getCafeList(
        isForceFromApi:Boolean = false
    ):Single<List<CafenomadDisplay>> {
        val context = cafeApplicationContext
        return getLocation()
            .subscribeOn(Schedulers.io())
            .flatMap { lonlat ->
                getCafeListFromLocation(context,lonlat,isForceFromApi)
            }
    }

    @SuppressLint("CheckResult")
    fun getCafeListFromFavorite():Single<List<CafenomadDisplay>> {
        return dataSource.queryFromDBV2AllFavorite()
            .observeOn(Schedulers.computation())
            .map { cafes ->
                //distance assignment
                cafes.onEach { cafe ->
                    cafe.cafenomad.distanceFromCurrentLoc =
                        if(userLoc != null)
                            Utils.distance(
                                userLoc!!.latitude,cafe.cafenomad.latitude.toDouble(),
                                userLoc!!.longitude,cafe.cafenomad.longitude.toDouble()
                            ).toInt()
                        else 0
                }
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
                        cafes.onEach {cafe->
                            //calculation of distance from cafe to current location
                            cafe.cafenomad.distanceFromCurrentLoc =
                                if(userLoc != null)
                                    Utils.distance(
                                        userLoc!!.latitude,cafe.cafenomad.latitude.toDouble(),
                                        userLoc!!.longitude,cafe.cafenomad.longitude.toDouble()
                                    ).toInt()
                                else 0
                        }.filter{cafe->
                            cafe.cafenomad.distanceFromCurrentLoc < range*1000
                        }
                        //sort by distance from nearest to farest
                        .sortedBy{it.cafenomad.distanceFromCurrentLoc}
                    }
            }
    }

    private fun getLocation(): Single<LatLng> {
        return if (userLoc == null) {
            dataModel.getLastKnownLocation(cafeApplicationContext)
                .doOnSuccess {
                    userLoc = it
                    if(_screenCenterLoc.value?.equals(it) != true)
                        _screenCenterLoc.postValue(it)

                }
        }
        else{
            Single.just(screenCenterLoc.value)
        }
    }


    //refetch button on map
    //refetch button on settings preference
    //mainFragment request cafe(first time fetch)
    fun initialLocalCafeData(list: List<CafenomadDisplay>){
        //update cafe list
        updateAllCafe(list)
        //update cafelist for display
        updateDisplayCafe(list)
    }

    private fun updateAllCafe(originalDataList:List<CafenomadDisplay>){
        if(cafeListAll.value == null || cafeListAll.value != originalDataList) {
            cafeListAll.postValue(originalDataList)
        }
    }

    /*
    get sort then setValue, and set Current choose Cafe
     */
    private fun updateDisplayCafe(originalDataList:List<CafenomadDisplay>){
        //get sort
        getSortedCafeList(
            originalDataList,
            if(isFavoriteOnly.value!!) FILTER_FAVORITE_ONLY else FILTER_ALL
        ).also { sortedConditionalCafeList ->
            cafeListDisplay.postValue(sortedConditionalCafeList)
        }.also { sortedConditionalCafeList ->
            //for favorite showing
            //若chosenCafe有賦值的狀況下，一併更新。以ID為基準在cafelist中找出該object
            //理論上cafelist是被綁在RX流程上已經被更新了，但ChosenCafe是只有在click的時候才會去更新
            val currentCafe = chosenCafe.value
            if (currentCafe != null) {
                val newChosenCafe = sortedConditionalCafeList.find {
                    it.cafenomad.id == currentCafe.cafenomad.id
                }

                //new displayCafeList doesn't contain chosenCafe -> assign to sortedConditionalCafeList.first
                if (newChosenCafe == null && sortedConditionalCafeList.isNotEmpty()) {
                    chosenCafe.postValue(sortedConditionalCafeList.first())
                }
                //new displayCafeList contains chosenCafe, but different value(ex. favorite) -> re-assign
                else if (currentCafe != newChosenCafe) {
                    chosenCafe.postValue(newChosenCafe)
                }
                else
                    chosenCafe.postValue(currentCafe)
            }
            //first time launch
            //chosenCafe == value
            else {
                chosenCafe.postValue(sortedConditionalCafeList.first())
            }
        }
    }

    //filter apply
    //max cafe display on settings preference
    @SuppressLint("CheckResult")
    fun onDisplayNumberChange(){
        isLoading.postValue(true)
        Single.just(null)
            .observeOn(Schedulers.computation())
            .subscribe({
                //update cafelist for display
                cafeListAll.value?.let {
                    updateDisplayCafe(it)
                }
                isLoading.postValue(false)
            },{error ->
                Timber.d("Update max cafe return number error: $error")
                isLoading.postValue(false)
            })
    }

    private fun getSortedCafeList(list:List<CafenomadDisplay>, type:Int):List<CafenomadDisplay> {
        val maxCafeNumShowing = PreferenceManager
            .getDefaultSharedPreferences(cafeApplicationContext)
            .getString(
                cafeApplicationContext.getString(R.string.preference_key_max_cafe_return_number),
                cafeApplicationContext.resources.getStringArray(R.array.preference_cafe_number_option)[0])!!
            .toInt()

        val isFullRange = isFavoriteOnly.value ?: true

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

        return list
            .asSequence()
            .sortedWith(
                compareBy<CafenomadDisplay>{it.cafenomad.distanceFromCurrentLoc}
                    .thenBy{it.cafenomad.tastyLevel*-1}
            )
            .filter { cafe ->
                if(isFullRange) true
                else {
                    val range = cafeApplicationContext.resources.getInteger(R.integer.range_cafe_nearby_min) * 1000
                    val isSetFavoriteOnly =
                        (type == FILTER_FAVORITE_ONLY)
                    (cafe.cafenomad.distanceFromCurrentLoc < range) && (if (isSetFavoriteOnly) cafe.isFavorite else true)
                }
            }
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
            .take(maxCafeNumShowing)
            .toList()
    }

    @SuppressLint("CheckResult")
    fun setFavorite(cafeId:String):Single<Long> =
        dataSource.insertFavoriteV2(cafeId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnSuccess {insertID ->
                if(insertID>0) {
                    //find target and set favorite to true then update
                    cafeListAll.value?.let { newCafeListAll->
                        newCafeListAll.find { cafe ->
                            cafe.cafenomad.id == cafeId
                        }?.also{updatedItem ->
                            updatedItem.isFavorite = true
                            cafeListAll.postValue(newCafeListAll)
                        }
                    }

                    //cafeDisplay update
                    cafeListDisplay.value?.let {
                        it.find { cafe ->
                            cafe.cafenomad.id == cafeId
                        }?.also{ updatedItem ->
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
                        it.find { cafe ->
                            cafe.cafenomad.id == cafeId
                        }?.also { updatedItem ->
                            updatedItem.isFavorite = false
                            cafeListAll.postValue(it)
                        }
                    }

                    //cafeDisplay update
                    var currentIndex = 0
                    lateinit var newCafeListDisplay:List<CafenomadDisplay>

                    cafeListDisplay.value?.let {
                        val updatedItem = it.find { cafe ->
                            cafe.cafenomad.id == cafeId
                        }?.also{ updatedItem ->
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

                        currentIndex = it.indexOf(updatedItem)
                    }

                    //chosen Cafe update
                    if (cafeId == chosenCafe.value?.cafenomad?.id) {
                        chosenCafe.value?.let {
                            it.isFavorite = false

                            val isTargetNotQualifiedToShow = newCafeListDisplay
                                .find { cafe -> cafe.cafenomad.id == it.cafenomad.id } == null
                            //if current chosenCafe is inappropriate to be shown on cafeListDisplay
                            //then set chosenCafe to cafeListDisplay[0]
                            if(isTargetNotQualifiedToShow){

                                if(newCafeListDisplay.isNotEmpty()){
                                    chosenCafe.postValue(
                                        //out of range
                                        if(currentIndex !in newCafeListDisplay.indices)
                                            newCafeListDisplay.first().copy()
                                        else
                                            newCafeListDisplay[currentIndex].copy()
                                    )
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

    fun updateScreenCenterLoc(latlng: LatLng){
        if(Looper.myLooper() == Looper.getMainLooper())
            _screenCenterLoc.value = latlng
        else
            _screenCenterLoc.postValue(latlng)
    }

    override fun onCleared() {
        super.onCleared()
        if(::disposable.isInitialized){
            disposable.dispose()
        }
    }
}