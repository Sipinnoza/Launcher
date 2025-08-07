package com.znliang.launcher.appdata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_click_log")
data class AppClickLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val timestamp: Long
)
