package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ServerEntity::class, TunnelConfigEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun tunnelDao(): TunnelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE servers ADD COLUMN billingType TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE servers ADD COLUMN billingDayOfMonth INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE servers ADD COLUMN billingIntervalDays INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE servers ADD COLUMN billingLastPaidDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE servers ADD COLUMN billingCostMonthly REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE servers ADD COLUMN billingApiKey TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE servers ADD COLUMN cachedBalance REAL")
                db.execSQL("ALTER TABLE servers ADD COLUMN cachedCurrency TEXT NOT NULL DEFAULT 'RUB'")
                db.execSQL("ALTER TABLE servers ADD COLUMN cachedHoursLeft INTEGER")
                db.execSQL("ALTER TABLE servers ADD COLUMN cachedDaysLeft INTEGER")
                db.execSQL("ALTER TABLE servers ADD COLUMN billingLastCheckedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xui_manager_db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
