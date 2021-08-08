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

    @Query("select T1.*,(FavoriteID.cafeId IS NOT NULL) AS isFavorite FROM (SELECT * FROM Cafenomad Where latitude BETWEEN :latitudeMin AND :latitudeMax AND longitude BETWEEN :longitudeMin AND :longitudeMax) AS T1 LEFT JOIN FavoriteID ON T1.id = FavoriteID.cafeId ")
    abstract fun queryCafeByCoordinateV2(latitudeMax:Double, latitudeMin:Double, longitudeMax:Double, longitudeMin:Double):Single<List<CafenomadDisplay>>

    @Query("select T1.*,(FavoriteID.cafeId IS NOT NULL) AS isFavorite FROM FavoriteID LEFT JOIN Cafenomad AS T1 ON T1.id = FavoriteID.cafeId")
    abstract fun queryAllFavorite():Single<List<CafenomadDisplay>>

    @Query("SELECT COUNT(id) FROM Cafenomad")
    abstract fun getRowNum():Single<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertFavoriteIdV2(favorite:FavoriteID):Single<Long>

    @Query("DELETE FROM FavoriteID WHERE cafeId=(:favoriteID)")
    abstract fun deleteFavoriteIdV2(favoriteID:String):Single<Int>

}