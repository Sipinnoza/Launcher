package com.znliang.launcher.tags.listener

import android.view.View
import android.view.ViewGroup

/**
 * @description:
 * @author xiebin04
 * @date 2025/03/13
 * @version
 */
interface ITagClickListener {
    fun onItemClick(parent: ViewGroup?, view: View?, position: Int)
}