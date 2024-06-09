package com.example.testingtaskapplication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert(LogItem::class)
    fun insertLogItem(logItem: LogItem)

    @Query("SELECT * FROM logItems")
    fun getAllLogItems(): Flow<List<LogItem>>
}