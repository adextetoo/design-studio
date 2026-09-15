package ng.naijaleague.fantasy.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SquadTest {

    /** Eight clubs, because the two-per-club cap forces at least eight (§08). */
    private val clubs = listOf("ENY", "RAN", "REM", "KAN", "3SC", "RIV", "PLA", "LOB")

    private fun squadOf(
        overrideClubs: List<String>? = null,
        priceEach: Long = 6_000_000
    ): Squad {
        val shape = listOf(
            Position.GK, Position.GK,
            Position.DEF, Position.DEF, Position.DEF, Position.DEF, Position.DEF,
            Position.MID, Position.MID, Position.MID, Position.MID, Position.MID,
            Position.FWD, Position.FWD, Position.FWD
        )
        val assigned = overrideClubs ?: List(15) { clubs[it / 2] }
        val players = shape.mapIndexed { i, pos ->
            Player("p$i", "Player $i", assigned[i], pos, priceEach, 12.0)
        }
        // A legal 4-3-3: 1 GK, 4 DEF, 3 MID, 3 FWD
        val starting = setOf("p0", "p2", "p3", "p4", "p5", "p7", "p8", "p9", "p12", "p13", "p14")
        return Squad(players, starting, captainId = "p12")
    }

    @Test
    fun `a legal squad validates clean`() {
        val squad = squadOf()
        assertEquals(emptyList(), squad.validate(), "unexpected: ${squad.validate()}")
        assertTrue(squad.isValid)
        assertEquals(15, squad.allPlayers.size)
        assertEquals(11, squad.startingXi.size)
        assertEquals(4, squad.bench.size)
    }

    @Test
    fun `formation is read off the XI`() {
        assertEquals("4-3-3", squadOf().formation)
    }

    @Test
    fun `budget is one hundred million naira`() {
        assertEquals(100_000_000L, SquadRules.BUDGET_NAIRA)
        val squad = squadOf(priceEach = 6_000_000)
        assertEquals(90_000_000L, squad.spend)
        assertEquals(10_000_000L, squad.budgetRemaining)
    }

    @Test
    fun `going over budget says how much by`() {
        val squad = squadOf(priceEach = 7_000_000) // 105m
        val v = squad.validate().single { it.code == "BUDGET" }
        assertTrue(v.message.contains("5,000,000"), "got: ${v.message}")
        assertFalse(squad.isValid)
    }

    @Test
    fun `three from one club is rejected - the cap is two not three`() {
        assertEquals(2, SquadRules.MAX_PER_CLUB)
        val tooMany = List(15) { if (it < 3) "ENY" else clubs[(it % 7) + 1] }
        val squad = squadOf(overrideClubs = tooMany)
        assertTrue(squad.validate().any { it.code == "CLUB_CAP" })
    }

    @Test
    fun `a squad must span at least eight clubs`() {
        val squad = squadOf()
        assertTrue(squad.allPlayers.map { it.clubId }.distinct().size >= 8)
    }

    @Test
    fun `position quotas are two five five three`() {
        assertEquals(2, SquadRules.QUOTA.getValue(Position.GK))
        assertEquals(5, SquadRules.QUOTA.getValue(Position.DEF))
        assertEquals(5, SquadRules.QUOTA.getValue(Position.MID))
        assertEquals(3, SquadRules.QUOTA.getValue(Position.FWD))
        assertEquals(15, SquadRules.QUOTA.values.sum())
    }

    @Test
    fun `a captain on the bench is caught`() {
        val squad = squadOf().copy(captainId = "p1") // p1 is a benched goalkeeper
        assertTrue(squad.validate().any { it.code == "CAPTAIN_ON_BENCH" })
    }

    @Test
    fun `no captain is caught`() {
        val squad = squadOf().copy(captainId = null)
        val v = squad.validate().single { it.code == "NO_CAPTAIN" }
        assertTrue(v.message.contains("double"))
    }

    @Test
    fun `an XI cannot field two goalkeepers`() {
        val squad = squadOf().copy(
            startingIds = setOf("p0", "p1", "p2", "p3", "p4", "p7", "p8", "p9", "p12", "p13", "p14")
        )
        assertTrue(squad.validateStartingXi().any { it.code == "XI_MAX_GK" })
    }

    @Test
    fun `an XI needs at least three defenders`() {
        val squad = squadOf().copy(
            startingIds = setOf("p0", "p2", "p3", "p7", "p8", "p9", "p10", "p11", "p12", "p13", "p14")
        )
        assertTrue(squad.validateStartingXi().any { it.code == "XI_MIN_DEF" })
    }

    @Test
    fun `error messages tell the manager what to do`() {
        val squad = Squad(squadOf().allPlayers.take(13), setOf("p0"), "p0")
        val v = squad.validate().single { it.code == "SQUAD_SIZE" }
        assertTrue(v.message.contains("Add 2 more"), "got: ${v.message}")
    }

    @Test
    fun `Owambe counts the bench and the plain gameweek does not`() {
        val squad = squadOf()
        val performances = squad.allPlayers.associate { p ->
            p.id to Performance(p.id, Venue.HOME, minutes = 90, goals = 0)
        }
        val plain = Scoring.scoreSquad(squad, performances, activeChip = null)
        val owambe = Scoring.scoreSquad(squad, performances, activeChip = Chip.OWAMBE)
        assertEquals(11, plain.playerScores.size)
        assertEquals(
            24, plain.points,
            "10 starters at 2 appearance points, plus the captain's 2 doubled to 4"
        )
        assertEquals(15, owambe.playerScores.size)
        assertEquals(
            32, owambe.points,
            "14 players at 2, plus the captain's 2 doubled to 4 — the four extra come off the bench"
        )
        assertEquals(8, owambe.points - plain.points, "Owambe is worth the four bench appearances")
        assertTrue(owambe.benchCounted)
        assertFalse(plain.benchCounted)
    }

    @Test
    fun `money formats as naira`() {
        assertEquals("₦100,000,000", Money.format(100_000_000))
        assertEquals("₦7,500,000", Money.format(7_500_000))
        assertEquals("12.5m", Money.compact(12_500_000))
        assertEquals("8m", Money.compact(8_000_000))
        assertEquals("10.2m", Money.compact(10_200_000))
    }
}
