package com.timothy.coffee.data.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(indices = [Index("id"), Index("name")])
data class Cafenomad (
    @PrimaryKey
    @field:SerializedName("id")
    val id:String,

    @field:SerializedName("name")
    val name:String,

    var cityname:String?,

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

): Serializable {
    @Ignore var distanceFromCenterOfScreen:Int = Int.MAX_VALUE
    @Ignore var distanceFromCurrentLoc:Int = Int.MAX_VALUE
}
data class CafenomadDisplay(@Embedded var cafenomad: Cafenomad, var isFavorite: Boolean = false): Serializable
