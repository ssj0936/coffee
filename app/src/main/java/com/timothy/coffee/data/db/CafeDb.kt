package com.timothy.coffee.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timothy.coffee.data.model.CafeSearchResult
import com.timothy.coffee.data.model.Cafenomad

@Database(entities = [Cafenomad::class,CafeSearchResult::class],version = 1)
abstract class CafeDb :RoomDatabase(){
    companion object{
        const val DB_NAME = "cafe_db"
    }

    abstract fun cafeDao():CafeDao
}