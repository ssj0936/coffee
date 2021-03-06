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

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    abstract fun insertSearchResult(result: CafeSearchResult)

//    @Query("select T1.*,(FavoriteID.cafeId IS NOT NULL) AS isFavorite FROM (SELECT * FROM Cafenomad Where id in (:cafeIds)) AS T1 LEFT JOIN FavoriteID ON T1.id = FavoriteID.cafeId ")
//    abstract fun queryCafeByIds(cafeIds:List<String>):Observable<List<CafenomadDisplay>>

//    @Query("Select * From Cafenomad Where id in (:cafeIds)")
//    abstract fun queryCafeByIds(cafeIds:List<String>):Observable<List<Cafenomad>>

//    @Query("Select * From Cafenomad Where cityname = (:cityname)")
//    abstract fun queryCafeByCity(cityname:String):Single<List<Cafenomad>>

//    @Query("select T1.*,(FavoriteID.cafeId IS NOT NULL) AS isFavorite FROM (SELECT * FROM Cafenomad Where cityname = (:cityname)) AS T1 LEFT JOIN FavoriteID ON T1.id = FavoriteID.cafeId ")
//    abstract fun queryCafeByCity(cityname:String):Single<List<CafenomadDisplay>>

//    @Query("select T1.*,(FavoriteID.cafeId IS NOT NULL) AS isFavorite FROM (SELECT * FROM Cafenomad Where latitude BETWEEN :latitude+0.01*:range AND :latitude-0.01*:range AND longitude BETWEEN :longitude+0.01*:range AND :longitude-0.01*:range) AS T1 LEFT JOIN FavoriteID ON T1.id = FavoriteID.cafeId ")
//    abstract fun queryCafeByCoordinate(latitude:Double, longitude:Double, range:Int):Single<List<CafenomadDisplay>>

    @Query("select T1.*,(FavoriteID.cafeId IS NOT NULL) AS isFavorite FROM (SELECT * FROM Cafenomad Where latitude BETWEEN :latitudeMin AND :latitudeMax AND longitude BETWEEN :longitudeMin AND :longitudeMax) AS T1 LEFT JOIN FavoriteID ON T1.id = FavoriteID.cafeId ")
    abstract fun queryCafeByCoordinateV2(latitudeMax:Double, latitudeMin:Double, longitudeMax:Double, longitudeMin:Double):Single<List<CafenomadDisplay>>

    @Query("select T1.*,(FavoriteID.cafeId IS NOT NULL) AS isFavorite FROM FavoriteID LEFT JOIN Cafenomad AS T1 ON T1.id = FavoriteID.cafeId")
    abstract fun queryAllFavorite():Single<List<CafenomadDisplay>>

    @Query("SELECT COUNT(id) FROM Cafenomad")
    abstract fun getRowNum():Single<Int>

//    @Query("Select * From CafeSearchResult Where city = (:city) LIMIT 1")
//    abstract fun queryCafeSearchResultObservable(city:String): Observable<List<CafeSearchResult>>

//    @Query("Select * From CafeSearchResult Where city = (:city) LIMIT 1")
//    abstract fun queryCafeSearchResult(city:String): Single<List<CafeSearchResult>>

//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    abstract fun insertFavoriteId(favorite:FavoriteID):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertFavoriteIdV2(favorite:FavoriteID):Single<Long>

//    @Query("DELETE FROM FavoriteID WHERE cafeId=(:favoriteID)")
//    abstract fun deleteFavoriteId(favoriteID:String):Int

    @Query("DELETE FROM FavoriteID WHERE cafeId=(:favoriteID)")
    abstract fun deleteFavoriteIdV2(favoriteID:String):Single<Int>

}