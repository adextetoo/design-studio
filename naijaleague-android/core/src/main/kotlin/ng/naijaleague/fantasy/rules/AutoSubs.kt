package ng.naijaleague.fantasy.rules

/**
 * Automatic substitutions (§05).
 *
 * Applied once every fixture in the gameweek has reached a terminal state. A
 * starter who did not appear is replaced by the first eligible player in bench
 * order, provided the XI that results is still a legal shape.
 *
 * THE NPFL WRINKLE, and it is the reason this module is not a copy of FPL's
 * behaviour: A POSTPONED FIXTURE COUNTS AS A NON-APPEARANCE. Three Matchday 2
 * fixtures were already postponed in the first fortnight of 2026/27 — Enyimba
 * v Shooting Stars, Warri Wolves v Rangers, Katsina United v Rivers United. A
 * game that treated a postponement as "played, nil points" would routinely
 * leave managers fielding nine men through nobody's fault, on a weekend they
 * could not have planned around. So a postponement brings the bench on, and
 * [SubstitutionEvent.reason] says which of the two happened, because the
 * difference matters to the person reading it.
 *
 * OWAMBE SHORT-CIRCUITS THE WHOLE THING. Under that chip all fifteen score,
 * bench included, so there is no bench to substitute from and nothing to
 * decide. At an owambe nobody sits outside.
 */
object AutoSubs {

    /** Why a player is not available to score. */
    enum class Absence {
        /** Named in the squad, did not get on the pitch. */
        DID_NOT_APPEAR,

        /** Their fixture was postponed. Common enough here to be a first-class case. */
        FIXTURE_POSTPONED
    }

    data class SubstitutionEvent(
        val outId: String,
        val inId: String,
        val absence: Absence
    ) {
        /** Shown to the manager on the points screen, in plain English (§12). */
        val reason: String
            get() = when (absence) {
                Absence.DID_NOT_APPEAR -> "Did not play"
                Absence.FIXTURE_POSTPONED -> "Match postponed"
            }
    }

    data class Result(
        val finalXi: List<Player>,
        val substitutions: List<SubstitutionEvent>
    )

    /**
     * Is this shape still legal for a starting XI?
     *
     * Asked of SquadRules rather than answered here, so a substitution can never
     * produce a lineup the squad validator would have rejected.
     */
    private fun shapeLegal(xi: List<Player>): Boolean {
        if (xi.size != SquadRules.STARTING_XI) return false
        return Position.entries.all { position ->
            val n = xi.count { it.position == position }
            n >= (SquadRules.XI_MINIMUM[position] ?: 0) &&
                n <= (SquadRules.XI_MAXIMUM[position] ?: SquadRules.STARTING_XI)
        }
    }

    /**
     * Resolve substitutions for one manager's gameweek.
     *
     * @param squad the squad as it stood at the deadline
     * @param absences why each absent player is absent, keyed by player id. A
     * player missing from this map played.
     * @param benchOrder bench player ids in the manager's chosen order; the
     * reserve keeper's place in it is irrelevant, since only they can replace
     * the keeper. Players not listed keep their order from the squad.
     * @param activeChip the chip played this gameweek, if any
     */
    fun resolve(
        squad: Squad,
        absences: Map<String, Absence>,
        benchOrder: List<String> = emptyList(),
        activeChip: Chip? = null
    ): Result {
        // Under Owambe every player scores, so there is nothing to substitute.
        if (activeChip == Chip.OWAMBE) {
            return Result(squad.allPlayers, emptyList())
        }

        val played = { p: Player -> p.id !in absences }

        val xi = squad.startingXi.toMutableList()
        val bench = squad.bench.sortedBy { player ->
            val at = benchOrder.indexOf(player.id)
            if (at == -1) Int.MAX_VALUE else at
        }.toMutableList()
        val subs = mutableListOf<SubstitutionEvent>()

        // The keeper first, and separately: only the reserve keeper may come on
        // for the keeper, and only ever into the keeper's slot. An outfielder in
        // goal is not a legal XI and is not a kindness either.
        val keeperSlot = xi.indexOfFirst { it.position == Position.GK }
        if (keeperSlot != -1 && !played(xi[keeperSlot])) {
            val reserve = bench.firstOrNull { it.position == Position.GK && played(it) }
            if (reserve != null) {
                subs += SubstitutionEvent(
                    outId = xi[keeperSlot].id,
                    inId = reserve.id,
                    absence = absences.getValue(xi[keeperSlot].id)
                )
                xi[keeperSlot] = reserve
                bench.remove(reserve)
            }
        }

        // Then outfield, in bench order. A substitution is only made if the XI it
        // produces is still legal — otherwise the next bench player is tried.
        for (slot in xi.indices) {
            val out = xi[slot]
            if (out.position == Position.GK || played(out)) continue

            val candidate = bench.firstOrNull { option ->
                option.position != Position.GK && played(option) &&
                    shapeLegal(xi.toMutableList().also { it[slot] = option })
            } ?: continue

            subs += SubstitutionEvent(
                outId = out.id,
                inId = candidate.id,
                absence = absences.getValue(out.id)
            )
            xi[slot] = candidate
            bench.remove(candidate)
        }

        return Result(xi, subs)
    }

    /**
     * Absences for a gameweek, worked out from the performances that were filed.
     *
     * A player with no performance filed at all is treated as postponed rather
     * than as absent: if the scorer never filed, the likeliest reason in this
     * league is that the match was not played. Zero minutes in a filed report is
     * the other case — he was there and did not get on.
     */
    fun absencesFrom(
        squad: Squad,
        performances: Map<String, Performance>
    ): Map<String, Absence> = squad.allPlayers.mapNotNull { player ->
        val perf = performances[player.id]
        when {
            perf == null -> player.id to Absence.FIXTURE_POSTPONED
            perf.minutes == 0 -> player.id to Absence.DID_NOT_APPEAR
            else -> null
        }
    }.toMap()
}
