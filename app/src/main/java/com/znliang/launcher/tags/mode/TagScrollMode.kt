package com.znliang.launcher.tags.mode

import androidx.annotation.IntDef

/**
 * @description:
 * @author xiebin04
 * @date 2025/03/12
 * @version
 */

@IntDef(TagScrollMode.MODE_DECELERATE, TagScrollMode.MODE_DISABLE, TagScrollMode.MODE_UNIFORM)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
annotation class TagScrollMode {
    companion object {
        const val MODE_DISABLE = 0
        const val MODE_DECELERATE = 1
        const val MODE_UNIFORM = 2
    }
}