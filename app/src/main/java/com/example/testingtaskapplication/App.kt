package com.example.testingtaskapplication

import android.app.Application
import com.example.testingtaskapplication.database.MainDb

class App:Application() {
    val db by lazy{MainDb.getDb(this)}
}