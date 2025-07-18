package com.boa.test.city.seeker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.boa.test.city.seeker.BuildConfig
import com.boa.test.city.seeker.data.local.dao.CityDao
import com.boa.test.city.seeker.data.local.entity.CityEntity
import timber.log.Timber
import java.util.concurrent.Executors

/**
 * The Room database for the city data.
 *
 * This database contains the [CityEntity] table and provides a [CityDao] to access the data.
 * It follows the singleton pattern to ensure only one instance of the database is created.
 */
@Database(entities = [CityEntity::class], version = 1, exportSchema = false)
abstract class CityDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    companion object {
        const val DB_NAME = "city_database"
        const val TABLE_CITIES = "cities"
        const val INDEX_NAME = "idx_city_name"
        const val INDEX_COUNTRY = "idx_city_country"
        const val COLUMN_NAME = "name"
        const val COLUMN_COUNTRY = "country"

        /**
         * The singleton instance of the [CityDatabase].
         * This property is marked as [Volatile] to ensure that writes are immediately
         * visible to other threads.
         */
        @Volatile
        private var INSTANCE: CityDatabase? = null

        /**
         * Gets the singleton instance of the [CityDatabase].
         * If the instance does not exist, it is created using [Room.databaseBuilder].
         * The database is created with indices on the "name" and "country" columns of the
         * "cities" table.
         * A query callback is added to log SQL queries in debug mode.
         *
         * @param context The application context used to build the database.
         * @return The singleton instance of [CityDatabase].
         */
        @Suppress("unused")
        fun getInstance(context: Context): CityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CityDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("CREATE INDEX $INDEX_NAME ON $TABLE_CITIES($COLUMN_NAME)")
                            db.execSQL(
                                "CREATE INDEX $INDEX_COUNTRY ON " +
                                        "$TABLE_CITIES($COLUMN_COUNTRY)"
                            )
                        }
                    })
                    .setQueryCallback({ sql, params ->
                        if (BuildConfig.DEBUG) {
                            Timber.d("RoomQuerySQL: $sql - Params: $params")
                        }
                    }, Executors.newSingleThreadExecutor())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
