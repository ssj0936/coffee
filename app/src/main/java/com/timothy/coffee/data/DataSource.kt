package com.timothy.coffee.data

import android.annotation.SuppressLint
import android.util.Log
import com.timothy.coffee.api.CafenomadApiService
import com.timothy.coffee.data.db.CafeDao
import com.timothy.coffee.data.model.CafeSearchResult
import com.timothy.coffee.data.model.Cafenomad
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
    fun query(city:String):Observable<List<Cafenomad>>{
        return Observable.concatArray(
            queryFromDB(city),
            queryFromApi(city))
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
    private fun queryFromDB(city: String):Observable<List<Cafenomad>>{
        return cafeDao.queryCafeSearchResult(city)
            .filter { it.isNotEmpty() }
            .toObservable()
            .flatMap {result->
                    cafeDao.queryCafeByIds(result[0].idList)
            }
            .subscribeOn(Schedulers.io())
    }


    private fun queryFromDBObservable(city: String):Observable<List<Cafenomad>>{
        return cafeDao.queryCafeSearchResultObservable(city)
            .doOnComplete {
                Timber.d("1_Observable doOnComplete")
            }
            .doOnNext {
                Timber.d("1_Observable doOnNext")
            }
            .flatMap { result ->
                if(result.isEmpty()){
                    Timber.d("result.isEmpty")
//                    Observable.create<List<Cafenomad>> {
//                        it.onComplete()
//                    }
                    Observable.just(emptyList())
                }
                else {
                    Timber.d("result.isNotEmpty")
                    cafeDao.queryCafeByIds(result[0].idList)
                }
            }
            .doOnComplete {
                Timber.d("2_Observable doOnComplete")
            }
            .doOnNext {
                Timber.d("2_Observable doOnNext")
            }
            .subscribeOn(Schedulers.io())
    }


    private fun insertToDB(list: List<Cafenomad>, city: String){
        var tmpList = if (list.size<500) list else list.subList(0,499)
        tmpList.forEach {it.cityname = city}
        val searchResult = CafeSearchResult(city,
            tmpList.stream().map { item->item.id }.collect(Collectors.toList()),
            tmpList.size)
        Timber.d(searchResult.toString())

        cafeDao.insertSearchResult(searchResult)
        cafeDao.insertCafe(tmpList)
    }

    private fun queryFromApi(city: String):Observable<List<Cafenomad>>{
        return Observable.just("")
//            .doOnNext {
//                Timber.d("queryFromApi")
//            }
            .flatMap {
            cafenomadApiService.searchCafes(city)
                .doOnNext {list->
                    insertToDB(list,city)
                }
        }
    }
}