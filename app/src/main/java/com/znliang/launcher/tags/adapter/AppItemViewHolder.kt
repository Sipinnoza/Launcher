package com.znliang.launcher.tags.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.znliang.launcher.R
import com.znliang.launcher.tags.model.AppInfo


class AppItemViewHolder(private val itemView: View){

    private val imageView: ImageView = itemView.findViewById(R.id.app_icon)
    private val textView: TextView = itemView.findViewById(R.id.app_label)

    fun bind(app: AppInfo) {
        imageView.setImageDrawable(app.icon)
        textView.text = app.label

        val color = getPopularityColor(app.popularity)

        if (color != Color.TRANSPARENT) {
            val cornerRadiusDp = 16f
            val cornerRadiusPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                cornerRadiusDp,
                itemView.context.resources.displayMetrics
            )

            // 创建径向渐变的圆角背景
            val bgDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = cornerRadiusPx
                gradientType = GradientDrawable.RADIAL_GRADIENT
                gradientRadius = cornerRadiusPx
                setGradientCenter(0.5f, 0.5f)
                colors = intArrayOf(color, Color.TRANSPARENT)
            }
            itemView.background = bgDrawable
        } else {
            itemView.background = null
        }
    }

    fun getView(): View = itemView

    /**
     * 根据流行度获取颜色。
     */
    fun getPopularityColor(popularity: Int): Int {
        return when {
            popularity == 0 -> Color.TRANSPARENT
            popularity <= 5 -> interpolateColor(Color.GRAY, Color.BLUE, popularity / 5f)
            popularity <= 20 -> interpolateColor(Color.BLUE, Color.GREEN, (popularity - 5) / 15f)
            popularity <= 50 -> interpolateColor(Color.GREEN, Color.YELLOW, (popularity - 20) / 30f)
            popularity <= 100 -> interpolateColor(Color.YELLOW, Color.RED, (popularity - 50) / 50f)
            else -> Color.RED
        }
    }

    /**
     * 在两个颜色之间线性插值。
     */
    fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        return ColorUtils.blendARGB(startColor, endColor, fraction.coerceIn(0f, 1f))
    }

    companion object {
        fun create(context: Context): AppItemViewHolder {
            val resources = context.resources
            val iconSize = resources.getDimensionPixelSize(R.dimen.app_icon_size)
            val itemSize = resources.getDimensionPixelSize(R.dimen.app_item_size)
            val iconMarginBottom = resources.getDimensionPixelSize(R.dimen.app_icon_margin_bottom)
            val textSizeSp = resources.getDimension(R.dimen.app_text_size) / resources.displayMetrics.scaledDensity
            val ems = resources.getInteger(R.integer.app_text_max_ems)
            val textColor = resources.getColor(R.color.white, context.theme)

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = ViewGroup.LayoutParams(itemSize, itemSize)
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
