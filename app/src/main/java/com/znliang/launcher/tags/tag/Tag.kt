package com.znliang.launcher.tags.tag

import android.graphics.Color
import android.graphics.PointF
import android.view.View
import com.znliang.launcher.tags.tag.Point3DF

/**
 * @description: Tag 类表示一个标签对象，包含三维坐标、二维投影、颜色、缩放比例等属性。
 * @author xiebin04
 * @date 2024/04/24
 * @version 1.0.0
 *
 * @param x 初始 3D 坐标 X 值
 * @param y 初始 3D 坐标 Y 值
 * @param z 初始 3D 坐标 Z 值
 * @param scale 标签缩放比例（用于比较排序）
 * @param popularity 标签热度，用于计算颜色渐变
 */
class Tag @JvmOverloads constructor(
    x: Float = 0f,
    y: Float = 0f,
    z: Float = 0f,
    var scale: Float = 1f,
    var popularity: Int = 0
) : Comparable<Tag> {

    // 绑定的视图，可用于显示标签内容
    var view: View? = null

    // 颜色分量：存储 [alpha, red, green, blue]，取值范围均为 0~1
    // 默认 alpha 为 1.0，RGB 默认值均为 0.5
    private val colorComponents = floatArrayOf(1f, 0.5f, 0.5f, 0.5f)

    // 标签的二维平面投影中心点，用于布局时计算位置
    private val flatCenter = PointF()

    // 标签的三维空间中心点，用于计算 3D 旋转等操作
    private val spatialCenter = Point3DF(x, y, z)

    // 提供 spatialCenter 各轴的读写访问
    var spatialX: Float
        get() = spatialCenter.x
        set(value) { spatialCenter.x = value }

    var spatialY: Float
        get() = spatialCenter.y
        set(value) { spatialCenter.y = value }

    var spatialZ: Float
        get() = spatialCenter.z
        set(value) { spatialCenter.z = value }

    // 提供 flatCenter 各轴的读写访问
    var flatX: Float
        get() = flatCenter.x
        set(value) { flatCenter.x = value }

    var flatY: Float
        get() = flatCenter.y
        set(value) { flatCenter.y = value }

    /**
     * 设置颜色分量数组。
     *
     * @param components 长度为 4 的浮点数数组，依次为 [alpha, red, green, blue]，
     *                   数值范围为 0~1。要求数组长度必须为 4，否则不会更新。
     */
    fun setColorComponents(components: FloatArray?) {
        if (components != null && components.size == colorComponents.size) {
            components.copyInto(colorComponents)
        }
    }

    /**
     * 标签的 alpha（透明度）分量，取值范围 0~1。
     */
    var alpha: Float
        get() = colorComponents[0]
        set(value) { colorComponents[0] = value }

    /**
     * 获取标签颜色的 ARGB 整数值。
     *
     * 计算过程：
     * - 将 0~1 之间的浮点数转换为 0~255 之间的整数。
     * - 通过 Color.argb() 方法组合成 ARGB 格式的颜色值。
     */
    val color: Int
        get() = Color.argb(
            (colorComponents[0] * 255).toInt(),
            (colorComponents[1] * 255).toInt(),
            (colorComponents[2] * 255).toInt(),
            (colorComponents[3] * 255).toInt()
        )

    /**
     * 根据标签的缩放比例进行比较排序，缩放比例小的在前。
     */
    override fun compareTo(other: Tag): Int = scale.compareTo(other.scale)
}
