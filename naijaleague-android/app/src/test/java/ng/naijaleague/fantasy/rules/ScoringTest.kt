package ng.naijaleague.fantasy.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests for the two mechanics that make this game different from FPL: the Away
 * Day Bonus and the differential multiplier. If these break, the product loses
 * its argument, so they are tested first and hardest.
 */
class ScoringTest {

    private fun player(
        pos: Position,
        ownership: Double = 30.0,
        price: Long = 7_000_000,
        id: String = "p1",
        club: String = "ENY"
    ) = Player(id, "Test Player", club, pos, price, ownership)

    private fun perf(
        venue: Venue = Venue.HOME,
        minutes: Int = 90,
        goals: Int = 0,
        assists: Int = 0,
        cleanSheet: Boolean = false,
        saves: Int = 0,
        conceded: Int = 0,
        yellow: Int = 0,
        red: Int = 0,
        three: Int = 0,
        id: String = "p1"
    ) = Performance(
        playerId = id, venue = venue, minutes = minutes, goals = goals, assists = assists,
        cleanSheet = cleanSheet, saves = saves, goalsConceded = conceded,
        yellowCards = yellow, redCards = red, theThree = three
    )

    // ---------- The Away Day Bonus (§05) ----------

    @Test
    fun `midfielder goal away pays one more than the same goal at home`() {
        val p = player(Position.MID)
        val home = Scoring.scorePlayer(p, perf(venue = Venue.HOME, goals = 1))
        val away = Scoring.scorePlayer(p, perf(venue = Venue.AWAY, goals = 1))
        assertEquals(7, home.finalPoints, "2 mins + 5 goal")
        assertEquals(8, away.finalPoints, "2 mins + 5 goal + 1 away bonus")
        assertTrue(away.lines.any { it.label == "Away Day Bonus - goal" && it.points == 1 })
        assertFalse(home.lines.any { it.label.startsWith("Away Day Bonus") })
    }

    @Test
    fun `away bonus pays per goal and per assist`() {
        val p = player(Position.FWD)
        val s = Scoring.scorePlayer(p, perf(venue = Venue.AWAY, goals = 2, assists = 1))
        // 2 mins + 8 goals + 2 away goals + 3 assist + 1 away assist
        assertEquals(16, s.basePoints)
    }

    @Test
    fun `defender clean sheet away pays two more`() {
        val p = player(Position.DEF)
        val home = Scoring.scorePlayer(p, perf(venue = Venue.HOME, cleanSheet = true))
        val away = Scoring.scorePlayer(p, perf(venue = Venue.AWAY, cleanSheet = true))
        assertEquals(6, home.finalPoints)
        assertEquals(8, away.finalPoints)
    }

    @Test
    fun `midfielder gets no away clean sheet bonus`() {
        val p = player(Position.MID)
        val away = Scoring.scorePlayer(p, perf(venue = Venue.AWAY, cleanSheet = true))
        assertEquals(3, away.finalPoints, "2 mins + 1 clean sheet, no away CS bonus for MID")
    }

    @Test
    fun `clean sheet needs the full hour`() {
        val p = player(Position.DEF)
        val short = Scoring.scorePlayer(p, perf(minutes = 45, cleanSheet = true))
        assertEquals(1, short.finalPoints, "appearance only")
    }

    // ---------- Differential multiplier (§08, template tyranny) ----------

    @Test
    fun `differential tiers follow ownership boundaries`() {
        assertEquals(DifferentialTier.UNDER_2, DifferentialTier.forOwnership(1.9))
        assertEquals(DifferentialTier.UNDER_5, DifferentialTier.forOwnership(2.0))
        assertEquals(DifferentialTier.UNDER_5, DifferentialTier.forOwnership(4.9))
        assertEquals(DifferentialTier.NONE, DifferentialTier.forOwnership(5.0))
        assertEquals(DifferentialTier.NONE, DifferentialTier.forOwnership(61.4))
    }

    @Test
    fun `a two percent owned away goal outscores a template goal`() {
        val template = player(Position.MID, ownership = 61.0)
        val differential = player(Position.MID, ownership = 1.4)
        val same = perf(venue = Venue.AWAY, goals = 1)
        assertEquals(8, Scoring.scorePlayer(template, same).finalPoints)
        assertEquals(12, Scoring.scorePlayer(differential, same).finalPoints, "8 x 1.5")
    }

    @Test
    fun `under five percent gets one and a quarter`() {
        val p = player(Position.MID, ownership = 4.0)
        assertEquals(10, Scoring.scorePlayer(p, perf(venue = Venue.AWAY, goals = 1)).finalPoints)
    }

    // ---------- Captain, Jara and Ground Man ----------

    @Test
    fun `captain doubles and Jara triples`() {
        val p = player(Position.MID)
        val base = perf(venue = Venue.AWAY, goals = 1)
        assertEquals(8, Scoring.scorePlayer(p, base).finalPoints)
        assertEquals(16, Scoring.scorePlayer(p, base, isCaptain = true).finalPoints)
        assertEquals(24, Scoring.scorePlayer(p, base, isCaptain = true, activeChip = Chip.JARA).finalPoints)
    }

    @Test
    fun `Jara does nothing for a player who is not captain`() {
        val p = player(Position.MID)
        val s = Scoring.scorePlayer(p, perf(goals = 1), isCaptain = false, activeChip = Chip.JARA)
        assertEquals(1, s.captainMultiplier)
        assertEquals(7, s.finalPoints)
    }

    @Test
    fun `Ground Man only lifts players at home`() {
        val p = player(Position.MID)
        val atHome = Scoring.scorePlayer(p, perf(venue = Venue.HOME, goals = 1), activeChip = Chip.GROUND_MAN)
        val away = Scoring.scorePlayer(p, perf(venue = Venue.AWAY, goals = 1), activeChip = Chip.GROUND_MAN)
        assertEquals(11, atHome.finalPoints, "7 x 1.5 = 10.5 rounds to 11")
        assertTrue(atHome.groundManApplied)
        assertEquals(8, away.finalPoints, "away is untouched by Ground Man")
        assertFalse(away.groundManApplied)
    }

    @Test
    fun `multipliers apply in the documented order`() {
        // base 7 -> x1.5 differential -> x1.5 Ground Man -> round -> x2 captain
        val p = player(Position.MID, ownership = 1.0)
        val s = Scoring.scorePlayer(
            p, perf(venue = Venue.HOME, goals = 1), isCaptain = true, activeChip = Chip.GROUND_MAN
        )
        assertEquals(7, s.basePoints)
        assertEquals(32, s.finalPoints, "round(7 x 1.5 x 1.5) = 16, then x2")
    }

    // ---------- Rounding ----------

    @Test
    fun `fractions round half away from zero`() {
        val p = player(Position.FWD, ownership = 3.0)

        val oddBase = Scoring.scorePlayer(p, perf(assists = 1))
        assertEquals(5, oddBase.basePoints, "2 mins + 3 assist")
        assertEquals(6, oddBase.finalPoints, "5 x 1.25 = 6.25 rounds down to 6")

        val evenBase = Scoring.scorePlayer(p, perf(goals = 1))
        assertEquals(6, evenBase.basePoints, "2 mins + 4 goal")
        assertEquals(8, evenBase.finalPoints, "6 x 1.25 = 7.5 rounds up to 8")
    }

    @Test
    fun `a negative score is not softened by the multiplier`() {
        val p = player(Position.FWD, ownership = 1.0)
        val s = Scoring.scorePlayer(p, perf(minutes = 20, yellow = 1, red = 1))
        assertEquals(-3, s.basePoints, "1 appearance - 1 yellow - 3 red")
        assertEquals(-5, s.finalPoints, "-3 x 1.5 = -4.5 rounds away from zero to -5")
    }

    // ---------- Goalkeeper and defensive detail ----------

    @Test
    fun `saves score once every three`() {
        val gk = player(Position.GK)
        assertEquals(2, Scoring.scorePlayer(gk, perf(saves = 2)).basePoints, "under three saves, nothing")
        assertEquals(3, Scoring.scorePlayer(gk, perf(saves = 5)).basePoints)
        assertEquals(4, Scoring.scorePlayer(gk, perf(saves = 6)).basePoints)
    }

    @Test
    fun `goals conceded bite every second goal and only for GK and DEF`() {
        assertEquals(1, Scoring.scorePlayer(player(Position.DEF), perf(conceded = 3)).basePoints)
        assertEquals(0, Scoring.scorePlayer(player(Position.GK), perf(conceded = 4)).basePoints)
        assertEquals(2, Scoring.scorePlayer(player(Position.MID), perf(conceded = 4)).basePoints)
    }

    @Test
    fun `goal values differ by position`() {
        val one = perf(goals = 1)
        assertEquals(8, Scoring.scorePlayer(player(Position.GK), one).basePoints)
        assertEquals(8, Scoring.scorePlayer(player(Position.DEF), one).basePoints)
        assertEquals(7, Scoring.scorePlayer(player(Position.MID), one).basePoints)
        assertEquals(6, Scoring.scorePlayer(player(Position.FWD), one).basePoints)
    }

    @Test
    fun `The Three is itemised like every other line`() {
        val s = Scoring.scorePlayer(player(Position.MID), perf(goals = 1, three = 3))
        assertEquals(10, s.basePoints)
        assertTrue(s.lines.any { it.label == "The Three" && it.points == 3 })
    }

    @Test
    fun `every point is explained`() {
        val s = Scoring.scorePlayer(player(Position.DEF), perf(venue = Venue.AWAY, cleanSheet = true, three = 2))
        assertEquals(s.basePoints, s.lines.sumOf { it.points }, "lines must always sum to the base")
        assertTrue(s.lines.isNotEmpty())
    }

    @Test
    fun `a player who did not appear scores nothing`() {
        val s = Scoring.scorePlayer(player(Position.FWD), perf(minutes = 0))
        assertEquals(0, s.basePoints)
        assertTrue(s.lines.isEmpty())
    }
}
