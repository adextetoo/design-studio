package ng.naijaleague.fantasy.brand

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

/**
 * Motion — brand system v1.1, section 13.
 *
 * Four rules, and the fourth one is the one that gets broken: on a three-year-old
 * Android handset a 600ms flourish is not delight, it is lag.
 */
object BrandMotion {

    /**
     * The Uli curve. One shared easing across the whole product: fast out, long
     * settle, no bounce. Bounce is a toy; this is an argument.
     */
    val UliCurve = CubicBezierEasing(0.22f, 0.9f, 0.3f, 1f)

    /** The ceiling. Nothing animates for longer than this, ever. */
    const val MAX_DURATION_MS = 240

    const val QUICK_MS = 120
    const val STANDARD_MS = 200
    const val ENTER_MS = MAX_DURATION_MS

    fun <T> quick() = tween<T>(durationMillis = QUICK_MS, easing = UliCurve)
    fun <T> standard() = tween<T>(durationMillis = STANDARD_MS, easing = UliCurve)
    fun <T> enter() = tween<T>(durationMillis = ENTER_MS, easing = UliCurve)

    /**
     * How long a points total takes to count up. Points count, they never fade —
     * a number arriving is the emotional payload of the entire product, and the
     * Away Day Bonus lands as a separate second beat so the manager feels the extra.
     */
    const val COUNT_UP_MS = 700
    const val AWAY_BONUS_BEAT_DELAY_MS = 260
}
