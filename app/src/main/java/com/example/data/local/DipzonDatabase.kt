package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SeriesEntity::class,
        EpisodeEntity::class,
        WatchProgressEntity::class,
        SavedItemEntity::class,
        LikedItemEntity::class,
        CommentEntity::class,
        UserProfileEntity::class,
        AnalyticsEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DipzonDatabase : RoomDatabase() {

    abstract fun dipzonDao(): DipzonDao

    companion object {
        @Volatile
        private var INSTANCE: DipzonDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DipzonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DipzonDatabase::class.java,
                    "dipzon_database"
                )
                .addCallback(DipzonDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DipzonDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.dipzonDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Guarantee data is present even if DB was already opened
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        if (database.dipzonDao().getTotalSeriesCount() == 0) {
                            populateInitialData(database.dipzonDao())
                        }
                    }
                }
            }

            suspend fun populateInitialData(dao: DipzonDao) {
                dao.insertAllSeries(InitialData.sampleSeries)
                dao.insertAllEpisodes(InitialData.sampleEpisodes)
                dao.insertAllComments(InitialData.sampleComments)
                dao.upsertUserProfile(InitialData.sampleProfile)
                // Seed a watch progress item so "İzlemeye Devam Et" shows up immediately!
                dao.upsertWatchProgress(
                    WatchProgressEntity(
                        seriesId = "ser_karanlik_safak",
                        episodeId = "ser_karanlik_safak_s1_e2",
                        seasonNumber = 1,
                        episodeNumber = 2,
                        positionSeconds = 85,
                        durationSeconds = 184,
                        updatedAt = System.currentTimeMillis() - 3600000L
                    )
                )
                // Seed an item in Saved
                dao.insertSavedItem(SavedItemEntity(seriesId = "ser_paralel_baglanti"))
                // Seed liked item
                dao.insertLikedItem(LikedItemEntity(seriesId = "ser_karanlik_safak"))
            }
        }
    }
}
