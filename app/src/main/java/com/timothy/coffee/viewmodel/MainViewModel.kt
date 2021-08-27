package com.timothy.coffee.viewmodel

import android.annotation.SuppressLint
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng
import com.timothy.coffee.CafeApp.Companion.cafeApplicationContext
import com.timothy.coffee.R
import com.timothy.coffee.data.LocationDataSource
import com.timothy.coffee.data.CafeDataSource
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.util.FILTER_NO_TIME_LIMIT
import com.timothy.coffee.util.*
import com.timothy.coffee.util.Constants.RANGE_CAFE_NEARBY_MAX
import com.timothy.coffee.util.Constants.RANGE_CAFE_NEARBY_MIN
import com.timothy.coffee.util.Utils.Companion.getFilterSetting
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.floor

const val FILTER_FAVORITE_ONLY = 0
const val FILTER_ALL = 1
const val FILTER_DISTANCE_FARTHEST = 2
const val FILTER_STAR = 3

class MainViewModel @Inject constructor(
    private val locationDataSource:LocationDataSource,
    private val cafeDataSource: CafeDataSource
): ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    //地圖中心所在(搜尋非附近cafe用)
    val screenCenterLoc: LiveData<LatLng>
        get() = _screenCenterLoc
    private val _screenCenterLoc = MutableLiveData<LatLng>()

    //目前user所在
    private var userLoc: LatLng? = null
    //抓下來的所以cafenomad資料
    private var cafeListAll: List<CafenomadDisplay> = mutableListOf()

    //用來控制目前顯示的cafe頁面所binding的cafenomad data
    val chosenCafe: MutableLiveData<CafenomadDisplay> = MutableLiveData()


    //經過排序與filter後要顯示出來的cafenomad資料
    val cafeListDisplay: MutableLiveData<List<CafenomadDisplay>> = MutableLiveData()

    var isFavoriteOnly: MutableLiveData<Boolean> = MutableLiveData(false)
    val isReSearchable: MutableLiveData<Boolean> = MutableLiveData()

    var isLoading: MutableLiveData<Boolean> = MutableLiveData(false)

    //get location -> get cafe list on that location -> postProcedure(sort, livedata setting)
    fun onMainFragmentReady() {
        //starting fetching data
        getLocation()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .flatMap { latlng ->
                getCafeListFromLocation(latlng)
            }
            .subscribe({
                initialLocalCafeData(it)
            }, { error -> Timber.e(error) })
            .let {
                compositeDisposable.add(it)
            }
    }

    fun onFilterChanged() {
        isLoading.value = true

        getCafeList(isFetchFavOnly = isFavoriteOnly.value!!)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .subscribe ({
                initialLocalCafeData(it)
                isLoading.postValue(false)
            },{error->
                Timber.e("Filter Change error: $error")
                isLoading.postValue(false)
            }).let {
                compositeDisposable.add(it)
            }
    }

    fun onMapResearch() {
        isReSearchable.value = false
        isLoading.value = true

        screenCenterLoc.value?.let { latlon ->
            getCafeListFromLocation(latlon, false)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe({
                    //update cafe list
                    initialLocalCafeData(it)
                    isLoading.postValue(false)
                }, { error ->
                    Timber.e("ReFetch data error: $error")
                    isLoading.postValue(false)
                }).let {
                    compositeDisposable.add(it)
                }
        }
    }

    //filter apply
    //max cafe display on settings preference
    fun onDisplayNumberChange() {
        isLoading.postValue(true)
        Single.just("")
            .observeOn(Schedulers.computation())
            .subscribe({
                //update cafelist for display
                updateDisplayCafe(cafeListAll)
                isLoading.postValue(false)
            }, { error ->
                Timber.d("Update max cafe return number error: $error")
                isLoading.postValue(false)
            }).let {
                compositeDisposable.add(it)
            }
    }

    fun onRefetchData(refetchCallback: RefetchCallback){
        if(isLoading.value == true) return

        isLoading.value = true
        isFavoriteOnly.value = false

        getCafeList(isForceFromApi = true)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .subscribe({
                refetchCallback.onRefetchSuccess()
                //update cafe list
                initialLocalCafeData(it)
                isLoading.postValue(false)
            },{error->
                refetchCallback.onRefetchFail()
                Timber.e("ReFetch data error: $error")
                isLoading.postValue(false)
            }).let {
                compositeDisposable.add(it)
            }
    }

    private fun getCafeList(
        isForceFromApi: Boolean = false,
        isFetchFavOnly: Boolean = false
    ): Single<List<CafenomadDisplay>> {
        return when (isFetchFavOnly) {
            true -> getCafeListFromFavorite()
            false -> getCafeList(isForceFromApi)
        }
    }

    private fun getCafeList(
        isForceFromApi: Boolean = false
    ): Single<List<CafenomadDisplay>> {
        return getLocation()
            .subscribeOn(Schedulers.io())
            .flatMap { lonlat ->
                getCafeListFromLocation(lonlat, isForceFromApi)
            }
    }

    fun getCafeListFromFavorite(): Single<List<CafenomadDisplay>> {
        return cafeDataSource.queryFromDBV2AllFavorite()
            .observeOn(Schedulers.computation())
            .map { cafes ->
                //distance assignment
                cafes.onEach { cafe ->
                    cafe.cafenomad.distanceFromCurrentLoc =
                        if (userLoc != null)
                            Utils.distance(
                                userLoc!!.latitude, cafe.cafenomad.latitude.toDouble(),
                                userLoc!!.longitude, cafe.cafenomad.longitude.toDouble()
                            ).toInt()
                        else 0
                }
            }
    }

    private fun getCafeListFromLocation(
        latLng: LatLng,
        isForceFromApi: Boolean = false
    ): Single<List<CafenomadDisplay>> {
        return Single.just(latLng)
            .subscribeOn(Schedulers.io())
            .flatMap { lonlat ->
                val range = RANGE_CAFE_NEARBY_MAX
                cafeDataSource.queryV2(lonlat.latitude, lonlat.longitude, range, isForceFromApi)
                    .observeOn(Schedulers.computation())
                    .map { cafes ->
                        //distance assignment
                        cafes.onEach { cafe ->
                            //calculation of distance from cafe to screen center
                            cafe.cafenomad.distanceFromCenterOfScreen =
                                screenCenterLoc.value?.let {
                                    Utils.distance(
                                        it.latitude, cafe.cafenomad.latitude.toDouble(),
                                        it.longitude, cafe.cafenomad.longitude.toDouble()
                                    ).toInt()
                                } ?: Int.MAX_VALUE

                            //calculation of distance from cafe to current location
                            cafe.cafenomad.distanceFromCurrentLoc =
                                userLoc?.let {
                                    Utils.distance(
                                        it.latitude, cafe.cafenomad.latitude.toDouble(),
                                        it.longitude, cafe.cafenomad.longitude.toDouble()
                                    ).toInt()
                                } ?: 0
                        }
                    }
            }
    }

    private fun getLocation(): Single<LatLng> {
        return if (userLoc == null) {
            locationDataSource.getLastKnownLocation(cafeApplicationContext)
                .doOnSuccess {
                    userLoc = it
                    if (_screenCenterLoc.value?.equals(it) != true)
                        _screenCenterLoc.postValue(it)

                }
        } else {
            Single.just(screenCenterLoc.value)
        }
    }


    //refetch button on map
    //refetch button on settings preference
    //mainFragment request cafe(first time fetch)
    private fun initialLocalCafeData(list: List<CafenomadDisplay>) {
        //update cafe list
        updateAllCafe(list)
        //update cafelist for display
        updateDisplayCafe(list)
    }

    private fun updateAllCafe(originalDataList: List<CafenomadDisplay>) {
        cafeListAll = originalDataList
    }

    /*
    get sort then setValue, and set Current choose Cafe
     */
    private fun updateDisplayCafe(originalDataList: List<CafenomadDisplay>) {
        //get sort
        getSortedCafeList(
            originalDataList,
            if (isFavoriteOnly.value!!) FILTER_FAVORITE_ONLY else FILTER_ALL
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
                } else
                    chosenCafe.postValue(currentCafe)
            }
            //first time launch
            //chosenCafe == value
            else {
                if(sortedConditionalCafeList.isNotEmpty()){
                    chosenCafe.postValue(sortedConditionalCafeList.first())
                }
            }
        }
    }

    private fun getSortedCafeList(list: List<CafenomadDisplay>, type: Int): List<CafenomadDisplay> {
        val maxCafeNumShowing = PreferenceManager
            .getDefaultSharedPreferences(cafeApplicationContext)
            .getString(
                cafeApplicationContext.getString(R.string.preference_key_max_cafe_return_number),
                cafeApplicationContext.resources.getStringArray(R.array.preference_cafe_number_option)[0]
            )!!
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
            .filter { cafe ->
                if (isFullRange) true
                else {
                    val range = RANGE_CAFE_NEARBY_MIN * 1000
                    val isSetFavoriteOnly = (type == FILTER_FAVORITE_ONLY)
                    (cafe.cafenomad.distanceFromCenterOfScreen < range) && (if (isSetFavoriteOnly) cafe.isFavorite else true)
                }
            }
            .filter {
                //filter都沒勾選 -> 無限制 全部顯示
                if (!isFilterNoTimeLimit && !isFilterSocket && !isFilterStandingDesk) true
                //
                else {
                    (if (isFilterNoTimeLimit) it.cafenomad.isTimeLimited == cafeApplicationContext.getString(
                        R.string.info_value_no
                    ) else true)
                            && (if (isFilterSocket) it.cafenomad.isSocketProvided == cafeApplicationContext.getString(
                        R.string.info_value_yes
                    ) else true)
                            && (if (isFilterStandingDesk) it.cafenomad.isStandingDeskAvailable == cafeApplicationContext.getString(
                        R.string.info_value_yes
                    ) else true)
                }
            }
            .filter {
                if (!isNoStar && !isOneStar && !isTwoStar && !isThreeStar && !isFourStar && !isFiveStar) true
                else {
                    ((isNoStar) && floor(it.cafenomad.tastyLevel) == 0.0)
                            || ((isOneStar) && floor(it.cafenomad.tastyLevel) == 1.0)
                            || ((isTwoStar) && floor(it.cafenomad.tastyLevel) == 2.0)
                            || ((isThreeStar) && floor(it.cafenomad.tastyLevel) == 3.0)
                            || ((isFourStar) && floor(it.cafenomad.tastyLevel) == 4.0)
                            || ((isFiveStar) && floor(it.cafenomad.tastyLevel) == 5.0)
                }
            }
            .sortedWith(
                compareBy<CafenomadDisplay> { it.cafenomad.distanceFromCenterOfScreen }
                    .thenBy { it.cafenomad.tastyLevel * -1 }
            )
            .take(maxCafeNumShowing)
            .toList()
    }

    @SuppressLint("CheckResult")
    fun setFavorite(cafeId: String) {
        cafeDataSource.insertFavoriteV2(cafeId)
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.io())
            .subscribe({ insertID ->
                if (insertID > 0) {
                    //find target and set favorite to true then update
                    cafeListAll.find { cafe ->
                        cafe.cafenomad.id == cafeId
                    }?.also { updatedItem ->
                        updatedItem.isFavorite = true
                    }

                    //cafeDisplay update
                    cafeListDisplay.value?.let {
                        it.find { cafe ->
                            cafe.cafenomad.id == cafeId
                        }?.also { updatedItem ->
                            updatedItem.isFavorite = true
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
                    Timber.d("adding favorite success")
                }
            },{error ->
                Timber.d("adding favorite fail: $error")
                error.printStackTrace()
            })
    }


    @SuppressLint("CheckResult")
    fun deleteFavorite(cafeId: String) {
        cafeDataSource.deleteFavoriteV2(cafeId)
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.io())
            .subscribe ({ deleteCnt ->
                if (deleteCnt > 0) {
                    //cafeList update
                    cafeListAll.find { cafe ->
                        cafe.cafenomad.id == cafeId
                    }?.also { updatedItem ->
                        updatedItem.isFavorite = false
                    }

                    //cafeDisplay update
                    var currentIndex = 0
                    lateinit var newCafeListDisplay: List<CafenomadDisplay>

                    cafeListDisplay.value?.let {
                        val updatedItem = it.find { cafe ->
                            cafe.cafenomad.id == cafeId
                        }?.also { updatedItem ->
                            updatedItem.isFavorite = false
                            // 更改完內容後，依據目前的display type決定是否需要做刪減
                            newCafeListDisplay = it
                            cafeListDisplay.postValue(it)
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
                            if (isTargetNotQualifiedToShow) {

                                if (newCafeListDisplay.isNotEmpty()) {
                                    chosenCafe.postValue(
                                        //out of range
                                        if (currentIndex !in newCafeListDisplay.indices)
                                            newCafeListDisplay.first().copy()
                                        else
                                            newCafeListDisplay[currentIndex].copy()
                                    )
                                } else {
                                    chosenCafe.postValue(null)
                                }
                            } else {
                                chosenCafe.postValue(it)
                            }
                        }
                    }
                    Timber.d("delete favorite success")
                }
            },{ error ->
                Timber.d("delete favorite fail: $error")
                error.printStackTrace()
            })
        }


    fun updateScreenCenterLoc(latlng: LatLng) {
        if (Looper.myLooper() == Looper.getMainLooper())
            _screenCenterLoc.value = latlng
        else
            _screenCenterLoc.postValue(latlng)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    interface RefetchCallback{
        fun onRefetchSuccess()
        fun onRefetchFail()
    }
}