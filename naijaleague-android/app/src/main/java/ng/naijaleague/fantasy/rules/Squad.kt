package ng.naijaleague.fantasy.rules

/**
 * Squad composition and its rules (§05, §08).
 *
 * The cap that matters most is MAX_PER_CLUB = 2, not FPL's 3. A flatter league
 * means the spread should be wider, and it quietly delivers the mission: a
 * manager ends up caring about at least eight NPFL clubs.
 */
object SquadRules {
    const val BUDGET_NAIRA = 100_000_000L
    const val SQUAD_SIZE = 15
    const val STARTING_XI = 11
    const val MAX_PER_CLUB = 2

    val QUOTA: Map<Position, Int> = mapOf(
        Position.GK to 2,
        Position.DEF to 5,
        Position.MID to 5,
        Position.FWD to 3
    )

    /** Minimum outfield shape for a legal starting XI. */
    val XI_MINIMUM: Map<Position, Int> = mapOf(
        Position.GK to 1,
        Position.DEF to 3,
        Position.MID to 2,
        Position.FWD to 1
    )
    val XI_MAXIMUM: Map<Position, Int> = mapOf(
        Position.GK to 1,
        Position.DEF to 5,
        Position.MID to 5,
        Position.FWD to 3
    )
}

/**
 * A validation failure, phrased the way the brand talks in an interface (§12):
 * clear English, says what is wrong and what to do about it, no banter. The
 * jokes live in the celebration copy, not in a message that blocks somebody.
 */
data class Violation(val code: String, val message: String)

data class Squad(
    val allPlayers: List<Player>,
    val startingIds: Set<String>,
    val captainId: String?,
    val viceCaptainId: String? = null
) {
    val startingXi: List<Player> get() = allPlayers.filter { it.id in startingIds }
    val bench: List<Player> get() = allPlayers.filter { it.id !in startingIds }
    val spend: Long get() = allPlayers.sumOf { it.priceNaira }
    val budgetRemaining: Long get() = SquadRules.BUDGET_NAIRA - spend

    fun countBy(position: Position) = allPlayers.count { it.position == position }
    fun countByClub(clubId: String) = allPlayers.count { it.clubId == clubId }

    /** Empty list means the squad is legal and can be saved. */
    fun validate(): List<Violation> {
        val v = mutableListOf<Violation>()

        if (allPlayers.size != SquadRules.SQUAD_SIZE) {
            val short = SquadRules.SQUAD_SIZE - allPlayers.size
            v += Violation(
                "SQUAD_SIZE",
                if (short > 0) "You have ${allPlayers.size} players. Add $short more to reach 15."
                else "You have ${allPlayers.size} players. Remove ${-short} to get back to 15."
            )
        }

        if (allPlayers.size != allPlayers.distinctBy { it.id }.size) {
            v += Violation("DUPLICATE", "The same player appears twice. Each player can only be picked once.")
        }

        SquadRules.QUOTA.forEach { (position, required) ->
            val have = countBy(position)
            if (have != required) {
                v += Violation(
                    "QUOTA_${position.name}",
                    "You need $required ${position.label.lowercase()}s and you have $have."
                )
            }
        }

        allPlayers.groupBy { it.clubId }
            .filterValues { it.size > SquadRules.MAX_PER_CLUB }
            .forEach { (clubId, players) ->
                v += Violation(
                    "CLUB_CAP",
                    "You have ${players.size} players from one club. The limit is 2 — drop " +
                        "${players.size - SquadRules.MAX_PER_CLUB} from $clubId."
                )
            }

        if (spend > SquadRules.BUDGET_NAIRA) {
            v += Violation(
                "BUDGET",
                "You are ${Money.format(spend - SquadRules.BUDGET_NAIRA)} over budget. " +
                    "Sell someone or pick cheaper."
            )
        }

        if (captainId == null) {
            v += Violation("NO_CAPTAIN", "Pick a captain. Your captain scores double.")
        } else if (captainId !in startingIds) {
            v += Violation("CAPTAIN_ON_BENCH", "Your captain is on the bench. Move them into your XI.")
        }
        if (viceCaptainId != null && viceCaptainId == captainId) {
            v += Violation("VICE_IS_CAPTAIN", "Your captain and vice-captain are the same player.")
        }

        v += validateStartingXi()
        return v
    }

    /** The XI rules are checked separately because the pitch screen uses them alone. */
    fun validateStartingXi(): List<Violation> {
        val v = mutableListOf<Violation>()
        val xi = startingXi

        if (xi.size != SquadRules.STARTING_XI) {
            v += Violation(
                "XI_SIZE",
                "You have ${xi.size} players in your XI. It has to be 11."
            )
        }
        SquadRules.XI_MINIMUM.forEach { (position, min) ->
            val have = xi.count { it.position == position }
            val max = SquadRules.XI_MAXIMUM.getValue(position)
            if (have < min) {
                v += Violation(
                    "XI_MIN_${position.name}",
                    "An XI needs at least $min ${position.label.lowercase()}${if (min > 1) "s" else ""}. You have $have."
                )
            } else if (have > max) {
                v += Violation(
                    "XI_MAX_${position.name}",
                    "You can only field $max ${position.label.lowercase()}${if (max > 1) "s" else ""}. You have $have."
                )
            }
        }
        return v
    }

    /** e.g. "4-3-3", read off the XI rather than stored, so it can never drift. */
    val formation: String
        get() = listOf(Position.DEF, Position.MID, Position.FWD)
            .joinToString("-") { pos -> startingXi.count { it.position == pos }.toString() }

    val isValid: Boolean get() = validate().isEmpty()
}

/** Naira formatting. Integer money in, human money out. */
object Money {
    /** "12.5m" for compact player prices. */
    fun compact(naira: Long): String {
        val millions = naira / 1_000_000.0
        return if (millions % 1.0 == 0.0) "${millions.toLong()}m"
        else "${(Math.round(millions * 10) / 10.0)}m"
    }

    /** "N100,000,000" for budgets and totals. */
    fun format(naira: Long): String {
        val digits = naira.toString().reversed().chunked(3).joinToString(",").reversed()
        return "₦$digits"
    }
}
