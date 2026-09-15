package ng.naijaleague.fantasy.data

import ng.naijaleague.fantasy.rules.Chip
import ng.naijaleague.fantasy.rules.Club
import ng.naijaleague.fantasy.rules.Performance
import ng.naijaleague.fantasy.rules.Player
import ng.naijaleague.fantasy.rules.Position
import ng.naijaleague.fantasy.rules.Squad
import ng.naijaleague.fantasy.rules.Venue
import kotlin.math.roundToLong

/**
 * The data the app opens on.
 *
 * THE CLUBS AND THE PLAYERS ARE REAL, AS FAR AS THE LEAGUE PUBLISHES THEM.
 * Twenty clubs with sourced grounds, capacities, honours and coaches — see
 * [NpflClubs]. Fifty-six real players whose club and position both come from a
 * club-official page or a dated report — see [NpflSquads].
 *
 * AND THE REST ARE LABELLED STAND-INS, because eighteen of the twenty clubs
 * publish no squad anybody can source. That is the actual state of NPFL data in
 * September 2026, and it is not a problem a fantasy app can write its way out
 * of. The two available responses were:
 *
 *   1. Invent Nigerian names. The demo looks finished. Every user who knows the
 *      league sees a stranger listed at their club and stops trusting the app —
 *      and those are the users this product exists for.
 *   2. Ship stand-ins that say on their face what they are, put the real names
 *      in wherever a source exists, and show the sourcing on the screen.
 *
 * This is the second. [Player.isPlaceholder] marks them, and the Choose Players
 * screen is required to show it. Filling them in is operator work, not a
 * research problem — somebody who has seen these teams play knows the answers.
 *
 * PRICES AND OWNERSHIP ARE GAME PARAMETERS, NOT FACTS. No market value is
 * published for an NPFL player, and ownership in a live build is computed from
 * real entries. Both are derived below, deterministically, and neither is a
 * claim about anybody.
 *
 * No Android imports here, so all of it runs through the rules engine on the JVM.
 */
object SampleData {

    // ---------------------------------------------------------------- clubs

    val table: List<NpflClubs.ClubRecord> get() = NpflClubs.all
    val clubs: List<Club> get() = NpflClubs.clubs

    fun club(id: String): Club = NpflClubs.club(id)

    const val PUBLISHED_SQUAD_TOTAL = NpflClubs.PUBLISHED_SQUAD_TOTAL
    const val PUBLISHED_VALUE_TOTAL_EUR = NpflClubs.PUBLISHED_VALUE_TOTAL_EUR

    // --------------------------------------------------------------- prices

    /**
     * Position band, scaled by the club's fixture-difficulty rating, rounded to
     * the nearest ₦500,000.
     *
     * FLAT WITHIN A CLUB AND A POSITION, deliberately. There is no per-player
     * statistic published for this league, so any variation between two
     * Enyimba defenders would be invented — and an invented price ladder reads
     * as received wisdom about who is good. Prices move once real minutes and
     * returns arrive, which is the only honest way to move them.
     */
    private fun priceFor(clubId: String, position: Position): Long {
        val band = when (position) {
            Position.GK -> 4.5
            Position.DEF -> 5.0
            Position.MID -> 5.5
            Position.FWD -> 6.0
        }
        val strength = NpflClubs.record(clubId).strength
        val scaled = band * (0.8 + (strength / 5.0) * 0.5)
        val halfMillions = (scaled * 2.0).roundToLong()
        return halfMillions * 500_000L
    }

    /**
     * Demo ownership for the fifteen players in the example squad.
     *
     * Hand-set, and the only hand-set numbers in the file. The differential
     * reward (§08) is the mechanic this product turns on, and a demo where no
     * player sits under 2% ownership would never show it working. So the
     * captain is heavily owned and blanks, and a 1.8%-owned forward hauls away
     * from home. Those are illustrative figures about a mechanic, not a claim
     * about how many people own ThankGod Chilaka.
     */
    private val demoOwnership: Map<String, Double> = mapOf(
        "ENY-asibe" to 34.2, "RIV-andy" to 18.7,
        "IKO-nnoli" to 41.3, "PLA-udoh" to 27.9,
        "PH-BEN-def1" to 14.2, "PH-KAN-def2" to 19.4, "PH-NAS-def3" to 7.7,
        "ENY-ovoke" to 52.8, "KUN-umoh" to 12.7,
        "PH-3SC-mid4" to 23.6, "PH-SPL-mid2" to 8.9, "PH-NIT-mid1" to 2.9,
        "RIV-awazie" to 61.4, "IKO-chilaka" to 1.8, "PH-ABW-fwd2" to 5.9
    )

    /**
     * Ownership for everyone else: a stable function of the player id, bounded
     * to 0–40%, so the pool shows a spread and shows the same spread on every
     * launch. Replaced wholesale by real entry counts in a live build.
     */
    private fun ownershipFor(id: String): Double {
        demoOwnership[id]?.let { return it }
        var h = 0x811c9dc5.toInt()
        for (ch in id) {
            h = h xor ch.code
            h *= 0x01000193
        }
        val bounded = ((h.toLong() and 0xffffffffL) % 4000L).toInt()
        return bounded / 100.0
    }

    // -------------------------------------------------------- the real pool

    /** Real players, from [NpflSquads], for the five clubs that have any. */
    private val realPlayers: List<Player> = NpflSquads.positioned().map { rp ->
        val id = NpflSquads.idFor(rp)
        Player(
            id = id,
            name = rp.name,
            clubId = rp.clubId,
            position = rp.position!!,
            priceNaira = priceFor(rp.clubId, rp.position),
            ownershipPct = ownershipFor(id),
            squadNumber = rp.squadNumber,
            isPlaceholder = false,
            sourceNote = rp.note
        )
    }

    /**
     * Stand-ins for the eighteen clubs with no sourceable squad.
     *
     * Twelve per club on a fixed ladder — no randomness, so the same pool
     * renders every launch and a price never moves because a hash changed. The
     * ladder does not scale by club strength: pretending a stand-in at a strong
     * club is worth more would be a judgement about a player who does not exist.
     */
    private val placeholderLadder: Map<Position, List<Double>> = mapOf(
        Position.GK to listOf(4.0, 4.5),
        Position.DEF to listOf(4.0, 4.5, 5.0, 5.5),
        Position.MID to listOf(4.5, 5.5, 6.5, 7.5),
        Position.FWD to listOf(5.5, 7.0)
    )

    private val placeholders: List<Player> = buildList {
        val needing = NpflClubs.all
            .map { it.id }
            .filterNot { it in NpflSquads.clubsWithVerifiedSquad }
        for (clubId in needing) {
            for ((position, prices) in placeholderLadder) {
                prices.forEachIndexed { index, millions ->
                    val n = index + 1
                    val id = "PH-$clubId-${position.short.lowercase()}$n"
                    add(
                        Player(
                            id = id,
                            name = "${NpflClubs.record(clubId).club.shortName} ${position.short} $n",
                            clubId = clubId,
                            position = position,
                            priceNaira = (millions * 1_000_000).toLong(),
                            ownershipPct = ownershipFor(id),
                            squadNumber = null,
                            isPlaceholder = true,
                            sourceNote = "Stand-in. No squad list is published for this " +
                                "club that any source will stand behind."
                        )
                    )
                }
            }
        }
    }

    /** The selection pool the Choose Players screen browses. */
    val pool: List<Player> = realPlayers + placeholders

    private val byId = pool.associateBy { it.id }

    fun player(id: String): Player = byId[id] ?: error("no such player: $id")

    /** Real players known by name whose position nobody publishes. */
    val awaitingPosition = NpflSquads.awaitingPosition()

    // ------------------------------------------------------ the demo squad

    /**
     * A legal example squad: 15 players, 4-3-3, ₦88.5m of ₦100m.
     *
     * EIGHT OF THE FIFTEEN ARE REAL. That is the ceiling, not a choice: only
     * five clubs have a sourced player with a sourced position, and the club cap
     * is two, so ten is the arithmetic maximum and two of those five clubs have
     * just one qualifying player between them. The other seven slots are
     * stand-ins, and the squad screen says which is which.
     */
    val squadPlayerIds = listOf(
        "ENY-asibe", "RIV-andy",
        "IKO-nnoli", "PLA-udoh", "PH-BEN-def1", "PH-KAN-def2", "PH-NAS-def3",
        "ENY-ovoke", "KUN-umoh", "PH-3SC-mid4", "PH-SPL-mid2", "PH-NIT-mid1",
        "RIV-awazie", "IKO-chilaka", "PH-ABW-fwd2"
    )

    val startingIds = setOf(
        "ENY-asibe",
        "IKO-nnoli", "PLA-udoh", "PH-BEN-def1", "PH-KAN-def2",
        "ENY-ovoke", "KUN-umoh", "PH-3SC-mid4",
        "RIV-awazie", "IKO-chilaka", "PH-ABW-fwd2"
    )

    val squad = Squad(
        allPlayers = squadPlayerIds.map { player(it) },
        startingIds = startingIds,
        captainId = "RIV-awazie",
        viceCaptainId = "ENY-ovoke"
    )

    /**
     * Gameweek 12, as filed by the scorers at the ten grounds.
     *
     * Note how many of the good returns are away fixtures. That is the Away Day
     * Bonus doing its job, and it is what the pitch screen should make obvious.
     * The captain blanked at home; a forward owned by 1.8% of the game scored
     * twice away. That is the whole argument of the product in one round.
     */
    val gameweek12: Map<String, Performance> = mapOf(
        // GK — home, clean sheet, five saves
        "ENY-asibe" to Performance("ENY-asibe", Venue.HOME, minutes = 90, cleanSheet = true,
            saves = 5, theThree = 2,
            theThreeReason = "Two saves at 1-0 in the last ten minutes.", provisional = false),
        // DEF
        "IKO-nnoli" to Performance("IKO-nnoli", Venue.AWAY, minutes = 90, cleanSheet = true,
            provisional = false),
        "PLA-udoh" to Performance("PLA-udoh", Venue.HOME, minutes = 90, goalsConceded = 2,
            provisional = false),
        "PH-BEN-def1" to Performance("PH-BEN-def1", Venue.AWAY, minutes = 78, assists = 1,
            provisional = false),
        "PH-KAN-def2" to Performance("PH-KAN-def2", Venue.HOME, minutes = 90, cleanSheet = true,
            theThree = 1, theThreeReason = "Won everything in the air.", provisional = false),
        // MID
        "ENY-ovoke" to Performance("ENY-ovoke", Venue.HOME, minutes = 90, goals = 1, assists = 1,
            theThree = 3, theThreeReason = "Made both goals. Ran the second half.",
            provisional = false),
        "KUN-umoh" to Performance("KUN-umoh", Venue.AWAY, minutes = 90, goals = 1, yellowCards = 1,
            provisional = false),
        "PH-3SC-mid4" to Performance("PH-3SC-mid4", Venue.HOME, minutes = 64, provisional = false),
        // FWD — the captain blanked, the 1.8%-owned differential did not
        "RIV-awazie" to Performance("RIV-awazie", Venue.HOME, minutes = 90, provisional = false),
        "IKO-chilaka" to Performance("IKO-chilaka", Venue.AWAY, minutes = 87, goals = 2,
            assists = 1, theThree = 3,
            theThreeReason = "Two away goals and the assist. Nobody owned him.",
            provisional = true),
        "PH-ABW-fwd2" to Performance("PH-ABW-fwd2", Venue.AWAY, minutes = 90, goals = 1,
            provisional = false),
        // Bench, which only counts under Owambe
        "RIV-andy" to Performance("RIV-andy", Venue.AWAY, minutes = 0, provisional = false),
        "PH-NAS-def3" to Performance("PH-NAS-def3", Venue.HOME, minutes = 90, cleanSheet = true,
            provisional = false),
        "PH-SPL-mid2" to Performance("PH-SPL-mid2", Venue.AWAY, minutes = 90, assists = 1,
            provisional = false),
        "PH-NIT-mid1" to Performance("PH-NIT-mid1", Venue.HOME, minutes = 12, provisional = false)
    )

    val activeChip: Chip? = null

    // ------------------------------------------------------------- leagues

    /** Mini-league table. Real-looking squad names, because that is what people write. */
    data class LeagueEntry(
        val rank: Int,
        val squadName: String,
        val manager: String,
        val town: String,
        val gameweekPoints: Int,
        val totalPoints: Int
    )

    val miniLeague = listOf(
        LeagueEntry(1, "Aba Boys Academy", "Chidi O.", "Aba", 81, 982),
        LeagueEntry(2, "Oga At The Top United", "Bukola A.", "Ibadan", 64, 964),
        LeagueEntry(3, "Harmattan Rangers", "Sadiq M.", "Kano", 59, 931),
        LeagueEntry(4, "Jagaban FC", "Emeka N.", "Enugu", 72, 905),
        LeagueEntry(5, "No Wahala XI", "Tunde B.", "Lagos", 44, 876),
        LeagueEntry(6, "Correct Eleven", "Halima Y.", "Jos", 51, 842),
        LeagueEntry(7, "Owambe Selects", "Femi K.", "Abeokuta", 38, 801),
        LeagueEntry(8, "Table No Dey Lie", "Ngozi E.", "Owerri", 66, 765)
    )

    // ------------------------------------------------------------ fixtures

    data class Fixture(
        val homeClubId: String,
        val awayClubId: String,
        val kickoff: String,
        val homeScore: Int? = null,
        val awayScore: Int? = null,
        val minute: Int? = null
    ) {
        val isLive: Boolean get() = minute != null

        /**
         * True when the home club is not actually at home. Read off the club
         * record rather than stored, so it cannot drift out of step with it.
         */
        val homeAtNeutralGround: Boolean
            get() = NpflClubs.record(homeClubId).playsHomeAtNeutralGround

        /** The ground this is played at, which for two clubs is not their own. */
        val venueName: String get() = NpflClubs.record(homeClubId).stadium
    }

    /**
     * A Friday-to-Sunday round at a standard 4:00 PM kickoff, which is the
     * pattern the league's own Matchday 1 and 2 lists follow, with the odd
     * outlier and a real chance of a postponement.
     *
     * Rangers' fixture carries the season's oddity: the table calls it a home
     * game and it is played 600km away in Abeokuta.
     */
    val fixtures = listOf(
        Fixture("RAN", "ENY", "Sun 16 Nov · 4:00 PM"),
        Fixture("BEN", "PLA", "Sun 16 Nov · 4:00 PM"),
        Fixture("KAN", "KAT", "Sun 16 Nov · 4:00 PM"),
        Fixture("IKO", "SPL", "Sun 16 Nov · 4:00 PM"),
        Fixture("WAR", "NIT", "Sun 16 Nov · 4:00 PM"),
        Fixture("DOM", "RAB", "Fri 14 Nov · 4:00 PM"),
        Fixture("NAS", "KWA", "Wed 19 Nov · 4:00 PM")
    )

    val liveFixture = Fixture("RIV", "KAN", "Live", homeScore = 1, awayScore = 0, minute = 67)

    data class LiveStat(val label: String, val home: Int, val away: Int)

    val liveStats = listOf(
        LiveStat("Possession", 58, 42),
        LiveStat("Shots on target", 6, 3),
        LiveStat("Corners", 4, 2),
        LiveStat("Fouls", 8, 12)
    )

    // ------------------------------------------------------------ gameweek

    const val gameweekNumber = 12
    const val deadlineLabel = "Sun 16 Nov · 3:00 PM"

    /**
     * Deadline as an instant, so the strip can actually count down and reach its
     * final-hour state. In the live build this comes from the fixture feed; here
     * it is anchored 26 hours ahead of first launch so the demo shows a plausible
     * countdown rather than a frozen literal.
     */
    private val deadlineEpochMillis: Long =
        System.currentTimeMillis() + 26L * 60L * 60L * 1000L

    fun hoursToDeadline(nowMillis: Long = System.currentTimeMillis()): Int {
        val remaining = deadlineEpochMillis - nowMillis
        return if (remaining <= 0) 0 else (remaining / (60L * 60L * 1000L)).toInt()
    }

    const val overallRank = 128_432
    const val totalManagers = 1_245_678
    const val bankedTransfers = 1
}
