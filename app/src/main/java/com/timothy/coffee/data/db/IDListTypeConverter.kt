package com.timothy.coffee.data.db

import android.text.TextUtils
import androidx.room.TypeConverter
import androidx.room.util.StringUtil


class IDListTypeConverter{
    @TypeConverter
    fun convertStringToList(IDString: String): List<String> = IDString.split(",")

    @TypeConverter
    fun convertListToString(IDList: List<String>): String = TextUtils.join(",", IDList)
}