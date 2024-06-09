package com.example.testingtaskapplication.di

import android.content.Context
import androidx.room.Room
import com.example.testingtaskapplication.database.MainDb
import com.example.testingtaskapplication.server.WebsocketServer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MainDb {
        return Room.databaseBuilder(
            context,
            MainDb::class.java,
            "logs.db"
        ).build()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Provides
    @Singleton
    fun provideWebsocketServer(@ApplicationContext context: Context, db: MainDb): WebsocketServer {
        return WebsocketServer(context, db)
    }
}