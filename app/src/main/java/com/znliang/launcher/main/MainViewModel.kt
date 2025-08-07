package com.znliang.launcher.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.znliang.launcher.appdata.AppLoader
import com.znliang.launcher.R
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
            is MainIntent.LongPress -> {
                handleLongPress(
                    app = intent.app,
                    context = intent.context,
                    anchor = intent.anchor)
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            appLoader.applyDefaultBlackList()
            appLoader.loadAppsAndStore()
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
            _state.value = _state.value.copy(
                searchQuery = "",
                isResultVisible = false,
                isSearchVisible = false
            )
            app.popularity += 1
            appLoader.markAppOpened(app.packageName)
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
        _state.value = _state.value.copy(
            isSearchVisible = tagZoomIn,
            searchQuery = "",
            isResultVisible = false
        )
    }

    private fun handleLongPress(app: AppInfo, context: Context, anchor: View) {
        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.app_long_press_menu, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_uninstall -> {
                    val packageName = app.packageName
                    Log.e("Launcher", "Try uninstall: $packageName")

                    if (packageName == context.packageName) {
                        Toast.makeText(context, "不能卸载 Launcher 自己", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }

                    try {
                        val intent = Intent(Intent.ACTION_DELETE).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        context.startActivity(intent)
                        Log.e("Launcher", "startActivity uninstall: $packageName")
                    } catch (e: Exception) {
                        Log.e("Launcher", "卸载失败: ${e.message}")
                        Toast.makeText(context, "卸载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    true
                }

                R.id.menu_add_widget -> {
                    Toast.makeText(context, "menu_uninstall尚未实现", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }

        popup.show()
    }
}
