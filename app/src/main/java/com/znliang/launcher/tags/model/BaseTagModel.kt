package com.znliang.launcher.tags.model

import android.graphics.drawable.Drawable
import org.json.JSONObject

interface ITagModel {
    val id: String?
    val name: String?
    val layout: String?
}

data class BaseTagModel(val data: JSONObject) : ITagModel {
    override val id: String? = data.optString("id")
    override val name: String? = data.optString("name")
    override val layout: String? = data.optString("layout")
}

data class AppInfo(
    val label: CharSequence,
    val packageName: String,
    val className: String,
    val icon: Drawable,
    override val id: String? = null,
    override val name: String? = null,
    override val layout: String? = null
) : ITagModel