package com.znliang.launcher.tags.tag

import kotlin.math.*

class TagCloud(
    private val tagList: MutableList<Tag>,
    private var radius: Int,
    private var lightColor: FloatArray,
    private var darkColor: FloatArray
) {
    // 旋转计算参数
    private var sinX = 0f
    private var cosX = 0f
    private var sinY = 0f
    private var cosY = 0f
    private var sinZ = 0f
    private var cosZ = 0f

    // 惯性参数（Z 轴保持不变）
    private var inertiaX = 0f
    private var inertiaY = 0f
    private val inertiaZ = 0f

    // popularity 范围
    private var minPopularity = 0
    private var maxPopularity = 0

    @JvmOverloads
    constructor(radius: Int = DEFAULT_RADIUS) : this(mutableListOf(), radius)
    constructor(tags: MutableList<Tag>, radius: Int) : this(
        tags,
        radius,
        DEFAULT_COLOR_DARK,
        DEFAULT_COLOR_LIGHT
    )

    fun clear() {
        tagList.clear()
    }

    fun getTagList(): List<Tag> = tagList

    operator fun get(position: Int): Tag = tagList[position]

    /**
     * 更新标签云（仅当惯性足够大时更新旋转与投影）
     */
    fun update() {
        if (abs(inertiaX) > 0.1f || abs(inertiaY) > 0.1f) {
            recalculateAngle()
            updateAll()
        }
    }

    // 初始化标签颜色（根据标签的 popularity 调整颜色渐变）
    private fun initTag(tag: Tag) {
        val percentage = getPercentage(tag)
        val argb = getColorFromGradient(percentage)
        tag.setColorComponents(argb)
    }

    // 根据标签的 popularity 计算一个百分比
    private fun getPercentage(tag: Tag): Float {
        val p = tag.popularity
        return if (minPopularity == maxPopularity) 1.0f
        else (p.toFloat() - minPopularity) / (maxPopularity - minPopularity)
    }

    /**
     * 添加新标签：初始化颜色、定位后加入列表，并刷新显示
     */
    fun add(newTag: Tag) {
        initTag(newTag)
        position(newTag)
        tagList.add(newTag)
        updateAll()
    }

    /**
     * 随机定位新标签（非重构模式下使用）
     */
    private fun position(newTag: Tag) {
        val phi = Math.random() * PI
        val theta = Math.random() * (2 * PI)
        newTag.spatialX = (radius * cos(theta) * sin(phi)).toFloat()
        newTag.spatialY = (radius * sin(theta) * sin(phi)).toFloat()
        newTag.spatialZ = (radius * cos(phi)).toFloat()
    }

    /**
     * 更新所有标签：旋转、投影、计算缩放及 alpha 值
     */
    private fun updateAll() {
        val diameter = (2 * radius).toFloat()
        // 第一遍：更新旋转后的坐标、计算投影与 scale，同时收集每个标签的 delta 值
        val deltaValues = tagList.map { tag ->
            val (rx, ry, rz) = applyRotation(tag.spatialX, tag.spatialY, tag.spatialZ)
            tag.spatialX = rx
            tag.spatialY = ry
            tag.spatialZ = rz

            val per = diameter / (diameter + rz)
            tag.flatX = rx * per
            tag.flatY = ry * per
            tag.scale = per

            diameter + rz // 此值用于计算 alpha
        }

        // 第二遍：根据所有标签的 delta 值计算全局最值，更新每个标签的透明度
        val maxDelta = deltaValues.maxOrNull() ?: 0f
        val minDelta = deltaValues.minOrNull() ?: 0f
        tagList.forEachIndexed { index, tag ->
            val delta = deltaValues[index]
            tag.alpha = if (maxDelta != minDelta) 1 - (delta - minDelta) / (maxDelta - minDelta) else 1f
        }

        sortTagByScale()
    }

    /**
     * 根据当前惯性值，计算旋转后的三维坐标
     */
    private fun applyRotation(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
        // 绕 X 轴旋转
        val ry1 = y * cosX - z * sinX
        val rz1 = y * sinX + z * cosX
        // 绕 Y 轴旋转
        val rx2 = x * cosY + rz1 * sinY
        val rz2 = -x * sinY + rz1 * cosY
        // 绕 Z 轴旋转
        val rx3 = rx2 * cosZ - ry1 * sinZ
        val ry3 = rx2 * sinZ + ry1 * cosZ
        return Triple(rx3, ry3, rz2)
    }

    /**
     * 根据给定百分比计算颜色渐变
     * 第一个分量固定为 1f，其余分量在 darkColor 与 lightColor 间插值
     */
    private fun getColorFromGradient(percentage: Float): FloatArray = FloatArray(4) { index ->
        when (index) {
            0 -> 1f
            else -> percentage * darkColor[index - 1] + (1f - percentage) * lightColor[index - 1]
        }
    }

    /**
     * 根据当前 inertia 值（角度，单位为度）计算旋转所需的三角函数值
     */
    private fun recalculateAngle() {
        val degToRad = PI / 180
        sinX = sin(inertiaX * degToRad).toFloat()
        cosX = cos(inertiaX * degToRad).toFloat()
        sinY = sin(inertiaY * degToRad).toFloat()
        cosY = cos(inertiaY * degToRad).toFloat()
        sinZ = sin(inertiaZ * degToRad).toFloat()
        cosZ = cos(inertiaZ * degToRad).toFloat()
    }

    fun setRadius(radius: Int) {
        this.radius = radius
    }

    fun setTagColorLight(tagColor: FloatArray) {
        lightColor = tagColor
    }

    fun setTagColorDark(tagColorDark: FloatArray) {
        darkColor = tagColorDark
    }

    fun setInertia(x: Float, y: Float) {
        inertiaX = x
        inertiaY = y
    }

    /**
     * 使用黄金角分布方法将所有标签均匀分布在球面上
     */
    fun createSurfaceDistribution() {
        val count = tagList.size
        if (count == 0) return
        val goldenRatio = (1 + sqrt(5.0)) / 2.0
        tagList.forEachIndexed { i, tag ->
            val theta = acos(1 - 2.0 * i / (count - 1))
            val phi = i * goldenRatio * 2 * PI
            tag.spatialX = (radius * sin(theta) * cos(phi)).toFloat()
            tag.spatialY = (radius * sin(theta) * sin(phi)).toFloat()
            tag.spatialZ = (radius * cos(theta)).toFloat()
            updateTagProjection(tag)
        }
    }

    /**
     * 根据标签三维坐标更新其二维投影和缩放比例
     */
    private fun updateTagProjection(tag: Tag) {
        val depthFactor = (radius + tag.spatialZ) / (2 * radius)
        tag.flatX = tag.spatialX * depthFactor
        tag.flatY = tag.spatialY * depthFactor
        tag.scale = depthFactor.coerceIn(0.5f, 1.5f)
    }

    private fun sortTagByScale() {
        tagList.sort()
    }

    companion object {
        private const val DEFAULT_RADIUS = 3
        private val DEFAULT_COLOR_DARK = floatArrayOf(0.886f, 0.725f, 0.188f, 1f)
        private val DEFAULT_COLOR_LIGHT = floatArrayOf(0.3f, 0.3f, 0.3f, 1f)
    }
}
