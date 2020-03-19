package com.timothy.coffee.data.model

import com.google.gson.annotations.SerializedName

data class Cafenomad (
    @field:SerializedName("id")
    val id:String,

    @field:SerializedName("name")
    val name:String,

    @field:SerializedName("wifi")
    val wifiStabilityLevel:Double,

    @field:SerializedName("seat")
    val seatLevel:Double,

    @field:SerializedName("quiet")
    val quietLevel:Double,

    @field:SerializedName("tasty")
    val tastyLevel:Double,

    @field:SerializedName("cheap")
    val priceLevel:Double,

    @field:SerializedName("music")
    val goodMusicLevel:Double,

    @field:SerializedName("address")
    val address:String,

    @field:SerializedName("latitude")
    val latitude:String,

    @field:SerializedName("longitude")
    val longitude:String,

    @field:SerializedName("url")
    val url:String,

    @field:SerializedName("limited_time")
    val isTimeLimited:String,

    @field:SerializedName("socket")
    val isSocketProvided:String,

    @field:SerializedName("standing_desk")
    val isStandingDeskAvailable:String,

    @field:SerializedName("mrt")
    val mrtName:String,

    @field:SerializedName("open_time")
    val openTime:String

)