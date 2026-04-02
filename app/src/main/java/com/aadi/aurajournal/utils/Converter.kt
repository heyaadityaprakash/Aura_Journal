package com.aadi.aurajournal.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
class Converter {
    @TypeConverter
//convert the list into json which can be stored in db
    fun fromStringList(value: List<String>): String{
        return Gson().toJson(value)
    }
//converts json data back to list to be read from db
    @TypeConverter
    fun toStringList(value: String): List<String>{
        //convert json to list
        val listType=object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value,listType)?:emptyList()
    }
}