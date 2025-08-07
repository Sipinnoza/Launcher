package com.znliang.launcher.appdata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<AppInfoEntity>)

    @Query("SELECT * FROM app_info WHERE packageName NOT IN (:blacklist)")
    suspend fun getAllExceptBlacklist(blacklist: Set<String>): List<AppInfoEntity>

    @Query("SELECT * FROM app_info WHERE label LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<AppInfoEntity>

    @Insert
    suspend fun insertClickLog(log: AppClickLogEntity)

    @Query("DELETE FROM app_click_log WHERE timestamp < :cutoff")
    suspend fun deleteOldClicks(cutoff: Long)

    @Query("""
    SELECT packageName, COUNT(*) as popularity
    FROM app_click_log
    WHERE timestamp > :since
    GROUP BY packageName
""")
    suspend fun getPopularitySince(since: Long): List<PopularityStat>
}