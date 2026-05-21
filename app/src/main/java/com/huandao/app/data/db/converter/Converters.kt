package com.huandao.app.data.db.converter

import androidx.room.TypeConverter

/**
 * Room 类型转换器。
 * V1 较简单——所有字段使用 Room 原生支持的类型（String/Int/Long/Double/Boolean），
 * 此处为 V2 扩展预留。
 */
class Converters {

    /**
     * Long 型毫秒时间戳 ↔ java.util.Date（预留）。
     * V1 暂不启用，所有时间字段均为 Long。
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }
}
