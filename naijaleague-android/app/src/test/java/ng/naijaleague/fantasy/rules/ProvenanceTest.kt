package ng.naijaleague.fantasy.rules

import ng.naijaleague.fantasy.rules.Provenance.Category
import ng.naijaleague.fantasy.rules.Provenance.Tier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Authority is per category, not one ranking. These tests pin the cases where a
 * single linear ranking would get it wrong.
 */
class ProvenanceTest {

    @Test
    fun `a club announcement can never write a stat`() {
        // Clubs announce arrivals loudly and departures quietly or not at all. A
        // pipeline that trusted these posts would silently accumulate players who
        // have already left — which is exactly what Enyimba's own squad page did
        // with Ekene Awazie.
        assertFalse(Provenance.canWriteStats(Tier.SIGNAL))
        Category.entries.forEach {
            assertEquals(0, Provenance.authority(Tier.SIGNAL, it))
            Tier.entries.forEach { existing ->
                assertFalse(Provenance.mayOverwrite(existing, Tier.SIGNAL, it))
            }
        }
    }

    @Test
    fun `a teamsheet settles who played and says nothing about assists`() {
        assertEquals(100, Provenance.authority(Tier.OFFICIAL, Category.APPEARANCE))
        assertEquals(0, Provenance.authority(Tier.OFFICIAL, Category.IN_PLAY))
        assertTrue(Provenance.mayOverwrite(Tier.VIDEO, Tier.OFFICIAL, Category.APPEARANCE))
        assertFalse(
            Provenance.mayOverwrite(Tier.LIVE, Tier.OFFICIAL, Category.IN_PLAY),
            "a teamsheet must not be allowed to overwrite an assist it never mentioned"
        )
    }

    @Test
    fun `video adjudicates in-play events and does not outrank a teamsheet`() {
        assertTrue(Provenance.mayOverwrite(Tier.LIVE, Tier.VIDEO, Category.IN_PLAY))
        assertFalse(Provenance.mayOverwrite(Tier.OFFICIAL, Tier.VIDEO, Category.APPEARANCE))
    }

    @Test
    fun `equal authority does not overwrite, so a disagreement escalates`() {
        // Two live stewards disagreeing is the dispute video review exists to
        // settle. Letting the second entry win by arriving second would discard
        // the disagreement instead of raising it.
        Category.entries.forEach {
            assertFalse(Provenance.mayOverwrite(Tier.LIVE, Tier.LIVE, it))
        }
    }

    @Test
    fun `live capture needs a second pair of eyes`() {
        assertTrue(Provenance.needsSecondEntry(Tier.LIVE))
        assertFalse(Provenance.needsSecondEntry(Tier.OFFICIAL))
        assertFalse(Provenance.needsSecondEntry(Tier.VIDEO))
    }

    @Test
    fun `every tier has a label a manager would understand`() {
        Tier.entries.forEach {
            val label = Provenance.label(it)
            assertTrue(label.isNotBlank())
            assertFalse(
                label.any { ch -> ch.isUpperCase() && label.indexOf(ch) > 0 && label[label.indexOf(ch) - 1] == '_' },
                "$label reads like an enum name, not English"
            )
            assertFalse(label == it.name, "${it.name} has no human label")
        }
    }
}
