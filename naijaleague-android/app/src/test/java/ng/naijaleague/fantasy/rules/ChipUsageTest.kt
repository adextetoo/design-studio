package ng.naijaleague.fantasy.rules

import ng.naijaleague.fantasy.rules.ChipUsage.Play
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** One of each chip, and never two in the same week. */
class ChipUsageTest {

    @Test
    fun `a fresh season has all five available`() {
        assertEquals(5, Chip.entries.size)
        assertEquals(Chip.entries.toList(), ChipUsage.remaining(emptyList()))
        assertEquals(Chip.entries.toList(), ChipUsage.playableIn(1, emptyList()))
    }

    @Test
    fun `a chip played once is gone, and the message says which week`() {
        val history = listOf(Play(Chip.JARA, 4))
        val decision = ChipUsage.canPlay(Chip.JARA, 12, history)
        assertFalse(decision.allowed)
        assertTrue(decision.reasons.single().contains("gameweek 4"))
        assertTrue(decision.reasons.single().contains("Jara"))
        assertFalse(Chip.JARA in ChipUsage.remaining(history))
    }

    @Test
    fun `one chip per gameweek, and it names the one already there`() {
        val history = listOf(Play(Chip.OWAMBE, 12))
        val decision = ChipUsage.canPlay(Chip.GROUND_MAN, 12, history)
        assertFalse(decision.allowed)
        assertTrue(decision.reasons.single().contains("Owambe"))
        // The same chip is fine in a different week.
        assertTrue(ChipUsage.canPlay(Chip.GROUND_MAN, 13, history).allowed)
    }

    @Test
    fun `both reasons are given when both apply`() {
        val history = listOf(Play(Chip.WAKA_PASS, 7), Play(Chip.ASO_EBI, 19))
        val decision = ChipUsage.canPlay(Chip.WAKA_PASS, 19, history)
        assertFalse(decision.allowed)
        assertEquals(2, decision.reasons.size, "got: ${decision.reasons}")
    }

    @Test
    fun `a chip cannot be played outside a real gameweek`() {
        assertFalse(ChipUsage.canPlay(Chip.JARA, 0, emptyList()).allowed)
        assertFalse(ChipUsage.canPlay(Chip.JARA, -3, emptyList()).allowed)
    }

    @Test
    fun `the picker offers exactly what the rules allow`() {
        // Built from canPlay, so this is the contract that keeps the greyed-out
        // chip and the refused save agreeing with each other.
        val history = listOf(Play(Chip.JARA, 3), Play(Chip.OWAMBE, 9))
        val playable = ChipUsage.playableIn(14, history)
        assertEquals(
            Chip.entries.filter { ChipUsage.canPlay(it, 14, history).allowed },
            playable
        )
        assertFalse(Chip.JARA in playable)
        assertFalse(Chip.OWAMBE in playable)
        assertEquals(3, playable.size)
        // And nothing is playable in a week that already has a chip.
        assertEquals(emptyList(), ChipUsage.playableIn(9, history))
    }

    @Test
    fun `the chip on a given week can be read back`() {
        val history = listOf(Play(Chip.GROUND_MAN, 11))
        assertEquals(Chip.GROUND_MAN, ChipUsage.playedOn(11, history))
        assertNull(ChipUsage.playedOn(12, history))
    }

    @Test
    fun `a full season uses each chip once in a different week`() {
        var history = emptyList<ChipUsage.Play>()
        Chip.entries.forEachIndexed { i, chip ->
            val gw = (i + 1) * 5
            assertTrue(ChipUsage.canPlay(chip, gw, history).allowed, "${chip.chipName} at $gw")
            history = history + Play(chip, gw)
        }
        assertEquals(emptyList(), ChipUsage.remaining(history))
        assertEquals(emptyList(), ChipUsage.playableIn(38, history))
    }
}
