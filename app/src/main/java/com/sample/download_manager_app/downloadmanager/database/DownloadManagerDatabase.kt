package com.sample.download_manager_app.downloadmanager.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sample.download_manager_app.downloadmanager.models.Task


@Database(entities = [Task::class], version = 2, exportSchema = true)
abstract class DownloadManagerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: DownloadManagerDatabase? = null

        fun getInstance(context: Context): DownloadManagerDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE task ADD COLUMN filename TEXT NOT NULL DEFAULT ''")
            }
        }

        private fun buildDatabase(context: Context): DownloadManagerDatabase {
            return Room.databaseBuilder(context, DownloadManagerDatabase::class.java, "download_manager_database")
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.e("##DM", "database created")
                    }
                })
                .build()
        }
    }
}