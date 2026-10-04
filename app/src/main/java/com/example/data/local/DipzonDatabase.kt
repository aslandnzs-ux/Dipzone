package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SeriesEntity::class, EpisodeEntity::class, WatchProgressEntity::class,
        SavedItemEntity::class, LikedItemEntity::class, CommentEntity::class,
        UserProfileEntity::class, AnalyticsEventEntity::class, SearchHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DipzonDatabase : RoomDatabase() {
    abstract fun dipzonDao(): DipzonDao

    companion object {
        @Volatile private var INSTANCE: DipzonDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS search_history (query TEXT NOT NULL, searchedAt INTEGER NOT NULL, searchCount INTEGER NOT NULL, PRIMARY KEY(query))")
                // v1 contained demo activity that looked like real user behaviour. Remove only that activity.
                db.execSQL("DELETE FROM watch_progress")
                db.execSQL("DELETE FROM saved_items")
                db.execSQL("DELETE FROM liked_items")
                db.execSQL("DELETE FROM analytics_events")
                db.execSQL("DELETE FROM comments")
                db.execSQL("UPDATE user_profile SET username='Dipzon Kullanıcısı', email='', isPremium=0, premiumTier='Ücretsiz', coins=0, videoQuality='Otomatik' WHERE id='dipzon_user_1'")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): DipzonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DipzonDatabase::class.java,
                    "dipzon_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DipzonDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DipzonDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) { populateInitialData(database.dipzonDao()) }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        if (database.dipzonDao().getTotalSeriesCount() == 0) {
                            populateInitialData(database.dipzonDao())
                        }
                    }
                }
            }

            private suspend fun populateInitialData(dao: DipzonDao) {
                // Demo catalogue remains available until real content is published from Admin.
                // Fake watch/search/like/save activity is intentionally NOT seeded.
                dao.insertAllSeries(InitialData.sampleSeries)
                dao.insertAllEpisodes(InitialData.sampleEpisodes)
                dao.upsertUserProfile(
                    UserProfileEntity(
                        username = "Dipzon Kullanıcısı",
                        email = "",
                        preferredGenres = "",
                        isPremium = false,
                        premiumTier = "Ücretsiz",
                        coins = 0,
                        videoQuality = "Otomatik"
                    )
                )
            }
        }
    }
}
