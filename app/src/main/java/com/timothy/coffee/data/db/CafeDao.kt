package com.timothy.coffee.data.db

import androidx.room.*
import com.timothy.coffee.data.model.CafeSearchResult
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.data.model.FavoriteID
import io.reactivex.Observable
import io.reactivex.Single

@Dao
abstract class CafeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCafe(cafe:List<Cafenomad>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSearchResult(result: CafeSearchResult)

//    @Query("select T1.*,(FavoriteID.cafeId IS NOT NULL) AS isFavorite FROM (SELECT * FROM Cafenomad Where id in (:cafeIds)) AS T1 LEFT JOIN FavoriteID ON T1.id = FavoriteID.cafeId ")
//    abstract fun queryCafeByIds(cafeIds:List<String>):Observable<List<CafenomadDisplay>>

//    @Query("Select * From Cafenomad Where id in (:cafeIds)")
//    abstract fun queryCafeByIds(cafeIds:List<String>):Observable<List<Cafenomad>>

//    @Query("Select * From Cafenomad Where cityname = (:cityname)")
//    abstract fun queryCafeByCity(cityname:String):Single<List<Cafenomad>>

    @Query("select T1.*,(FavoriteID.cafeId IS NOT NULL) AS isFavorite FROM (SELECT * FROM Cafenomad Where cityname = (:cityname)) AS T1 LEFT JOIN FavoriteID ON T1.id = FavoriteID.cafeId ")
    abstract fun queryCafeByCity(cityname:String):Observable<List<CafenomadDisplay>>

//    @Query("Select * From CafeSearchResult Where city = (:city) LIMIT 1")
//    abstract fun queryCafeSearchResultObservable(city:String): Observable<List<CafeSearchResult>>

//    @Query("Select * From CafeSearchResult Where city = (:city) LIMIT 1")
//    abstract fun queryCafeSearchResult(city:String): Single<List<CafeSearchResult>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertFavoriteId(favorite:FavoriteID)

    @Query("DELETE FROM FavoriteID WHERE cafeId=(:favoriteID)")
    abstract fun deleteFavoriteId(favoriteID:String)

}