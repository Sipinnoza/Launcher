package com.znliang.launcher.tags.listener

import android.view.View
import android.view.ViewGroup
import com.znliang.launcher.tags.model.AppInfo

/**
 * @description:
 * @author xiebin04
 * @date 2025/03/13
 * @version
 */
interface ITagClickListener {

    fun onItemClick(app: AppInfo)

    fun onItemLongPress(app: AppInfo)
}