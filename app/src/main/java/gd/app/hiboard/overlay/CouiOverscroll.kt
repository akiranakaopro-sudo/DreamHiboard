package gd.app.hiboard.overlay

import kotlin.math.abs
import kotlin.math.exp

/**
 * Oppo / COUI Assist overscroll — same curve as
 * [com.coui.appcompat.animation.COUIPhysicalAnimationUtil.calcRealOverScrollDist].
 *
 * Per-delta resistance grows with stretch; asymptotes at [MAX_FRACTION] of the
 * axis size (no hard wall like a linear ×0.38 clamp).
 */
object CouiOverscroll {
    /**
     * [COUIPhysicalAnimationUtil] OVERFLING_MAX_DISTANCE_SCREEN_FACTOR —
     * soft max stretch as a fraction of width/height.
     */
    const val MAX_FRACTION = 0.3731f

    /** calcRealOverScrollDist as float: `delta * (1 - ratio) / 5 * 2`. */
    fun dampDelta(delta: Float, currentOver: Float, maxOver: Float): Float {
        val max = maxOver.coerceAtLeast(1f)
        val ratio = (abs(currentOver) / max).coerceAtMost(1f)
        return delta * (1f - ratio) / 5f * 2f
    }

    /**
     * Closed form of the COUI delta law when the launcher sends undamped
     * `amount/width` past 1.0 (Oppo [OplusWorkspace.overScroll]).
     *
     * `dv/du = 0.4 * (1 - v/max)` → `v = max * (1 - e^(-0.4 u / max))`.
     */
    fun visualFromUndamped(undampedOver: Float, maxOver: Float): Float {
        if (undampedOver <= 0f) return 0f
        val max = maxOver.coerceAtLeast(1f)
        return max * (1f - exp((-0.4f * undampedOver / max).toDouble()).toFloat())
    }
}
