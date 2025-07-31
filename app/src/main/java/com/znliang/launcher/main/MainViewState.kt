package com.znliang.launcher.main

import com.znliang.launcher.tags.model.AppInfo

data class MainViewState(
    val appList: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<AppInfo> = emptyList(),
    val isSearchVisible: Boolean = false,
    val isResultVisible: Boolean = false
)