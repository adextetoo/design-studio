package ng.naijaleague.fantasy.rules

/**
 * Transfers (§05).
 *
 * One free per gameweek, banked up to five, minus four points per extra. The
 * app must always show the cost before a manager confirms, every single time —
 * nobody should ever lose four points by accident.
 */
object Transfers {
    const val FREE_PER_GW = 1
    const val MAX_BANKED = 5
    const val HIT_PER_EXTRA = 4

    /**
     * Free transfers available this gameweek, given what was banked coming in.
     * Banking is capped, so a quiet October pays for a busy December but not for
     * the whole season.
     */
    fun availableThisGameweek(bankedComingIn: Int): Int =
        minOf(bankedComingIn + FREE_PER_GW, MAX_BANKED)

    /**
     * What the manager carries into the next gameweek after making [transfersMade].
     */
    fun bankAfter(bankedComingIn: Int, transfersMade: Int): Int {
        val available = availableThisGameweek(bankedComingIn)
        val unused = (available - transfersMade).coerceAtLeast(0)
        return minOf(unused, MAX_BANKED)
    }

    /**
     * The points hit. Zero while transfers are unlimited — before Gameweek 1,
     * during the January window, and under the Aso Ebi chip.
     */
    fun pointsHit(
        bankedComingIn: Int,
        transfersMade: Int,
        unlimited: Boolean = false
    ): Int {
        if (unlimited) return 0
        val available = availableThisGameweek(bankedComingIn)
        val extra = (transfersMade - available).coerceAtLeast(0)
        return extra * HIT_PER_EXTRA
    }

    /** The confirmation line shown before a manager commits. */
    fun confirmationCopy(bankedComingIn: Int, transfersMade: Int, unlimited: Boolean = false): String {
        if (unlimited) return "Unlimited transfers this gameweek. This one is free."
        val hit = pointsHit(bankedComingIn, transfersMade, false)
        val available = availableThisGameweek(bankedComingIn)
        return when {
            hit == 0 && available - transfersMade > 0 ->
                "Free transfer. You will have ${available - transfersMade} left."
            hit == 0 -> "Free transfer. That is your last free one."
            else -> "This costs $hit points. Confirm only if you mean it."
        }
    }
}
