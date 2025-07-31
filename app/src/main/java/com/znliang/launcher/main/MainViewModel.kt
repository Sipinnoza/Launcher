package com.znliang.launcher.main

import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znliang.launcher.AppLoader
import com.znliang.launcher.tags.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val appLoader: AppLoader) : ViewModel() {

    private val _state = MutableStateFlow(MainViewState())
    val state: StateFlow<MainViewState> = _state

    fun processIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.LoadApps -> loadApps()
            is MainIntent.SearchQueryChanged -> searchApps(intent.query)
            is MainIntent.LaunchApp -> { launchApp(intent.app) }
            is MainIntent.PageStateChanged -> { pageStateChanged(intent.tagZoomIn) }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            appLoader.applyDefaultBlackList()
            appLoader.loadApps()
            val apps = appLoader.getFinalList()
            _state.value = _state.value.copy(appList = apps)
        }
    }

    private fun searchApps(query: String) {
        viewModelScope.launch {
            val results = if (query.isBlank()) emptyList() else appLoader.searchApps(query)
            _state.value = _state.value.copy(
                searchQuery = query,
                searchResults = results,
                isResultVisible = query.isNotBlank()
            )
        }
    }

    private fun launchApp(app: AppInfo) {
        viewModelScope.launch {
            _state.value = _state.value.copy(searchQuery = "")
        }
        try {
            val intent = Intent().apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(appLoader.context, intent, null)
        } catch (_: Exception) { }
    }

    private fun pageStateChanged(tagZoomIn: Boolean) {
        _state.value = _state.value.copy(isSearchVisible = tagZoomIn, searchQuery = "", isResultVisible = false)
    }
}
