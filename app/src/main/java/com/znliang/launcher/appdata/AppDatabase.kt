package com.znliang.launcher.appdata

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 2,
    entities = [
        AppInfoEntity::class,
        AppClickLogEntity::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
}
