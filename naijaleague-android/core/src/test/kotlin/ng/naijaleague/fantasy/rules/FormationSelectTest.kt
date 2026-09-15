package ng.naijaleague.fantasy.rules

import ng.naijaleague.fantasy.data.SampleData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Changing shape has to produce a legal eleven every time, or the picker is
 * offering the manager an error.
 */
class FormationSelectTest {

    private val squad = SampleData.squad

    @Test
    fun `every reachable formation produces a legal XI`() {
        // The contract the picker depends on. If this fails, some shape on the
        // carousel saves a squad the validator then rejects.
        Formations.reachableFrom(squad).forEach { shape ->
            val xi = Formations.selectXi(squad, shape)
            assertNotNull(xi, "${shape.label} is reachable but selected nothing")
            val moved = squad.copy(startingIds = xi)
            assertEquals(
                emptyList(), moved.validate(),
                "${shape.label} produced an illegal squad: ${moved.validate()}"
            )
            assertEquals(shape.label, moved.formation)
        }
    }

    @Test
    fun `an unreachable shape selects nothing rather than guessing`() {
        // Two forwards is all this squad has spare for a front three plus the
        // quota, so a shape needing more than it owns must refuse.
        val thin = squad.copy(
            allPlayers = squad.allPlayers.filterNot { it.position == Position.FWD }
        )
        Formations.legal.filter { it.forwards > 0 }.forEach {
            assertNull(Formations.selectXi(thin, it), "${it.label} should be unreachable")
        }
    }

    @Test
    fun `changing shape never benches the captain`() {
        // A captain on the bench scores nothing and the validator rejects it.
        Formations.reachableFrom(squad).forEach { shape ->
            val xi = Formations.selectXi(squad, shape)!!
            assertTrue(
                squad.captainId in xi,
                "${shape.label} dropped the captain"
            )
        }
    }

    @Test
    fun `moving one line keeps everybody it can`() {
        // 4-3-3 to 4-4-2 is one change, not eleven. Continuity is the difference
        // between adjusting a team and being handed a new one.
        val from = Formations.of(squad.startingXi)!!
        assertEquals("4-3-3", from.label)
        val to = Formations.legal.first { it.label == "4-4-2" }
        val xi = Formations.selectXi(squad, to)!!
        val kept = squad.startingIds.count { it in xi }
        assertEquals(10, kept, "only the swapped forward should change")
    }

    @Test
    fun `the same squad and shape always give the same eleven`() {
        Formations.reachableFrom(squad).forEach { shape ->
            assertEquals(
                Formations.selectXi(squad, shape),
                Formations.selectXi(squad, shape),
                "${shape.label} is not deterministic"
            )
        }
    }

    @Test
    fun `a full fifteen can reach every legal shape`() {
        // What the 2/5/5/3 quota is for.
        assertEquals(Formations.legal, Formations.reachableFrom(squad))
    }
}
