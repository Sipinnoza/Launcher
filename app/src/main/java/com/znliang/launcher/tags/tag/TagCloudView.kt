
package com.znliang.launcher.tags.tag

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import com.znliang.launcher.tags.adapter.TagsAdapter
import com.znliang.launcher.tags.mode.TagScrollMode
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sqrt

/**
 * @description: TagCloudView 用于显示一个旋转的标签云，通过触摸控制惯性滚动，同时支持自动滚动模式（减速或匀速）。
 * @author xiebin04
 * @date 2024/04/24
 * @version 1.0.0
 */
class TagCloudView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), Runnable {

    // 用于存储颜色信息（0~1之间的浮点数）
    private data class TagColor(val r: Float, val g: Float, val b: Float, val a: Float)

    // 基本配置参数
    private var speed: Float = DEFAULT_SPEED
    private var tagCloud: TagCloud? = null
    private var inertiaX: Float = DEFAULT_INERTIA
    private var inertiaY: Float = DEFAULT_INERTIA
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private var radiusPercent: Float = DEFAULT_RADIUS_PERCENT
    private var darkColor: TagColor = TagColor(0f, 0f, 0f, 0f)
    private var lightColor: TagColor = TagColor(1f, 1f, 1f, 0f)

    // 滑动控制模式：手动滑动以及自动滚动模式
    private var manualScroll: Boolean = true
    private var autoScrollMode: @TagScrollMode Int = TagScrollMode.MODE_UNIFORM
    private var minSize: Int = 0
    private var isOnTouch: Boolean = false
    private val choreographer = Choreographer.getInstance()
    private val frameCallback = Choreographer.FrameCallback { run() }
    private var adapter: TagsAdapter? = null
    private var downX: Float = 0f
    private var downY: Float = 0f
    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop

    private var isPaused: Boolean = false

    init {
        setFocusableInTouchMode(true)
        tagCloud = TagCloud()
        initMinSize(context)
    }

    fun getAdapter() = adapter

    /**
     * 根据当前窗口尺寸，计算最小尺寸
     */
    private fun initMinSize(context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val bounds = wm.currentWindowMetrics.bounds
        minSize = min(bounds.width(), bounds.height())
    }

    /**
     * 设置适配器，并初始化数据
     */
    fun setAdapter(newAdapter: TagsAdapter) {
        if (adapter !== newAdapter) {
            adapter = newAdapter
        }
        initWithAdapter()
    }

    /**
     * 使用适配器数据初始化 TagCloud，
     * 并计算每个 Tag 的位置、颜色、投影（2D）及缩放比例。
     */
    private fun initWithAdapter() {
        radiusPercent = 0.7f
        post {
            // 计算中心点与半径
            centerX = width / 2f
            centerY = height / 2f
            radius = min(centerX, centerY) * radiusPercent

            tagCloud?.apply {
                setRadius(radius.toInt())
                setTagColorLight(floatArrayOf(lightColor.r, lightColor.g, lightColor.b, lightColor.a))
                setTagColorDark(floatArrayOf(darkColor.r, darkColor.g, darkColor.b, darkColor.a))
                clear()
                adapter?.let { adapter ->
                    // 遍历所有标签数据，初始化 Tag 并绑定 View
                    for (i in 0 until adapter.count) {
                        val tag = Tag(popularity = adapter.getPopularity(i)).apply {
                            view = adapter.getView(context, i, this@TagCloudView)
                        }
                        add(tag)
                    }
                }
                setInertia(inertiaX, inertiaY)
                // 使用黄金角分布算法，将标签均匀分布在球面上
                createSurfaceDistribution()
            }
            refreshChildrenViews()
            layoutChildren()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(minSize, widthMeasureSpec)
        val height = resolveSize(minSize, heightMeasureSpec)
        setMeasuredDimension(width, height)
        // UNSPECIFIED 模式下测量所有子 View，必要时可根据需求调整
        measureChildren(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (radius <= 0f || tagCloud == null) return
        layoutChildren()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!manualScroll) return false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                isOnTouch = true
                // 请求父控件不拦截触摸事件
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                // 如果移动距离超过 touchSlop，则拦截事件
                if ((ev.x - downX).absoluteValue > touchSlop || (ev.y - downY).absoluteValue > touchSlop) {
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isOnTouch = false
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!manualScroll) return false
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = e.x
                downY = e.y
                isOnTouch = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = e.x - downX
                val dy = e.y - downY
                // 仅在移动距离足够大时更新惯性
                if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                    updateInertia(dx, dy)
                    processTouch()
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isOnTouch = false
                return true
            }
        }
        return false
    }

    /**
     * 根据触摸位移计算更新惯性参数。
     * 计算公式：inertia = (位移 / 半径) * 速度 * 缩放因子
     */
    private fun updateInertia(dx: Float, dy: Float) {
        inertiaX = (dy / radius * speed * TOUCH_SCALE_FACTOR).coerceIn(-MAX_INERTIA, MAX_INERTIA)
        inertiaY = (-dx / radius * speed * TOUCH_SCALE_FACTOR).coerceIn(-MAX_INERTIA, MAX_INERTIA)
    }

    /**
     * 根据当前惯性更新 TagCloud 状态（旋转、投影计算）并刷新视图
     */
    private fun processTouch() {
        tagCloud?.apply {
            setInertia(inertiaX, inertiaY)
            update()
        }
        layoutChildren()
    }

    private fun refreshChildrenViews() {
        val tags = tagCloud?.getTagList() ?: return
        if (childCount != tags.size) {
            removeAllViews()
            tags.forEachIndexed { index, tag ->
                tag.view?.let {
                    (it.parent as? ViewGroup)?.removeView(it)
                    addView(it)
                }
            }
        } else {
            // 子View数量匹配时，检查并补充缺失的View
            tags.forEachIndexed { index, tag ->
                val child = getChildAt(index)
                if (child != tag.view) {
                    if (child != null) removeViewAt(index)
                    tag.view?.let {
                        (it.parent as? ViewGroup)?.removeView(it)
                        addView(it, index)
                    }
                }
            }
        }
    }


    private fun layoutChildren() {
        val tags = tagCloud?.getTagList() ?: return
        for (tag in tags) {
            val child = tag.view ?: continue
            if (child.visibility != VISIBLE) continue

            child.isEnabled = tag.alpha >= 0.85f
            adapter?.onThemeColorChanged(child, tag.color, tag.alpha)

            if (abs(child.scaleX - tag.scale) > 0.01f) {
                child.scaleX = tag.scale
                child.scaleY = tag.scale
                child.translationZ = tag.scale
            }

            val left = (centerX + tag.flatX - child.measuredWidth / 2).toInt()
            val top = (centerY + tag.flatY - child.measuredHeight / 2).toInt()
            val right = left + child.measuredWidth
            val bottom = top + child.measuredHeight

            if (abs(child.left - left) > 1 || abs(child.top - top) > 1) {
                child.layout(left, top, right, bottom)
            }
        }
    }



    /**
     * 每一帧更新回调：
     * 如果处于自动滚动模式且当前未触摸屏幕，
     * 根据不同自动滚动模式，调整惯性参数，并触发标签更新。
     */
    override fun run() {
        if (isPaused) {
            return
        }

        if (!isOnTouch && autoScrollMode != TagScrollMode.MODE_DISABLE) {
            when (autoScrollMode) {
                // MODE_DECELERATE 模式：逐步衰减惯性
                TagScrollMode.MODE_DECELERATE -> {
                    inertiaX = updateInertiaWithDeceleration(inertiaX)
                    inertiaY = updateInertiaWithDeceleration(inertiaY)
                }
                // MODE_UNIFORM 模式：逐步调整惯性趋向固定自动滚动速度
                TagScrollMode.MODE_UNIFORM -> {
                    val adjusted = adjustInertiaToFixedSpeed(inertiaX, inertiaY)
                    inertiaX = adjusted.first
                    inertiaY = adjusted.second
                }
            }
            processTouch()
        }
        choreographer.postFrameCallback(frameCallback)
    }

    /**
     * 调整当前惯性，使其逐步趋向固定自动滚动速度（目标速度）。
     *
     * 算法说明：
     * 1. 计算当前惯性向量的大小（速度）。
     * 2. 如果当前速度为 0，则直接返回目标速度（默认 X 轴正方向）。
     * 3. 计算当前速度与目标速度的差值，并以 INERTIA_ADJUST_STEP 为步长进行调整。
     * 4. 根据新的速度和原始方向计算新的惯性向量。
     */
    private fun adjustInertiaToFixedSpeed(currentX: Float, currentY: Float): Pair<Float, Float> {
        val currentSpeed = sqrt(currentX * currentX + currentY * currentY)
        val targetSpeed = FIXED_AUTO_SCROLL_SPEED
        if (currentSpeed == 0f) return Pair(targetSpeed, 0f)
        // 差值 diff > 0 表示当前速度大于目标速度
        val diff = currentSpeed - targetSpeed
        val adjustment = kotlin.math.sign(diff) * min(abs(diff), INERTIA_ADJUST_STEP)
        val newSpeed = currentSpeed - adjustment
        // 计算速度比例，保持原始方向不变
        val ratio = newSpeed / currentSpeed
        return Pair(currentX * ratio, currentY * ratio)
    }

    /**
     * 根据阈值对惯性进行衰减：
     * 如果惯性超过正阈值，则减小；低于负阈值，则增加。
     */
    private fun updateInertiaWithDeceleration(inertia: Float): Float {
        return when {
            inertia > DECELERATION_THRESHOLD -> inertia - DECELERATION_STEP
            inertia < -DECELERATION_THRESHOLD -> inertia + DECELERATION_STEP
            else -> inertia
        }
    }

    companion object {
        private const val DEFAULT_SPEED = 2f
        private const val DEFAULT_INERTIA = 0.5f
        private const val DEFAULT_RADIUS_PERCENT = 0.7f
        private const val TOUCH_SCALE_FACTOR = 0.4f
        // 固定自动滚动速度，即目标惯性
        private const val FIXED_AUTO_SCROLL_SPEED = 0.2f
        // 每帧调整惯性步长（可调节平滑度）
        private const val INERTIA_ADJUST_STEP = 0.005f
        // 衰减惯性时的阈值与步长
        private const val DECELERATION_THRESHOLD = 0.04f
        private const val DECELERATION_STEP = 0.02f
        private const val MAX_INERTIA = 1f
    }
}
