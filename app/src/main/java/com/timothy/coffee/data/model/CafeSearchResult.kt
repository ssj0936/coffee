package com.timothy.coffee.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.timothy.coffee.data.db.CafeIdsTypeConverters

@Entity
@TypeConverters(CafeIdsTypeConverters::class)
class CafeSearchResult(

    @field:PrimaryKey
    val city:String,

    val cafeIds:List<String>,
    val totalCount:Int
)