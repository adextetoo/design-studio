package ng.naijaleague.fantasy.rules

/**
 * Domain model for the NaijaLeague Fantasy game.
 *
 * Pure Kotlin on purpose: no Android imports anywhere in this package, so the
 * rules that make this product different from FPL can be unit-tested on the JVM
 * without an emulator. Brand system references below point at sections of the
 * NaijaLeague Fantasy brand system v1.1.
 */

enum class Position(val label: String, val short: String) {
    GK("Goalkeeper", "GK"),
    DEF("Defender", "DEF"),
    MID("Midfielder", "MID"),
    FWD("Forward", "FWD")
}

/** The NPFL is a home-win league, so venue is a first-class concept here (§05, §08). */
enum class Venue { HOME, AWAY }

data class Club(
    val id: String,
    val name: String,
    val shortName: String,
    val city: String
)

data class Player(
    val id: String,
    val name: String,
    val clubId: String,
    val position: Position,
    /** Whole naira. Integer money only — no floating point anywhere near a price. */
    val priceNaira: Long,
    /** Ownership locked at the deadline and held for the gameweek (§08). */
    val ownershipPct: Double
)

/**
 * Differential reward (§08, "Jara on differentials").
 *
 * The mechanic that makes the brave pick mathematically correct instead of
 * merely romantic, and the direct fix for template tyranny.
 */
enum class DifferentialTier(val multiplier: Double, val label: String) {
    NONE(1.0, ""),
    UNDER_5(1.25, "Differential x1.25"),
    UNDER_2(1.5, "Differential x1.5");

    companion object {
        fun forOwnership(ownershipPct: Double): DifferentialTier = when {
            ownershipPct < 2.0 -> UNDER_2
            ownershipPct < 5.0 -> UNDER_5
            else -> NONE
        }
    }
}

/**
 * Chips (§05). Five a season, one per gameweek.
 * Names are Nigerian because the mechanics are Nigerian.
 */
enum class Chip(val chipName: String, val explanation: String) {
    JARA("Jara", "Your captain scores triple instead of double."),
    OWAMBE("Owambe", "All 15 players score, bench included."),
    ASO_EBI("Aso Ebi", "Unlimited transfers for one gameweek."),
    WAKA_PASS("Waka Pass", "One gameweek with a different squad, then your old one returns."),
    GROUND_MAN("Ground Man", "Every player of yours at home this gameweek gets a 1.5x multiplier.")
}

/**
 * One player's raw match record, as filed by the scorer at the ground (§05).
 * Provisional until Monday 12:00 — the two-hour public challenge window is the
 * trust mechanism that makes thin NPFL data survivable.
 */
data class Performance(
    val playerId: String,
    val venue: Venue,
    val minutes: Int,
    val goals: Int = 0,
    val assists: Int = 0,
    val cleanSheet: Boolean = false,
    val saves: Int = 0,
    val penaltiesSaved: Int = 0,
    val penaltiesMissed: Int = 0,
    val goalsConceded: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val ownGoals: Int = 0,
    /** The Three: 3, 2 or 1 for the best performers in the match; 0 otherwise. */
    val theThree: Int = 0,
    /** The scorer's published reason for The Three. Never null when theThree > 0. */
    val theThreeReason: String? = null,
    val provisional: Boolean = true
)

/** One itemised line of a player's score. Every point is explained (§05, §08). */
data class PointLine(val label: String, val points: Int)

data class PlayerScore(
    val playerId: String,
    val lines: List<PointLine>,
    val basePoints: Int,
    val differentialTier: DifferentialTier,
    val groundManApplied: Boolean,
    /** 1 = ordinary, 2 = captain, 3 = captain under the Jara chip. */
    val captainMultiplier: Int,
    val finalPoints: Int
)
