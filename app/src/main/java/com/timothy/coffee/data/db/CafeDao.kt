package com.timothy.coffee.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timothy.coffee.data.model.CafeSearchResult
import com.timothy.coffee.data.model.Cafenomad

@Dao
abstract class CafeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertResultCafeIdList(result: CafeSearchResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCafeData(cafe:List<Cafenomad>)

    @Query("SELECT * FROM CafeSearchResult WHERE city=:city")
    abstract fun queryCafeListByCity(city:String):CafeSearchResult

    @Query("SELECT * FROM Cafenomad WHERE id in (:ids)")
    abstract fun queryCafeById(ids:List<String>):List<Cafenomad>
}