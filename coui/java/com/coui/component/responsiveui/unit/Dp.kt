package com.coui.component.responsiveui.unit

import android.content.Context

/** An immutable density-independent pixel value. */
data class Dp(val value: Float) : Comparable<Dp> {

    fun toPixel(context: Context): Float =
        value * context.resources.displayMetrics.density

    override fun compareTo(other: Dp): Int = value.compareTo(other.value)

    operator fun plus(other: Dp): Dp = Dp(value + other.value)

    operator fun minus(other: Dp): Dp = Dp(value - other.value)

    operator fun div(other: Dp): Dp = Dp(value / other.value)

    override fun toString(): String = "$value dp"

    companion object {
        fun pixel2Dp(context: Context, pixel: Int): Dp = pixel.pixel2Dp(context)
    }
}

/** Creates a density-independent value from an integer DP amount. */
val Int.dp: Dp
    get() = Dp(toFloat())

/** Converts a physical-pixel amount to density-independent pixels. */
fun Int.pixel2Dp(context: Context): Dp =
    Dp(this / context.resources.displayMetrics.density)
