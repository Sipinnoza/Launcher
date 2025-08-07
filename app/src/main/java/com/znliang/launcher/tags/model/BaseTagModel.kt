package com.znliang.launcher.tags.model

import android.graphics.drawable.Drawable

interface ITagModel {
    val id: String?
    val name: String?
    val layout: String?
}

data class AppInfo(
    val label: CharSequence,
    val packageName: String,
    val className: String,
    val icon: Drawable,
    var popularity: Int = 0,
    override val id: String? = null,
    override val name: String? = null,
    override val layout: String? = null
) : ITagModel