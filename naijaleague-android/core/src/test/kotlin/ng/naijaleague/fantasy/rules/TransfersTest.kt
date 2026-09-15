package ng.naijaleague.fantasy.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransfersTest {

    @Test
    fun `one free transfer a gameweek`() {
        assertEquals(1, Transfers.availableThisGameweek(0))
    }

    @Test
    fun `banking is capped at five`() {
        assertEquals(5, Transfers.availableThisGameweek(4))
        assertEquals(5, Transfers.availableThisGameweek(5))
        assertEquals(5, Transfers.availableThisGameweek(9))
        assertEquals(5, Transfers.bankAfter(bankedComingIn = 9, transfersMade = 0))
    }

    @Test
    fun `unused transfers roll over`() {
        assertEquals(1, Transfers.bankAfter(bankedComingIn = 0, transfersMade = 0))
        assertEquals(3, Transfers.bankAfter(bankedComingIn = 4, transfersMade = 2))
        assertEquals(0, Transfers.bankAfter(bankedComingIn = 0, transfersMade = 3))
    }

    @Test
    fun `extra transfers cost four points each`() {
        assertEquals(4, Transfers.HIT_PER_EXTRA)
        assertEquals(0, Transfers.pointsHit(bankedComingIn = 0, transfersMade = 1))
        assertEquals(4, Transfers.pointsHit(bankedComingIn = 0, transfersMade = 2))
        assertEquals(8, Transfers.pointsHit(bankedComingIn = 2, transfersMade = 5))
        assertEquals(12, Transfers.pointsHit(bankedComingIn = 0, transfersMade = 4))
    }

    @Test
    fun `unlimited windows cost nothing`() {
        assertEquals(0, Transfers.pointsHit(0, 15, unlimited = true))
    }

    @Test
    fun `the cost is always spelled out before confirming`() {
        assertTrue(Transfers.confirmationCopy(0, 2).contains("costs 4 points"))
        assertTrue(Transfers.confirmationCopy(4, 1).contains("4 left"))
        assertTrue(Transfers.confirmationCopy(0, 1).contains("last free one"))
        assertTrue(Transfers.confirmationCopy(0, 3, unlimited = true).contains("Unlimited"))
    }
}
