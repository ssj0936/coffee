package com.timothy.coffee.data.db

import androidx.room.TypeConverter
import androidx.room.util.StringUtil
import java.util.*

class CafeIdsTypeConverters {

    @TypeConverter
    fun idStringToStringList(ids:String?):List<String>{
        return ids?.split(",") ?: emptyList()
    }

    @TypeConverter
    fun idStringListToString(ids:List<String>?):String{
        return ids?.joinToString(",") ?: ""
    }
}