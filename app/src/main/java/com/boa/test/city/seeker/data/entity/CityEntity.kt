package com.boa.test.city.seeker.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
//@Fts4
data class CityEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "country")
    val country: String,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "longitude")
    val longitude: Double
) {
    fun getTitle():String {
        return "$country - $name"
    }

    fun getSubtitle():String {
        return "Lat: $latitude, Lon: $longitude"
    }
}
