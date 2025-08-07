package com.znliang.launcher.tags.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.znliang.launcher.tags.model.AppInfo

abstract class TagsAdapter {

    abstract val count: Int

    abstract fun getView(context: Context?, position: Int, parent: ViewGroup?): View?

    abstract fun getItem(position: Int): Any?

    abstract fun getPopularity(position: Int): Int

    abstract fun onThemeColorChanged(view: View?, themeColor: Int, alpha: Float)

    abstract fun updateView(list: List<AppInfo>)
}

