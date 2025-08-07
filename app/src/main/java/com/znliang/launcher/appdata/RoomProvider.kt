package com.znliang.launcher.appdata

import android.content.Context
import androidx.room.Room

object RoomProvider {
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "launcher_db"
            )
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}