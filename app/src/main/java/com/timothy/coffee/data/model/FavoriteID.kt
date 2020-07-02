package com.timothy.coffee.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoriteID (
    @PrimaryKey
    val cafeId:String
)