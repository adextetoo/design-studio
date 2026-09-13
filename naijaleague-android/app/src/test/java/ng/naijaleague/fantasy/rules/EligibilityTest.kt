package ng.naijaleague.fantasy.rules

import ng.naijaleague.fantasy.rules.Eligibility.Entrant
import ng.naijaleague.fantasy.rules.Eligibility.Permit
import ng.naijaleague.fantasy.rules.Eligibility.PrizeKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Every gate here must fail closed. A compliance check that waves through bad
 * input is worse than no check, because it reads as one in review.
 */
class EligibilityTest {

    private val lagosPermit = Permit(
        authority = "LSLGA",
        reference = "PP-2026-0417",
        validFrom = "2026-08-01",
        validUntil = "2027-05-31",
        states = setOf("Lagos State", "Oyo State", "Enugu State"),
        coversCash = false
    )

    private val today = "2026-11-16"

    private fun adult(
        state: String? = "Lagos State",
        verified: Boolean = false,
        excluded: Boolean = false,
        staff: Boolean = false
    ) = Entrant(age = 27, state = state, identityVerified = verified,
        selfExcluded = excluded, staff = staff)

    @Test
    fun `a missing age is a refusal, not a pass`() {
        val decision = Eligibility.canPlay(Entrant(age = null))
        assertFalse(decision.allowed)
        assertTrue(decision.reasons.single().contains("date of birth"))
    }

    @Test
    fun `the playing gate follows the brand system's sixteen`() {
        assertEquals(16, Eligibility.MINIMUM_PLAYING_AGE)
        assertTrue(Eligibility.canPlay(Entrant(age = 16)).allowed)
        assertFalse(Eligibility.canPlay(Entrant(age = 15)).allowed)
        // And the oldest end of the core user is well inside the plausible range.
        assertTrue(Eligibility.canPlay(Entrant(age = 60)).allowed)
    }

    @Test
    fun `a two-thousand-year-old is refused and told to check the year`() {
        val decision = Eligibility.canPlay(Entrant(age = 2026))
        assertFalse(decision.allowed)
        assertTrue(decision.reasons.single().contains("check the year"))
        assertFalse(Eligibility.canPlay(Entrant(age = -1)).allowed)
    }

    @Test
    fun `a sixteen year old plays, wins a match ticket, and does not win cash`() {
        // Straight out of the approved Three Pick copy. A winning 16-year-old is
        // owed a prize, not a refusal — just not a cash one.
        val teenager = Entrant(age = 16, state = "Lagos State", identityVerified = true)
        assertTrue(Eligibility.canPlay(teenager).allowed)

        val ticket = Eligibility.canAward(
            teenager, PrizeKind.MATCH_TICKET, 15_000L, lagosPermit, today
        )
        assertTrue(ticket.allowed, "blockers: ${ticket.reasons}")

        val cash = Eligibility.canAward(
            teenager, PrizeKind.CASH, 50_000L, lagosPermit.copy(coversCash = true),
            today, cashEnabled = true
        )
        assertFalse(cash.allowed)
        assertTrue(cash.reasons.any { it.contains("${Eligibility.MINIMUM_CASH_AGE} and over") })
    }

    @Test
    fun `a jersey still needs a permit`() {
        // The bug this port fixes. The earlier guard was only ever true for cash,
        // so the entire non-cash schedule was awardable with no permit at all —
        // and every test supplied one, so the suite passed over a dead control.
        val noPermit = Eligibility.canAward(
            adult(), PrizeKind.MERCHANDISE, 15_000L, permit = null, today = today
        )
        assertFalse(noPermit.allowed, "non-cash prizes must not bypass the permit")
        assertTrue(noPermit.reasons.any { it.contains("No promotional permit") })

        PrizeKind.entries.filterNot { it.isCash }.forEach { kind ->
            assertFalse(
                Eligibility.canAward(adult(), kind, 1_000L, null, today).allowed,
                "$kind was awardable without a permit"
            )
        }
    }

    @Test
    fun `an expired permit covers nothing`() {
        val expired = Eligibility.canAward(
            adult(), PrizeKind.AIRTIME, 1_000L, lagosPermit, today = "2027-06-01"
        )
        assertFalse(expired.allowed)
        assertTrue(expired.reasons.any { it.contains("not valid on 2027-06-01") })
    }

    @Test
    fun `a permit that does not reach the winner's state does not cover them`() {
        val outside = Eligibility.canAward(
            adult(state = "Kano State"), PrizeKind.DATA_BUNDLE, 2_000L, lagosPermit, today
        )
        assertFalse(outside.allowed)
        assertTrue(outside.reasons.any { it.contains("does not cover Kano State") })
    }

    @Test
    fun `cash needs both the release flag and a cash-covering permit`() {
        // Two independent locks. Neither alone opens it.
        val flagOnly = Eligibility.canAward(
            adult(verified = true), PrizeKind.CASH, 500_000L, lagosPermit, today,
            cashEnabled = true
        )
        assertFalse(flagOnly.allowed, "the permit does not cover cash")
        assertTrue(flagOnly.reasons.any { it.contains("does not cover cash") })

        val permitOnly = Eligibility.canAward(
            adult(verified = true), PrizeKind.CASH, 500_000L,
            lagosPermit.copy(coversCash = true), today, cashEnabled = false
        )
        assertFalse(permitOnly.allowed, "cash is off in this release")
        assertTrue(permitOnly.reasons.any { it.contains("not enabled in this release") })

        val both = Eligibility.canAward(
            adult(verified = true), PrizeKind.CASH, 500_000L,
            lagosPermit.copy(coversCash = true), today, cashEnabled = true
        )
        assertTrue(both.allowed, "blockers: ${both.reasons}")
    }

    @Test
    fun `cash also needs identity verification`() {
        val unverified = Eligibility.canAward(
            adult(verified = false), PrizeKind.CASH, 500_000L,
            lagosPermit.copy(coversCash = true), today, cashEnabled = true
        )
        assertFalse(unverified.allowed)
        assertTrue(unverified.reasons.any { it.contains("Identity verification") })
    }

    @Test
    fun `opting out and staffing both block, and every blocker is reported`() {
        val decision = Eligibility.canAward(
            Entrant(age = 15, state = "Kano State", selfExcluded = true, staff = true),
            PrizeKind.CASH, -5L, permit = null, today = today
        )
        assertFalse(decision.allowed)
        // The whole outstanding list, not the first thing wrong: too young to
        // play, too young for cash, cash off, unverified, no permit, opted out,
        // staff, negative value.
        assertTrue(decision.reasons.size >= 7, "only got: ${decision.reasons}")
        assertTrue(decision.reasons.any { it.contains("opted out") })
        assertTrue(decision.reasons.any { it.contains("Staff") })
        assertTrue(decision.reasons.any { it.contains("cannot be negative") })
    }

    @Test
    fun `a valid non-cash award passes`() {
        val ok = Eligibility.canAward(
            adult(), PrizeKind.MATCH_TICKET, 25_000L, lagosPermit, today
        )
        assertTrue(ok.allowed, "blockers: ${ok.reasons}")
    }

    @Test
    fun `a reversal is a new entry, never an edit`() {
        val original = Eligibility.award(
            id = "aw-1", entrantId = "u-9", kind = PrizeKind.AIRTIME, valueNaira = 2_000L,
            permit = lagosPermit, basis = "Gameweek 12 top scorer, Lagos mini-league",
            at = "2026-11-17T09:00:00Z"
        )
        val reversal = Eligibility.reverse(
            id = "rv-1", original = original, basis = "Duplicate award",
            at = "2026-11-18T09:00:00Z"
        )
        assertEquals(Eligibility.LedgerEntry.Type.AWARD, original.type)
        assertEquals(Eligibility.LedgerEntry.Type.REVERSAL, reversal.type)
        assertEquals("aw-1", reversal.reversalOf)
        assertEquals(original.entrantId, reversal.entrantId)
        assertEquals(original.valueNaira, reversal.valueNaira)
        assertEquals(lagosPermit.reference, reversal.permitReference)
        assertNull(original.reversalOf)
    }

    @Test
    fun `there is no way to express a paid entry`() {
        // Free entry always. The absence is the control: if a stake ever becomes
        // representable, this test is the thing that should have stopped it.
        val fields = Entrant::class.java.declaredFields.map { it.name.lowercase() }
        assertTrue(
            fields.none { it.contains("stake") || it.contains("wager") || it.contains("bet") },
            "an entrant must not be able to carry a stake: $fields"
        )
    }
}
