package com.znliang.launcher.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.znliang.launcher.appdata.AppLoader

class MainViewModelFactory(private val appLoader: AppLoader) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(appLoader) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
