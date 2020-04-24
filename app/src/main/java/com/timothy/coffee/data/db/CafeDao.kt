package com.timothy.coffee.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timothy.coffee.data.model.CafeSearchResult
import com.timothy.coffee.data.model.Cafenomad
import io.reactivex.Observable
import io.reactivex.Single

@Dao
abstract class CafeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCafe(cafe:List<Cafenomad>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSearchResult(result: CafeSearchResult)

    @Query("Select * From Cafenomad Where id in (:cafeIds) LIMIT 30")
    abstract fun queryCafeByIds(cafeIds:List<String>):Observable<List<Cafenomad>>

    @Query("Select * From Cafenomad Where cityname = (:cityname) LIMIT 30")
    abstract fun queryCafeByCity(cityname:String):Observable<List<Cafenomad>>

    @Query("Select * From CafeSearchResult Where city = (:city) LIMIT 1")
    abstract fun queryCafeSearchResultObservable(city:String): Observable<List<CafeSearchResult>>

    @Query("Select * From CafeSearchResult Where city = (:city) LIMIT 1")
    abstract fun queryCafeSearchResult(city:String): Single<List<CafeSearchResult>>

}