package com.znliang.launcher.tags.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.znliang.launcher.tags.model.AppInfo

class AppAdapter(
    private var tags: List<AppInfo>,
    private val onAppClick: (app: AppInfo) -> Unit
) : TagsAdapter() {

    private val viewCache = mutableMapOf<AppInfo, View>()

    override val count: Int
        get() = tags.size

    override fun getView(context: Context?, position: Int, parent: ViewGroup?): View {
        val ctx = context ?: return View(parent?.context)
        val app = tags[position]

        viewCache[app]?.let { cachedView ->
            return cachedView
        }

        val holder = AppItemViewHolder.create(ctx)
        holder.bind(app, onAppClick)

        val newView = holder.getView()
        viewCache[app] = newView

        return newView
    }

    override fun getItem(position: Int): Any = tags[position]

    override fun getPopularity(position: Int): Int = position

    override fun onThemeColorChanged(view: View?, themeColor: Int, alpha: Float) {
        view?.alpha = alpha
    }
}
