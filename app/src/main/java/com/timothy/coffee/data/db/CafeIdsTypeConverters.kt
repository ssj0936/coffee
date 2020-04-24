package com.timothy.coffee.data.db

import androidx.room.TypeConverter
import androidx.room.util.StringUtil
import java.util.*

class CafeIdsTypeConverters {

    @TypeConverter
    fun idStringToStringList(ids:String?):List<String>{
        return if (ids==null)
            emptyList()
        else
            ids.split(",")
    }

    @TypeConverter
    fun idStringListToString(ids:List<String>?):String{
        return if(ids==null)
            ""
        else
            ids.joinToString(",")
    }
}