package com.kampplus.hava.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.jvm.Volatile

@Database(
    entities = [FavoriteCityEntity::class, PinnedCityEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteCityDao(): FavoriteCityDao
    abstract fun pinnedCityDao(): PinnedCityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hava_durumu.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE favorite_cities_new (
                        id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        countryCode TEXT,
                        country TEXT,
                        admin1 TEXT,
                        admin1Code TEXT,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO favorite_cities_new
                    SELECT id, name, latitude, longitude, countryCode, country, admin1, admin1Code
                    FROM favorite_cities
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE favorite_cities")
                db.execSQL("ALTER TABLE favorite_cities_new RENAME TO favorite_cities")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE pinned_city (
                        slot INTEGER NOT NULL,
                        id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        countryCode TEXT,
                        country TEXT,
                        admin1 TEXT,
                        admin1Code TEXT,
                        PRIMARY KEY(slot)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE favorite_cities_new (
                        id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        countryCode TEXT,
                        country TEXT,
                        admin1 TEXT,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO favorite_cities_new
                    SELECT id, name, latitude, longitude, countryCode, country, admin1
                    FROM favorite_cities
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE favorite_cities")
                db.execSQL("ALTER TABLE favorite_cities_new RENAME TO favorite_cities")

                db.execSQL(
                    """
                    CREATE TABLE pinned_city_new (
                        slot INTEGER NOT NULL,
                        id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        countryCode TEXT,
                        country TEXT,
                        admin1 TEXT,
                        PRIMARY KEY(slot)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO pinned_city_new
                    SELECT slot, id, name, latitude, longitude, countryCode, country, admin1
                    FROM pinned_city
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE pinned_city")
                db.execSQL("ALTER TABLE pinned_city_new RENAME TO pinned_city")
            }
        }
    }
}
