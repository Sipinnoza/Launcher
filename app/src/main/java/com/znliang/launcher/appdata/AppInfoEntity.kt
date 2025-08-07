package com.znliang.launcher.appdata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_info")
data class AppInfoEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val className: String,
    val popularity: Int = 0
)
