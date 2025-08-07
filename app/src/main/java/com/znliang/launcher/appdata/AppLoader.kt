package com.znliang.launcher.appdata

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.edit
import com.znliang.launcher.tags.model.AppInfo

class AppLoader(
    val context: Context,
    private val packageManager: PackageManager,
    private val appDao: AppInfoDao
) {

    companion object {
        private const val PREFS_NAME = "app_blacklist"
        private const val KEY_BLACKLIST = "blacklist_set"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var appList: List<AppInfo> = emptyList()

    /**
     * 获取黑名单包名集合
     */
    fun getBlackList(): Set<String> {
        return prefs.getStringSet(KEY_BLACKLIST, emptySet()) ?: emptySet()
    }

    /**
     * 添加包名到黑名单
     */
    fun addToBlackList(packageName: String) {
        val set = getBlackList().toMutableSet()
        if (set.add(packageName)) {
            prefs.edit { putStringSet(KEY_BLACKLIST, set) }
        }
    }

    fun applyDefaultBlackList() {
        val defaultList = listOf(
            "com.znliang.launcher",
            "cn.soulapp.android",
            "com.android.stk"
        )
        val set = getBlackList().toMutableSet()
        var changed = false
        for (pkg in defaultList) {
            if (set.add(pkg)) changed = true
        }
        if (changed) {
            prefs.edit { putStringSet(KEY_BLACKLIST, set) }
        }
    }

    /**
     * 获取应用列表
     */
    fun getFinalList(): List<AppInfo> {
        val blackList = getBlackList()
        return appList.filterNot { it.packageName in blackList }
    }

    /**
     * 根据关键字搜索应用
     */
    fun searchApps(keyword: String): List<AppInfo> {
        val lowerKeyword = keyword.lowercase()
        return appList.filter {
            it.label.toString().lowercase().contains(lowerKeyword) ||
                    it.packageName.lowercase().contains(lowerKeyword)
        }
    }

    /**
     * 从黑名单移除包名
     */
    fun removeFromBlackList(packageName: String) {
        val set = getBlackList().toMutableSet()
        if (set.remove(packageName)) {
            prefs.edit { putStringSet(KEY_BLACKLIST, set) }
        }
    }

    /**
     * 判断包名是否在黑名单
     */
    fun isInBlackList(packageName: String): Boolean {
        return getBlackList().contains(packageName)
    }

    suspend fun loadAppsAndStore() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        val blackList = getBlackList()

        // 清理 7 天前的点击记录
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        appDao.deleteOldClicks(sevenDaysAgo)

        // 获取最近 7 天点击次数
        val popularityMap = appDao.getPopularitySince(sevenDaysAgo).associateBy { it.packageName }

        appList = resolveInfos.map {
            val packageName = it.activityInfo.packageName
            val popularity = popularityMap[packageName]?.popularity ?: 0

            AppInfo(
                label = it.loadLabel(packageManager),
                packageName = packageName,
                className = it.activityInfo.name,
                icon = it.loadIcon(packageManager),
                popularity = popularity
            )
        }.sortedBy { it.label.toString().lowercase() }


        val visibleApps = appList.filterNot { it.packageName in blackList }

        val appEntities = visibleApps.map {
            AppInfoEntity(
                packageName = it.packageName,
                label = it.label.toString(),
                className = it.className,
                popularity = it.popularity
            )
        }
        appDao.insertAll(appEntities)
    }

    suspend fun markAppOpened(packageName: String) {
        val now = System.currentTimeMillis()
        val clickLog = AppClickLogEntity(packageName = packageName, timestamp = now)
        appDao.insertClickLog(clickLog)
    }
}