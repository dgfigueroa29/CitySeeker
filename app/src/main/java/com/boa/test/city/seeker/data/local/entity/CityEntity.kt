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

/**
 * Represents a city entity in the local database.
 *
 * @property id The unique identifier of the city.
 * @property name The name of the city.
 * @property country The country where the city is located.
 * @property latitude The latitude coordinate of the city.
 * @property longitude The longitude coordinate of the city.
 */
@Entity(
    tableName = TABLE_CITIES, indices = [
        Index(value = ["name"], name = INDEX_NAME),
        Index(value = ["country"], name = INDEX_COUNTRY),
    ]
)
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
)
