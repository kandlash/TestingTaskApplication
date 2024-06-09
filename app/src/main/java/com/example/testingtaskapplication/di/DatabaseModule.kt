package com.example.testingtaskapplication.di

import android.content.Context
import androidx.room.Room
import com.example.testingtaskapplication.database.Dao
import com.example.testingtaskapplication.database.MainDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): MainDb {
        return Room.databaseBuilder(
            appContext,
            MainDb::class.java,
            "logs.db"
        ).build()
    }

    @Provides
    fun provideLogDao(db: MainDb): Dao {
        return db.getDao()
    }
}