package com.timothy.coffee.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.timothy.coffee.data.model.CafeSearchResult
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.FavoriteID

@Database(entities = [Cafenomad::class, CafeSearchResult::class, FavoriteID::class],version = 2)
abstract class CafeDb:RoomDatabase(){
    abstract fun cafeDao():CafeDao

    companion object{
        val MIGRATION_1_2 = object:Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `FavoriteID` (`cafeId` TEXT NOT NULL,PRIMARY KEY(`cafeId`)) ")
            }
        }
    }
}