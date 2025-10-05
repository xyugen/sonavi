package com.pagzone.sonavi.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pagzone.sonavi.data.dao.DetectionLogDao
import com.pagzone.sonavi.data.dao.EmergencyContactDao
import com.pagzone.sonavi.data.dao.SoundProfileDao
import com.pagzone.sonavi.data.dao.VibrationPatternDao
import com.pagzone.sonavi.data.db.AppDatabase
import com.pagzone.sonavi.data.db.utils.DatabaseInitializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration(true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Use coroutine so this won’t block startup
                    CoroutineScope(Dispatchers.IO).launch {
                        // IMPORTANT: we can’t get DAO from SupportSQLiteDatabase
                        // → Instead, we reopen the real AppDatabase
                        val appDb = Room.databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "app_database"
                        ).build()
                        DatabaseInitializer.populateBuiltInSounds(appDb.soundProfileDao())
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideSoundProfileDao(db: AppDatabase): SoundProfileDao = db.soundProfileDao()

    @Provides
    fun provideVibrationPatternDao(db: AppDatabase): VibrationPatternDao = db.vibrationPatternDao()

    @Provides
    fun provideEmergencyContactDao(db: AppDatabase): EmergencyContactDao = db.emergencyContactDao()

    @Provides
    fun provideDetectionLogDao(db: AppDatabase): DetectionLogDao = db.detectionLogDao()
}
