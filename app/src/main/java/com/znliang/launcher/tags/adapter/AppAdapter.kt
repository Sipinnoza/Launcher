package com.znliang.launcher.tags.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.znliang.launcher.tags.listener.ITagClickListener
import com.znliang.launcher.tags.model.AppInfo

class AppAdapter(private var tags: List<AppInfo>, private val onTagClickListener: ITagClickListener? = null) : TagsAdapter() {

    private val viewCache = mutableMapOf<String, AppItemViewHolder>()

    override val count: Int
        get() = tags.size

    override fun getView(context: Context?, position: Int, parent: ViewGroup?): View {
        val ctx = context ?: return View(parent?.context)
        val app = tags[position]

        viewCache[app.packageName]?.let { cachedView ->
            return cachedView.getView()
        }

        val holder = AppItemViewHolder.create(ctx)
        holder.bind(app)

        addListener(holder, app)
        viewCache[app.packageName] = holder

        return holder.getView()
    }

    override fun getItem(position: Int): Any = tags[position]

    override fun getPopularity(position: Int): Int = position

    override fun onThemeColorChanged(view: View?, themeColor: Int, alpha: Float) {
        view?.alpha = alpha
    }

    override fun updateView(list: List<AppInfo>) {
        list.forEach { tag ->
            viewCache[tag.packageName]?.bind(tag)
        }
    }


    /**
     * 为 Tag 视图添加点击事件监听器（仅在未设置的情况下）。
     */
    private fun addListener(hodler: AppItemViewHolder, app: AppInfo) {
        val view = hodler.getView()
        if (!view.hasOnClickListeners()) {
            onTagClickListener?.let { listener ->
                view.setOnClickListener {
                    listener.onItemClick(app)
                }
            }
        }

        if (!view.hasOnLongClickListeners()) {
            onTagClickListener?.let { listener ->
                view.setOnLongClickListener {
                    listener.onItemLongPress(app)
                    true
                }
            }
        }
    }
}
