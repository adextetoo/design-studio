package ng.naijaleague.fantasy.rules

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * The NPFL scoring engine (§05).
 *
 * Two things here exist in no other fantasy game, and both come straight out of
 * the brand system:
 *
 *  1. The Away Day Bonus. In the NPFL home teams win, so backing an away player
 *     is the bravest thing a manager can do and the game pays for it.
 *  2. The differential multiplier, which taxes the template (§08).
 *
 * Order of operations is fixed and documented because it is a real decision the
 * brand system left open:
 *
 *     base -> x differential -> x Ground Man -> round -> x captain
 *
 * The captain multiplier is applied last, to an already-rounded subtotal, so a
 * captained score is always exactly double (or triple) the number the manager
 * can see on the player's own card. Rounding is half away from zero, so a
 * negative score is never quietly softened by the multiplier.
 */
object Scoring {

    /** Away Day Bonus, published so the rules page cannot drift from it (§05). */
    const val AWAY_GOAL_OR_ASSIST_BONUS = 1
    const val AWAY_CLEAN_SHEET_BONUS = 2
    const val SAVES_PER_POINT = 3
    const val CONCEDED_PER_DEDUCTION = 2
    const val ASSIST = 3
    const val PENALTY_SAVED = 5
    const val YELLOW_CARD = -1
    const val RED_CARD = -3
    const val PENALTY_MISSED = -2
    const val OWN_GOAL = -2
    const val APPEARANCE_UNDER_60 = 1
    const val APPEARANCE_60_PLUS = 2

    /** Public so the rules page can render the table from the engine itself. */
    fun goalValue(position: Position) = when (position) {
        Position.GK, Position.DEF -> 6
        Position.MID -> 5
        Position.FWD -> 4
    }

    /** Public so the rules page can render the table from the engine itself. */
    fun cleanSheetValue(position: Position) = when (position) {
        Position.GK, Position.DEF -> 4
        Position.MID -> 1
        Position.FWD -> 0
    }

    private fun roundHalfAwayFromZero(value: Double): Int {
        val magnitude = abs(value).roundToLong()
        return (if (value < 0) -magnitude else magnitude).toInt()
    }

    /** The itemised base score, before any multiplier. */
    fun baseLines(player: Player, perf: Performance): List<PointLine> {
        val lines = mutableListOf<PointLine>()
        val away = perf.venue == Venue.AWAY

        when {
            perf.minutes <= 0 -> Unit
            perf.minutes < 60 -> lines += PointLine("Played ${perf.minutes} mins", 1)
            else -> lines += PointLine("Played ${perf.minutes} mins", 2)
        }

        if (perf.goals > 0) {
            val each = goalValue(player.position)
            lines += PointLine(
                if (perf.goals == 1) "Goal" else "${perf.goals} goals",
                each * perf.goals
            )
            if (away) lines += PointLine("Away Day Bonus - goal", perf.goals)
        }

        if (perf.assists > 0) {
            lines += PointLine(
                if (perf.assists == 1) "Assist" else "${perf.assists} assists",
                3 * perf.assists
            )
            if (away) lines += PointLine("Away Day Bonus - assist", perf.assists)
        }

        // A clean sheet only counts if the player was on the pitch for the hour.
        if (perf.cleanSheet && perf.minutes >= 60) {
            val cs = cleanSheetValue(player.position)
            if (cs > 0) {
                lines += PointLine("Clean sheet", cs)
                if (away && (player.position == Position.GK || player.position == Position.DEF)) {
                    lines += PointLine("Away Day Bonus - clean sheet", 2)
                }
            }
        }

        if (player.position == Position.GK && perf.saves >= 3) {
            lines += PointLine("${perf.saves} saves", perf.saves / 3)
        }
        if (perf.penaltiesSaved > 0) {
            lines += PointLine("Penalty saved", 5 * perf.penaltiesSaved)
        }

        if (player.position == Position.GK || player.position == Position.DEF) {
            if (perf.goalsConceded >= 2) {
                lines += PointLine("${perf.goalsConceded} conceded", -(perf.goalsConceded / 2))
            }
        }

        if (perf.theThree > 0) {
            lines += PointLine("The Three", perf.theThree)
        }

        if (perf.yellowCards > 0) lines += PointLine("Yellow card", -1 * perf.yellowCards)
        if (perf.redCards > 0) lines += PointLine("Red card", -3 * perf.redCards)
        if (perf.penaltiesMissed > 0) lines += PointLine("Penalty missed", -2 * perf.penaltiesMissed)
        if (perf.ownGoals > 0) lines += PointLine("Own goal", -2 * perf.ownGoals)

        return lines
    }

    /**
     * Score one player for one gameweek.
     *
     * @param isCaptain the manager's captain for this gameweek
     * @param activeChip the chip played this gameweek, if any
     */
    fun scorePlayer(
        player: Player,
        perf: Performance,
        isCaptain: Boolean = false,
        activeChip: Chip? = null
    ): PlayerScore {
        val lines = baseLines(player, perf)
        val base = lines.sumOf { it.points }

        val tier = DifferentialTier.forOwnership(player.ownershipPct)
        val groundMan = activeChip == Chip.GROUND_MAN && perf.venue == Venue.HOME
        val captainMultiplier = when {
            !isCaptain -> 1
            activeChip == Chip.JARA -> 3
            else -> 2
        }

        var subtotal = base.toDouble() * tier.multiplier
        if (groundMan) subtotal *= 1.5
        val rounded = roundHalfAwayFromZero(subtotal)

        return PlayerScore(
            playerId = player.id,
            lines = lines,
            basePoints = base,
            differentialTier = tier,
            groundManApplied = groundMan,
            captainMultiplier = captainMultiplier,
            finalPoints = rounded * captainMultiplier
        )
    }

    /**
     * Score a whole squad for a gameweek.
     *
     * Only the starting XI scores, unless the Owambe chip is played — then all
     * 15 do, bench included, because at an owambe nobody sits outside.
     */
    fun scoreSquad(
        squad: Squad,
        performances: Map<String, Performance>,
        activeChip: Chip? = null
    ): GameweekScore {
        val scoring = if (activeChip == Chip.OWAMBE) squad.allPlayers else squad.startingXi
        val scores = scoring.mapNotNull { player ->
            performances[player.id]?.let { perf ->
                scorePlayer(player, perf, isCaptain = player.id == squad.captainId, activeChip = activeChip)
            }
        }
        return GameweekScore(
            playerScores = scores,
            chip = activeChip,
            benchCounted = activeChip == Chip.OWAMBE
        )
    }
}

data class GameweekScore(
    val playerScores: List<PlayerScore>,
    val chip: Chip?,
    val benchCounted: Boolean
) {
    val points: Int get() = playerScores.sumOf { it.finalPoints }

    /** True while any contributing performance is still open to challenge (§05). */
    fun isProvisional(performances: Map<String, Performance>): Boolean =
        playerScores.any { performances[it.playerId]?.provisional == true }
}
