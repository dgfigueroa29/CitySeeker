package com.boa.test.city.seeker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.boa.test.city.seeker.data.local.CityDatabase.Companion.COLUMN_COUNTRY
import com.boa.test.city.seeker.data.local.CityDatabase.Companion.COLUMN_NAME
import com.boa.test.city.seeker.data.local.CityDatabase.Companion.INDEX_COUNTRY
import com.boa.test.city.seeker.data.local.CityDatabase.Companion.INDEX_NAME
import com.boa.test.city.seeker.data.local.CityDatabase.Companion.TABLE_CITIES

@Entity(
    tableName = TABLE_CITIES, indices = [
        Index(value = ["name"], name = INDEX_NAME),
        Index(value = ["country"], name = INDEX_COUNTRY),
    ]
)
//@Fts4
data class CityEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = COLUMN_NAME)
    val name: String,
    @ColumnInfo(name = COLUMN_COUNTRY)
    val country: String,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "longitude")
    val longitude: Double
) {
    fun getTitle(): String {
        return "$name, $country"
    }

    fun getSubtitle(): String {
        return "Lat: $latitude, Lon: $longitude"
    }
}
