package ng.naijaleague.fantasy.rules

/**
 * The legal formations, ENUMERATED FROM THE RULES rather than listed by hand.
 *
 * WHY DERIVE INSTEAD OF HARDCODE. [Squad.validateStartingXi] already decides
 * what a legal XI is, from [SquadRules]. A hand-written list of formation
 * strings would be a second, independent statement of the same rule — and the
 * moment somebody widened DEF to 3-5 in SquadRules without remembering to edit
 * the list, the picker would offer a shape the validator rejects, or hide one
 * it would have allowed. Both failures land on the manager as "the app says no
 * and won't say why". Enumerating from the same constants means the picker
 * cannot disagree with the validator, because there is only one rule.
 *
 * The enumeration walks a few dozen combinations once, at first access. There is
 * no cleverness here to get wrong, which is the point.
 *
 * WHAT A FORMATION IS AND IS NOT. It is a description of the outfield split of
 * the eleven players on the pitch. It is NOT a thing a manager owns, and it is
 * deliberately not stored: a squad already implies its formation, so persisting
 * one as well would create two sources of truth that can disagree. [of] reads
 * the answer off an XI instead.
 */
object Formations {

    data class Formation(
        val defenders: Int,
        val midfielders: Int,
        val forwards: Int
    ) {
        /** How everyone writes it: 4-3-3, never 1-4-3-3. The keeper is assumed. */
        val label: String get() = "$defenders-$midfielders-$forwards"
    }

    /**
     * Every split that satisfies the position bounds and fills the XI.
     *
     * Sorted defenders ascending then midfielders ascending, which puts the
     * attacking shapes first within each back line and gives the picker a
     * stable, readable order.
     */
    val legal: List<Formation> by lazy {
        val keepers = SquadRules.XI_MINIMUM[Position.GK] ?: 1
        val outfield = SquadRules.STARTING_XI - keepers
        fun range(position: Position) =
            (SquadRules.XI_MINIMUM[position] ?: 0)..(SquadRules.XI_MAXIMUM[position] ?: outfield)

        buildList {
            for (d in range(Position.DEF)) {
                for (m in range(Position.MID)) {
                    val f = outfield - d - m
                    if (f in range(Position.FWD)) add(Formation(d, m, f))
                }
            }
        }
    }

    val labels: List<String> get() = legal.map { it.label }

    /** The formation an XI is already playing, or null if the XI is not legal. */
    fun of(startingXi: List<Player>): Formation? {
        if (startingXi.size != SquadRules.STARTING_XI) return null
        if (startingXi.count { it.position == Position.GK } != (SquadRules.XI_MINIMUM[Position.GK] ?: 1)) {
            return null
        }
        val shape = Formation(
            defenders = startingXi.count { it.position == Position.DEF },
            midfielders = startingXi.count { it.position == Position.MID },
            forwards = startingXi.count { it.position == Position.FWD }
        )
        return if (shape in legal) shape else null
    }

    /**
     * Can this squad field that formation at all?
     *
     * The picker asks before offering a shape, so a manager is never shown a
     * formation their fifteen cannot make. Showing it and then refusing is the
     * behaviour this exists to prevent.
     */
    fun reachable(squad: Squad, formation: Formation): Boolean =
        squad.countBy(Position.GK) >= (SquadRules.XI_MINIMUM[Position.GK] ?: 1) &&
            squad.countBy(Position.DEF) >= formation.defenders &&
            squad.countBy(Position.MID) >= formation.midfielders &&
            squad.countBy(Position.FWD) >= formation.forwards

    fun reachableFrom(squad: Squad): List<Formation> = legal.filter { reachable(squad, it) }

    /**
     * The XI this squad would field in that shape.
     *
     * Returns null when the fifteen cannot make it — the picker greys those out
     * rather than offering a shape it would then refuse.
     *
     * WHO GETS PICKED, in order, and each rule is there for a reason a manager
     * would recognise:
     *
     *   1. THE CAPTAIN, ALWAYS. Changing shape must never bench the armband. A
     *      captain on the bench scores nothing and the validator rejects it, so
     *      a picker that could produce one is a picker that produces an error.
     *   2. WHOEVER IS ALREADY PLAYING. Moving from 4-3-3 to 4-4-2 should change
     *      one player, not eleven. Continuity is the difference between adjusting
     *      a team and being handed a new one.
     *   3. THEN THE MOST EXPENSIVE ON THE BENCH. Price is this game's only
     *      standing estimate of who is better — there are no form ratings for
     *      this league — so it is the honest tie-break, and the manager can
     *      still swap anyone afterwards.
     *
     * Deterministic: the same squad and shape always give the same eleven.
     */
    fun selectXi(squad: Squad, formation: Formation): Set<String>? {
        if (!reachable(squad, formation)) return null
        val wanted = mapOf(
            Position.GK to (SquadRules.XI_MINIMUM[Position.GK] ?: 1),
            Position.DEF to formation.defenders,
            Position.MID to formation.midfielders,
            Position.FWD to formation.forwards
        )
        val order = compareByDescending<Player> { it.id == squad.captainId }
            .thenByDescending { it.id in squad.startingIds }
            .thenByDescending { it.priceNaira }
            // Last resort so the result cannot depend on list order.
            .thenBy { it.id }

        return buildSet {
            wanted.forEach { (position, count) ->
                addAll(
                    squad.allPlayers
                        .filter { it.position == position }
                        .sortedWith(order)
                        .take(count)
                        .map { it.id }
                )
            }
        }
    }
}
