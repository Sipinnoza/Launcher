package com.znliang.launcher.utils

import android.view.View

class DoubleClickListener(
    private val doubleClickThreshold: Long = 300L,
    private val onDoubleClick: () -> Unit
) : View.OnClickListener {
    private var lastClickTime = 0L

    override fun onClick(v: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime <= doubleClickThreshold) {
            onDoubleClick()
        }
        lastClickTime = currentTime
    }
}
