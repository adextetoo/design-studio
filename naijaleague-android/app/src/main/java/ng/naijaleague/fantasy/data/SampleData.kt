package ng.naijaleague.fantasy.data

import ng.naijaleague.fantasy.rules.Chip
import ng.naijaleague.fantasy.rules.Club
import ng.naijaleague.fantasy.rules.Performance
import ng.naijaleague.fantasy.rules.Player
import ng.naijaleague.fantasy.rules.Position
import ng.naijaleague.fantasy.rules.Squad
import ng.naijaleague.fantasy.rules.Venue

/**
 * EXAMPLE DATA — not a real squad, not real players.
 *
 * The clubs are the real NPFL. The players are invented, deliberately: putting
 * real internationals into NPFL squads (as the reference mockups did) states
 * something false about where those players actually play, and this product's
 * whole credibility rests on knowing the league.
 *
 * The names are chosen to exercise the typography constraint in §02 for real —
 * Yoruba under-dots and tone marks, Igbo dotted vowels, Hausa hooked letters.
 * If a screen renders these wrong, the bug is visible immediately instead of at
 * launch. That is the point of shipping them as the sample set.
 *
 * No Android imports here, so the same data can be run through the rules engine
 * on the JVM.
 */
object SampleData {

    /**
     * The 2026/27 NPFL, transcribed from the Transfermarkt league table
     * (current-value column, captured against the 15/08/2026 snapshot).
     *
     * Market value and squad size are carried alongside each club so the table
     * can be checked against the totals Transfermarkt publishes — see
     * SampleDataTest, which fails if a transcription error creeps in. The squad
     * sizes sum to exactly 829, which is what the source reports.
     *
     * Values are in euros because that is the unit the source publishes. They
     * are league reference data, not fantasy prices.
     */
    data class ClubRecord(
        val club: Club,
        val marketValueEur: Long,
        val squadSize: Int
    )

    private fun rec(
        id: String, name: String, short: String, city: String,
        valueEur: Long, squad: Int
    ) = ClubRecord(Club(id, name, short, city), valueEur, squad)

    /** Ordered by market value, as the source orders it. */
    val table: List<ClubRecord> = listOf(
        rec("RAN", "Rangers International FC", "RAN", "Enugu", 2_560_000, 42),
        rec("RIV", "Rivers United FC", "RIV", "Port Harcourt", 2_460_000, 35),
        rec("BEN", "Bendel Insurance", "BEN", "Benin City", 2_200_000, 43),
        // City not stated by the source and not implied by the club name.
        rec("BAR", "Barau Football Club", "BAR", "", 1_740_000, 48),
        rec("PLA", "Plateau United FC", "PLA", "Jos", 1_720_000, 56),
        rec("3SC", "Shooting Stars Sports Club", "3SC", "Ibadan", 1_700_000, 41),
        rec("KAN", "Kano Pillars", "KAN", "Kano", 1_700_000, 45),
        rec("ENY", "Enyimba Aba", "ENY", "Aba", 1_490_000, 40),
        rec("IKO", "Ikorodu City FC", "IKO", "Ikorodu", 1_280_000, 46),
        rec("ABW", "Abia Warriors FC", "ABW", "Umuahia", 1_230_000, 40),
        rec("KAT", "Katsina United FC", "KAT", "Katsina", 1_200_000, 43),
        rec("WAR", "Warri Wolves FC", "WAR", "Warri", 1_070_000, 51),
        rec("NAS", "Nasarawa United", "NAS", "Lafia", 1_020_000, 45),
        rec("NIT", "Niger Tornadoes", "NIT", "Minna", 1_010_000, 44),
        rec("KUN", "Kun Khalifat Football Club", "KUN", "", 855_000, 52),
        rec("KWA", "Kwara United FC", "KWA", "Ilorin", 855_000, 42),
        rec("SPL", "Sporting Lagos FC", "SPL", "Lagos", 735_000, 37),
        rec("RAB", "Rancher's Bees FC", "RAB", "", 605_000, 28),
        rec("DOM", "Doma United", "DOM", "Gombe", 225_000, 26),
        rec("INT", "Inter Lagos Football Club", "INT", "Lagos", 25_000, 25)
    )

    /** Totals the source publishes, kept here so the transcription is testable. */
    const val PUBLISHED_SQUAD_TOTAL = 829
    const val PUBLISHED_VALUE_TOTAL_EUR = 25_650_000L

    val clubs: List<Club> = table.map { it.club }

    fun club(id: String): Club = clubs.first { it.id == id }

    private fun p(
        id: String, name: String, club: String, pos: Position,
        millions: Double, ownership: Double
    ) = Player(id, name, club, pos, (millions * 1_000_000).toLong(), ownership)

    /** The selection pool the Choose Players screen browses. */
    val pool: List<Player> = listOf(
        // Goalkeepers
        p("gk1", "Ọláyínká Bámídélé", "BEN", Position.GK, 5.5, 34.2),
        p("gk2", "Sulaimon Ƙasim", "KAN", Position.GK, 4.5, 18.7),
        p("gk3", "Ṅnaemeka Ọ̀diké", "RAN", Position.GK, 5.0, 22.1),
        p("gk4", "Ɗauda Aliyu", "KAT", Position.GK, 4.0, 6.4),
        p("gk5", "Ebenezer Ìgè", "3SC", Position.GK, 4.5, 9.8),
        // Defenders
        p("df1", "Chidiébéré Ọ̀nụ̀", "ENY", Position.DEF, 6.0, 41.3),
        p("df2", "Ṣọlá Adéyẹmí", "3SC", Position.DEF, 5.5, 27.9),
        p("df3", "Ibrahim Ɗanjuma", "KAN", Position.DEF, 5.0, 19.4),
        p("df4", "Ẹmẹká Ùchè", "RAN", Position.DEF, 6.0, 31.6),
        p("df5", "Tunde Ògúndélé", "BEN", Position.DEF, 5.0, 14.2),
        p("df6", "Ọ̀bínna Ezè", "ABW", Position.DEF, 4.5, 3.1),
        p("df7", "Yakubu Ƴaro", "NAS", Position.DEF, 4.5, 7.7),
        p("df8", "Ṣàmúẹ̀l Òkè", "BEN", Position.DEF, 5.0, 11.5),
        p("df9", "Terver Ìorhemba", "WAR", Position.DEF, 4.0, 2.4),
        p("df10", "Bólájí Adétólá", "SPL", Position.DEF, 5.5, 6.8),
        // Midfielders
        p("md1", "Ìfẹ́anyì Ụ̀zọ̀", "ENY", Position.MID, 8.0, 52.8),
        p("md2", "Abdulƙadir Ṣehu", "PLA", Position.MID, 6.5, 4.2),
        p("md3", "Chinedu Ọ̀kụ̀", "RAN", Position.MID, 8.5, 47.1),
        p("md4", "Ṣeun Adélékè", "SPL", Position.MID, 7.0, 23.6),
        p("md5", "Ndubuisi Ámádí", "ABW", Position.MID, 6.0, 8.9),
        p("md6", "Kelechi Ọ̀gbọ́nna", "KWA", Position.MID, 6.5, 12.7),
        p("md7", "Bashir Ɓello", "DOM", Position.MID, 5.5, 1.6),
        p("md8", "Olúwáṣẹ́gun Fáyẹmí", "IKO", Position.MID, 9.0, 38.4),
        p("md9", "Sunday Ìkpè", "PLA", Position.MID, 6.0, 9.3),
        p("md10", "Ịkechukwu Ńwosu", "NIT", Position.MID, 5.5, 2.9),
        // Forwards
        p("fw1", "Ọlámidé Àkànbí", "3SC", Position.FWD, 10.5, 61.4),
        p("fw2", "Musa Ƴaro", "NAS", Position.FWD, 7.0, 15.8),
        p("fw3", "Godspower Ìhè", "RIV", Position.FWD, 7.0, 1.8),
        p("fw4", "Ṣẹ̀gun Olátúnjí", "IKO", Position.FWD, 6.5, 5.9),
        p("fw5", "Chukwudi Ọ̀nwụ", "ENY", Position.FWD, 9.5, 44.0),
        p("fw6", "Hamisu Ɗantata", "KAN", Position.FWD, 8.0, 20.3)
    )

    fun player(id: String): Player = pool.first { it.id == id }

    /**
     * A legal example squad: 15 players, 10 clubs, no more than 2 from any one,
     * ₦98,000,000 spent of ₦100,000,000. Formation 4-3-3.
     */
    val squadPlayerIds = listOf(
        "gk1", "gk2",
        "df1", "df2", "df3", "df4", "df5",
        "md1", "md2", "md3", "md4", "md5",
        "fw1", "fw2", "fw3"
    )

    val startingIds = setOf(
        "gk1",
        "df1", "df2", "df3", "df4",
        "md1", "md3", "md4",
        "fw1", "fw2", "fw3"
    )

    val squad = Squad(
        allPlayers = squadPlayerIds.map { player(it) },
        startingIds = startingIds,
        captainId = "fw1",
        viceCaptainId = "md1"
    )

    /**
     * Gameweek 12 results, as filed by the scorers at the ten grounds.
     * Note how many of the good returns are away fixtures — that is the Away Day
     * Bonus doing its job, and it is what the pitch screen should make obvious.
     */
    val gameweek12: Map<String, Performance> = mapOf(
        // 1 GK - home, kept a clean sheet with five saves
        "gk1" to Performance("gk1", Venue.HOME, 90, cleanSheet = true, saves = 5, theThree = 2,
            theThreeReason = "Two saves at 1-0 in the last ten minutes.", provisional = false),
        // 4 DEF
        "df1" to Performance("df1", Venue.AWAY, 90, cleanSheet = true, provisional = false),
        "df2" to Performance("df2", Venue.HOME, 90, goalsConceded = 2, provisional = false),
        "df3" to Performance("df3", Venue.AWAY, 78, assists = 1, provisional = false),
        "df4" to Performance("df4", Venue.HOME, 90, cleanSheet = true, theThree = 1,
            theThreeReason = "Won everything in the air.", provisional = false),
        // 3 MID
        "md1" to Performance("md1", Venue.HOME, 90, goals = 1, assists = 1, theThree = 3,
            theThreeReason = "Made both goals. Ran the second half.", provisional = false),
        "md3" to Performance("md3", Venue.AWAY, 90, goals = 1, yellowCards = 1, provisional = false),
        "md4" to Performance("md4", Venue.HOME, 64, provisional = false),
        // 3 FWD - the captain blanked, the 1.8%-owned differential did not
        "fw1" to Performance("fw1", Venue.HOME, 90, provisional = false),
        "fw2" to Performance("fw2", Venue.AWAY, 90, goals = 1, provisional = false),
        "fw3" to Performance("fw3", Venue.AWAY, 87, goals = 2, assists = 1, theThree = 3,
            theThreeReason = "Two away goals and the assist. Nobody owned him.", provisional = true),
        // Bench, which only counts under Owambe
        "gk2" to Performance("gk2", Venue.AWAY, 0, provisional = false),
        "df5" to Performance("df5", Venue.HOME, 90, cleanSheet = true, provisional = false),
        "md2" to Performance("md2", Venue.AWAY, 90, assists = 1, provisional = false),
        "md5" to Performance("md5", Venue.HOME, 12, provisional = false)
    )

    val activeChip: Chip? = null

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

    data class Fixture(
        val homeClubId: String,
        val awayClubId: String,
        val kickoff: String,
        val homeScore: Int? = null,
        val awayScore: Int? = null,
        val minute: Int? = null
    ) {
        val isLive: Boolean get() = minute != null
    }

    val fixtures = listOf(
        Fixture("RAN", "ENY", "Sun 16 Nov · 4:00 PM"),
        Fixture("BEN", "PLA", "Sun 16 Nov · 4:00 PM"),
        Fixture("KAN", "KAT", "Sun 16 Nov · 4:00 PM"),
        Fixture("IKO", "SPL", "Sun 16 Nov · 4:00 PM"),
        Fixture("WAR", "NIT", "Sun 16 Nov · 4:00 PM"),
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
