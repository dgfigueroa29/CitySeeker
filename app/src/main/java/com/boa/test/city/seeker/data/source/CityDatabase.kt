package com.boa.test.city.seeker.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boa.test.city.seeker.data.dao.CityDao
import com.boa.test.city.seeker.data.entity.CityEntity

@Database(entities = [CityEntity::class], version = 1, exportSchema = false)
abstract class CityDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    companion object {
        @Volatile private var INSTANCE: CityDatabase? = null

        fun getDatabase(context: Context): CityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CityDatabase::class.java,
                    "city_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
