package com.example.testingtaskapplication.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "logItems")
data class LogItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(name="date")
    val date: String,
    @ColumnInfo(name= "logText")
    val logText: String
)