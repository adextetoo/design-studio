package ng.naijaleague.fantasy.rules

/**
 * Chip eligibility (§05).
 *
 * A remaining-uses counter alone cannot express these rules. "One chip per
 * gameweek" is a question about the manager's whole history, not about a single
 * number, and so is a per-half restriction. So eligibility is asked of the
 * history.
 *
 * Kept beside the rules rather than in a screen, so the picker greys a chip out
 * for the same reason the save would refuse it, and the two cannot drift.
 */
object ChipUsage {

    /** Five chips, one of each, across a season. */
    const val USES_PER_CHIP = 1

    /** At most one chip in any gameweek. The rule people most often forget. */
    const val MAX_PER_GAMEWEEK = 1

    data class Play(val chip: Chip, val gameweek: Int)

    data class Decision(val allowed: Boolean, val reasons: List<String> = emptyList())

    /**
     * May this chip be played in this gameweek?
     *
     * Returns every reason it cannot rather than the first, so a manager sees the
     * whole picture instead of clearing one obstacle to find another.
     */
    fun canPlay(chip: Chip, gameweek: Int, history: List<Play> = emptyList()): Decision {
        val reasons = mutableListOf<String>()

        if (gameweek < 1) {
            return Decision(false, listOf("A chip has to be played in a real gameweek."))
        }

        val alreadyUsed = history.count { it.chip == chip }
        if (alreadyUsed >= USES_PER_CHIP) {
            val when_ = history.first { it.chip == chip }.gameweek
            reasons += "You played ${chip.chipName} in gameweek $when_. " +
                "It is one use a season."
        }

        val thisWeek = history.filter { it.gameweek == gameweek }
        if (thisWeek.size >= MAX_PER_GAMEWEEK) {
            reasons += "You already have ${thisWeek.first().chip.chipName} on this " +
                "gameweek. One chip at a time."
        }

        return Decision(reasons.isEmpty(), reasons)
    }

    /** Chips still unplayed, in the order the rules page lists them. */
    fun remaining(history: List<Play>): List<Chip> =
        Chip.entries.filter { chip -> history.none { it.chip == chip } }

    /** The chip on a given gameweek, if the manager played one. */
    fun playedOn(gameweek: Int, history: List<Play>): Chip? =
        history.firstOrNull { it.gameweek == gameweek }?.chip

    /**
     * The chips a manager can play this week, for the picker.
     *
     * Built from [canPlay] rather than from its own logic, so a chip can never
     * be offered here and refused there.
     */
    fun playableIn(gameweek: Int, history: List<Play>): List<Chip> =
        Chip.entries.filter { canPlay(it, gameweek, history).allowed }
}
