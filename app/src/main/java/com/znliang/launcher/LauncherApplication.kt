package com.znliang.launcher

import android.app.Application
import com.znliang.launcher.appdata.AppInfoDao
import com.znliang.launcher.appdata.RoomProvider

class LauncherApplication: Application() {

    val database by lazy { RoomProvider.get(applicationContext) }

    val appInfoDao: AppInfoDao by lazy {
        database.appInfoDao()
    }
}