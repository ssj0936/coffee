package com.timothy.coffee.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.timothy.coffee.data.db.IDListTypeConverter

@Entity
@TypeConverters(IDListTypeConverter::class)
data class CafeSearchResult(
    @PrimaryKey
    val city:String,
    val idList:List<String>,
    val totalCount:Int
)