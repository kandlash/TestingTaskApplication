package com.example.testingtaskapplication.di

import android.app.Application
import com.example.testingtaskapplication.database.MainDb
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App:Application() {
    val db by lazy{MainDb.getDb(this)}
}