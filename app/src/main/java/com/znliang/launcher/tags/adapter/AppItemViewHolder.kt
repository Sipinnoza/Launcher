package com.znliang.launcher.tags.adapter

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.znliang.launcher.R
import com.znliang.launcher.tags.model.AppInfo


class AppItemViewHolder(private val itemView: View){

    private val imageView: ImageView = itemView.findViewById(R.id.app_icon)
    private val textView: TextView = itemView.findViewById(R.id.app_label)

    fun bind(app: AppInfo) {
        imageView.setImageDrawable(app.icon)
        textView.text = app.label
    }

    fun getView(): View = itemView

    companion object {
        fun create(context: Context): AppItemViewHolder {
            val resources = context.resources
            val iconSize = resources.getDimensionPixelSize(R.dimen.app_icon_size)
            val iconMarginBottom = resources.getDimensionPixelSize(R.dimen.app_icon_margin_bottom)
            val textSizeSp = resources.getDimension(R.dimen.app_text_size) / resources.displayMetrics.scaledDensity
            val ems = resources.getInteger(R.integer.app_text_max_ems)
            val textColor = resources.getColor(R.color.white, context.theme)

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                isClickable = true
                isFocusable = true
            }

            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                    bottomMargin = iconMarginBottom
                }
                id = R.id.app_icon
            }

            val textView = TextView(context).apply {
                setTextColor(textColor)
                textSize = textSizeSp
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                maxEms = ems
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                id = R.id.app_label
            }
            layout.addView(imageView)
            layout.addView(textView)

            return AppItemViewHolder(layout)
        }
    }
}
