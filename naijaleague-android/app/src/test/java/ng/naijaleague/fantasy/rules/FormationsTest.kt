package ng.naijaleague.fantasy.rules

import ng.naijaleague.fantasy.data.SampleData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The formation list must agree with the validator, because it is derived from
 * the same constants. These tests exist to catch the day somebody stops
 * deriving it.
 */
class FormationsTest {

    private fun xi(def: Int, mid: Int, fwd: Int): List<Player> = buildList {
        add(Player("g", "G", "ENY", Position.GK, 5_000_000L, 1.0))
        repeat(def) { add(Player("d$it", "D$it", "ENY", Position.DEF, 5_000_000L, 1.0)) }
        repeat(mid) { add(Player("m$it", "M$it", "ENY", Position.MID, 5_000_000L, 1.0)) }
        repeat(fwd) { add(Player("f$it", "F$it", "ENY", Position.FWD, 5_000_000L, 1.0)) }
    }

    @Test
    fun `every enumerated formation is one the validator accepts`() {
        // This is the contract. If it ever fails, the picker is offering a shape
        // the app will then refuse to save, and the manager gets "no" with no why.
        assertTrue(Formations.legal.isNotEmpty())
        Formations.legal.forEach { shape ->
            val squad = Squad(
                allPlayers = xi(shape.defenders, shape.midfielders, shape.forwards) +
                    listOf(
                        Player("g2", "G2", "RIV", Position.GK, 4_000_000L, 1.0),
                        Player("bd", "BD", "RIV", Position.DEF, 4_000_000L, 1.0),
                        Player("bm", "BM", "IKO", Position.MID, 4_000_000L, 1.0),
                        Player("bf", "BF", "IKO", Position.FWD, 4_000_000L, 1.0)
                    ),
                startingIds = xi(shape.defenders, shape.midfielders, shape.forwards)
                    .map { it.id }.toSet(),
                captainId = "g"
            )
            assertEquals(
                emptyList(), squad.validateStartingXi(),
                "${shape.label} is enumerated as legal but the validator rejects it"
            )
        }
    }

    @Test
    fun `every shape the validator accepts is enumerated`() {
        // The other direction: a legal shape missing from the list is a formation
        // the picker silently hides.
        for (d in 0..11) for (m in 0..11) {
            val f = 10 - d - m
            if (f < 0) continue
            val lineup = xi(d, m, f)
            if (lineup.size != SquadRules.STARTING_XI) continue
            val legalToValidator = Squad(
                allPlayers = lineup, startingIds = lineup.map { it.id }.toSet(), captainId = "g"
            ).validateStartingXi().isEmpty()
            val enumerated = Formations.legal.any {
                it.defenders == d && it.midfielders == m && it.forwards == f
            }
            assertEquals(legalToValidator, enumerated, "$d-$m-$f disagrees")
        }
    }

    @Test
    fun `the familiar shapes are all there and the silly ones are not`() {
        val labels = Formations.labels
        listOf("4-4-2", "4-3-3", "3-5-2", "3-4-3", "5-3-2", "5-4-1", "4-5-1").forEach {
            assertTrue(it in labels, "$it should be playable")
        }
        listOf("2-5-3", "6-3-1", "4-2-4").forEach {
            assertTrue(it !in labels, "$it should not be playable")
        }
    }

    @Test
    fun `the list is ordered defenders then midfielders ascending`() {
        val keys = Formations.legal.map { it.defenders * 100 + it.midfielders }
        assertEquals(keys.sorted(), keys, "picker order must be stable and readable")
    }

    @Test
    fun `an XI that is not a legal shape is not given a formation`() {
        assertNull(Formations.of(xi(2, 5, 3)), "two at the back is not a formation this game allows")
        assertNull(Formations.of(xi(4, 4, 1)), "ten players is not an XI")
    }

    @Test
    fun `the example squad plays a shape it can actually reach`() {
        val squad = SampleData.squad
        val shape = Formations.of(squad.startingXi)
        assertEquals("4-3-3", shape?.label)
        assertTrue(Formations.reachable(squad, shape!!))
        // And its 2/5/5/3 can reach every shape in the list, which is what that
        // quota is for.
        assertEquals(
            Formations.legal, Formations.reachableFrom(squad),
            "a full 15 should be able to field any legal shape"
        )
    }
}
