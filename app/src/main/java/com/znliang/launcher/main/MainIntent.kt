package com.znliang.launcher.main

import android.content.Context
import android.view.View
import com.znliang.launcher.tags.model.AppInfo

sealed class MainIntent {
    object LoadApps : MainIntent()
    data class SearchQueryChanged(val query: String) : MainIntent()
    data class PageStateChanged(val tagZoomIn: Boolean) : MainIntent()
    data class LaunchApp(val app: AppInfo) : MainIntent()
    data class LongPress(val app: AppInfo, val context: Context, val anchor: View) : MainIntent()
}