package com.timothy.coffee.data

import android.annotation.SuppressLint
import android.util.Log
import com.timothy.coffee.api.CafenomadApiService
import com.timothy.coffee.data.db.CafeDao
import com.timothy.coffee.data.model.CafeSearchResult
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.data.model.FavoriteID
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.stream.Collectors
import javax.inject.Inject

class DataSource @Inject constructor(
   private val cafeDao: CafeDao,
   private val cafenomadApiService: CafenomadApiService
) {

//    fun queryTest(city:String):Observable<List<CafenomadDisplay>>{
//        return cafeDao.queryCafeByCityTest(city).toObservable().subscribeOn(Schedulers.io())
//    }

    fun query(city:String):Observable<List<CafenomadDisplay>>{
        return Observable.concatArray(
            queryFromDB(city),
            queryFromApi(city))
            .subscribeOn(Schedulers.io())
    }

    @SuppressLint("CheckResult")
    fun queryV2(latitude:Double, longitude:Double, range:Int):Observable<List<CafenomadDisplay>>{
        return Observable.just("")
            .flatMap {
                cafeDao.getRowNum().toObservable()
            }.flatMap {
                if(it<=0){
//                    Timber.d("no data in DB")
                    cafenomadApiService.searchAllCafes()
                        .doOnNext {list->
//                            Timber.d("insert into DB:${list}")
                            insertToDBV2(list)
                        }
                }else{
//                    Timber.d("Data in DB already")
                    Observable.just(emptyList())
                }
            }.flatMap {
//                Timber.d("fetch from DB")
                queryFromDBV2(latitude, longitude, range)
            }
            .subscribeOn(Schedulers.io())
    }

    /*有可能Query不到東西，可能像是第一次Query時DB table根本沒建起來，又或是table有了但裡面沒有符合的資料
    若Dao return type為Observable，query不到東西不會emit這就蠻尷尬的，因為這樣在concatArray中也不會觸發下一個type為Observable開始工作
    解決的方法是改用Single<List<T>>，使用Single的的差別在於：
    query 不到東西的話，Observable不會emit任何東西，但single會回傳onError
    之所以用Single<List<T>>，是為了在沒東西時發射onSuccess回傳空list，並再用toObservable轉換成Observable並發送OnCompleter
    結束並開始下一個Observable的作業，也就是ApiQuery的部分

    (https://blog.nex3z.com/2017/10/31/android-room-rxjava-%E6%9F%A5%E8%AF%A2%E8%AE%B0%E5%BD%95%E4%B8%8D%E5%AD%98%E5%9C%A8%E7%9A%84%E5%A4%84%E7%90%86%E6%96%B9%E6%B3%95/)
    (https://medium.com/androiddevelopers/room-rxjava-acb0cd4f3757)
    (https://code.tutsplus.com/zh-hant/tutorials/reactive-programming-operators-in-rxjava-20--cms-28396)*/
    private fun queryFromDB(city: String):Observable<List<CafenomadDisplay>>{
        return cafeDao.queryCafeByCity(city)
            .toObservable()
            .subscribeOn(Schedulers.io())
    }

    private fun queryFromDBV2(latitude:Double, longitude:Double, range:Int):Observable<List<CafenomadDisplay>>{
        return queryFromDBV2Convert(latitude,longitude,range)
            .toObservable()
            .subscribeOn(Schedulers.io())
    }

    private fun queryFromDBV2Convert(latitude:Double, longitude:Double, range:Int):Single<List<CafenomadDisplay>>{
        Timber.d("select T1.*,(FavoriteID.cafeId IS NOT NULL) AS isFavorite FROM (SELECT * FROM Cafenomad Where latitude BETWEEN ${latitude+0.01*range} AND ${latitude-0.01*range} AND longitude BETWEEN ${longitude+0.01*range} AND ${longitude-0.01*range}) AS T1 LEFT JOIN FavoriteID ON T1.id = FavoriteID.cafeId")

        return cafeDao.queryCafeByCoordinateV2(latitude+0.01*range,latitude-0.01*range,
            longitude+0.01*range,longitude-0.01*range)
    }

    private fun insertToDB(list: List<Cafenomad>, city: String){
        var tmpList = list
        tmpList.forEach {it.cityname = city}
        cafeDao.insertCafe(tmpList)
    }

    private fun insertToDBV2(list: List<Cafenomad>){
        var tmpList = list
        tmpList.forEach {it.cityname = null}
        cafeDao.insertCafe(tmpList)
    }

    private fun queryFromApi(city: String):Observable<List<CafenomadDisplay>>{
        return Observable.just("")
            .observeOn(Schedulers.io())
            .flatMap {
                cafenomadApiService.searchCafes(city)
                    .doOnNext {list->
                        insertToDB(list,city)
                    }
            }.flatMap {
                queryFromDB(city)
            }
    }

    private fun queryFromApiV2(latitude:Double, longitude:Double, range:Int):Observable<List<CafenomadDisplay>>{
        return Observable.just("")
            .observeOn(Schedulers.io())
            .flatMap {
                cafenomadApiService.searchAllCafes()
                    .doOnNext {list->
                        insertToDBV2(list)
                    }
            }.flatMap {
                queryFromDBV2(latitude, longitude, range)
            }
    }

    fun insertFavorite(cafeId:String):Long{
        //return ID for success, return -1 for conflict replace
        return cafeDao.insertFavoriteId(FavoriteID(cafeId))
    }

    fun deleteFavorite(cafeId:String):Int{
        //return num of delete item
        return cafeDao.deleteFavoriteId(cafeId)
    }
}