package ng.naijaleague.fantasy.rules

import ng.naijaleague.fantasy.data.SampleData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Auto-substitutions, and in particular the postponement case, which is the one
 * an FPL clone gets wrong in this league.
 */
class AutoSubsTest {

    private fun player(id: String, pos: Position, club: String = "ENY") =
        Player(id, "Test $id", club, pos, 5_000_000L, 10.0)

    /** 4-4-2 starting, bench of GK + DEF + MID + FWD. */
    private fun squad(): Squad {
        val xi = listOf(
            player("g1", Position.GK),
            player("d1", Position.DEF), player("d2", Position.DEF),
            player("d3", Position.DEF), player("d4", Position.DEF),
            player("m1", Position.MID), player("m2", Position.MID),
            player("m3", Position.MID), player("m4", Position.MID),
            player("f1", Position.FWD), player("f2", Position.FWD)
        )
        val bench = listOf(
            player("g2", Position.GK),
            player("d5", Position.DEF),
            player("m5", Position.MID),
            player("f3", Position.FWD)
        )
        return Squad(
            allPlayers = xi + bench,
            startingIds = xi.map { it.id }.toSet(),
            captainId = "f1",
            viceCaptainId = "m1"
        )
    }

    @Test
    fun `a postponed fixture brings the bench on and says why`() {
        // The whole reason this module is not a copy of FPL's. Three Matchday 2
        // fixtures were postponed inside the first fortnight of the season. If a
        // postponement scored nil instead of substituting, a manager whose two
        // Enyimba players were idle would field nine men for nothing they did.
        val result = AutoSubs.resolve(
            squad(),
            absences = mapOf("d1" to AutoSubs.Absence.FIXTURE_POSTPONED),
            benchOrder = listOf("d5", "m5", "f3", "g2")
        )
        assertEquals(1, result.substitutions.size)
        val sub = result.substitutions.single()
        assertEquals("d1", sub.outId)
        assertEquals("d5", sub.inId)
        assertEquals("Match postponed", sub.reason)
        assertTrue(result.finalXi.any { it.id == "d5" })
        assertFalse(result.finalXi.any { it.id == "d1" })
    }

    @Test
    fun `a player who was there and did not get on reads differently`() {
        val result = AutoSubs.resolve(
            squad(),
            absences = mapOf("m1" to AutoSubs.Absence.DID_NOT_APPEAR),
            benchOrder = listOf("m5", "d5", "f3", "g2")
        )
        assertEquals("Did not play", result.substitutions.single().reason)
    }

    @Test
    fun `only the reserve keeper comes on for the keeper`() {
        // An outfielder in goal is not a legal XI, and it is not a kindness.
        val result = AutoSubs.resolve(
            squad(),
            absences = mapOf("g1" to AutoSubs.Absence.DID_NOT_APPEAR),
            benchOrder = listOf("d5", "m5", "f3", "g2")
        )
        assertEquals("g2", result.substitutions.single().inId)
        assertEquals(1, result.finalXi.count { it.position == Position.GK })
    }

    @Test
    fun `a substitution that would break the shape is skipped for the next one`() {
        // Three defenders is the floor. With both remaining bench outfielders
        // available, losing two defenders must pull the defender on, not the
        // midfielder who happens to be earlier in bench order.
        val result = AutoSubs.resolve(
            squad(),
            absences = mapOf(
                "d1" to AutoSubs.Absence.DID_NOT_APPEAR,
                "d2" to AutoSubs.Absence.DID_NOT_APPEAR
            ),
            benchOrder = listOf("m5", "d5", "f3", "g2")
        )
        val xi = result.finalXi
        assertEquals(11, xi.size)
        assertTrue(xi.count { it.position == Position.DEF } >= 3, "shape: ${Formations.of(xi)?.label}")
        assertTrue(Formations.of(xi) != null, "the XI after substitution must be a legal shape")
    }

    @Test
    fun `Owambe substitutes nobody because everybody is already playing`() {
        val result = AutoSubs.resolve(
            squad(),
            absences = mapOf("d1" to AutoSubs.Absence.FIXTURE_POSTPONED),
            activeChip = Chip.OWAMBE
        )
        assertEquals(emptyList(), result.substitutions)
        assertEquals(15, result.finalXi.size, "under Owambe all fifteen score")
    }

    @Test
    fun `a missing report is read as a postponement, not as a blank`() {
        // If the scorer never filed at all, the likeliest reason in this league
        // is that the match was not played. Zero filed minutes is the other case.
        val absences = AutoSubs.absencesFrom(SampleData.squad, SampleData.gameweek12)
        assertEquals(
            AutoSubs.Absence.DID_NOT_APPEAR,
            absences["RIV-andy"],
            "the reserve keeper has a report filed at zero minutes"
        )
        val partial = SampleData.gameweek12.filterKeys { it != "PH-NAS-def3" }
        val fromPartial = AutoSubs.absencesFrom(SampleData.squad, partial)
        assertEquals(AutoSubs.Absence.FIXTURE_POSTPONED, fromPartial["PH-NAS-def3"])
    }

    @Test
    fun `nobody absent means nobody substituted`() {
        val result = AutoSubs.resolve(squad(), absences = emptyMap())
        assertEquals(emptyList(), result.substitutions)
        assertEquals(11, result.finalXi.size)
    }

    @Test
    fun `an absence with no legal replacement leaves the XI short rather than illegal`() {
        // Every bench outfielder also absent. Ten men is the honest outcome; a
        // tenth-man XI that claims to be legal is not.
        val result = AutoSubs.resolve(
            squad(),
            absences = mapOf(
                "f1" to AutoSubs.Absence.DID_NOT_APPEAR,
                "d5" to AutoSubs.Absence.DID_NOT_APPEAR,
                "m5" to AutoSubs.Absence.DID_NOT_APPEAR,
                "f3" to AutoSubs.Absence.DID_NOT_APPEAR
            )
        )
        assertEquals(emptyList(), result.substitutions)
        assertTrue(result.finalXi.any { it.id == "f1" }, "the absent starter stays in the XI slot")
    }

    @Test
    fun `Ground Man does not pay a club that is only nominally at home`() {
        // Rangers spend all of 2026/27 in Abeokuta and Doma United in Gombe. A
        // 1.5x home multiplier on those fixtures would reward the one thing the
        // chip is about, in the two cases where it is not true.
        val p = player("x1", Position.FWD, club = "RAN")
        val atRealHome = Performance("x1", Venue.HOME, minutes = 90, goals = 1)
        val atAbeokuta = Performance("x1", Venue.HOME, atNeutralGround = true, minutes = 90, goals = 1)

        val paid = Scoring.scorePlayer(p, atRealHome, activeChip = Chip.GROUND_MAN)
        val notPaid = Scoring.scorePlayer(p, atAbeokuta, activeChip = Chip.GROUND_MAN)

        assertTrue(paid.groundManApplied)
        assertFalse(notPaid.groundManApplied)
        assertTrue(paid.finalPoints > notPaid.finalPoints)
        assertNull(
            Formations.of(emptyList()),
            "sanity: an empty XI is not a formation"
        )
    }
}
