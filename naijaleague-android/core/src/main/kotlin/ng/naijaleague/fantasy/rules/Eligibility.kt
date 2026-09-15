package ng.naijaleague.fantasy.rules

/**
 * Who may play, and who may be given a prize.
 *
 * THE LEGAL SHAPE, and why the code is built this way. Since the Supreme Court
 * held in November 2024 that lotteries, betting and gaming are residual matters
 * reserved to the states, the federal National Lottery Act framework no longer
 * supplies a national licence. Prize competitions are licensed state by state —
 * in Lagos by the LSLGA, which issues a Promotional Permit, with a Universal
 * Reciprocity Certificate extending reach across participating states.
 *
 * Lagos defines gaming by reference to "money or money's worth", and that
 * definition bites whether or not the entrant risked anything. There is no
 * confirmed carve-out for free entry or for non-cash prizes. So this is built
 * inside the narrowest defensible envelope:
 *
 *   - FREE ENTRY ALWAYS. The user never stakes anything. There is no stake
 *     amount in this module and paid entry is not expressible in it, which is
 *     the point.
 *   - NON-CASH PRIZES ONLY for now. Cash is modelled and then gated behind both
 *     a release flag and a permit that explicitly covers cash. Two independent
 *     locks, deliberately.
 *   - EVERY PRIZE NEEDS A VALID PERMIT, non-cash included. Lagos defines gaming
 *     by reference to money's worth, which is exactly why a jersey or a match
 *     ticket needs the permit too.
 *
 * Nothing here is legal advice, and no Nigerian case law or regulator statement
 * addresses fantasy sports by name. Every gate is built to fail closed, so the
 * conservative reading is the one the code enforces.
 *
 * THE AGE RULE COMES FROM THE BRAND SYSTEM, which already settled it. The core
 * user is 16 to 60, and the approved Three Pick copy is explicit: "Cash prizes
 * are for entrants aged 18 and over. Under 18s who win take airtime, data or a
 * match ticket instead." So 16 plays, under-18 wins non-cash, 18 wins cash.
 * EligibilityTest checks the code against that string rather than against a
 * number written here twice.
 *
 * AND A DISAGREEMENT WORTH RECORDING. The earlier build's compliance work
 * reached a stricter conclusion: a hard 18+ gate at SIGNUP, not at redemption.
 * Its reasoning was that the gaming statute makes it an offence to knowingly
 * let a minor take part in a promotional competition, and that NDPA/GAID treats
 * under-18s as children whose consent basis is unsettled — so letting minors
 * play but not win would still mean processing a child's personal data under
 * that unsettled regime.
 *
 * That is not implemented, because the board approved the other reading and the
 * board owns this call. It is written down because it is a live question and the
 * next person should not have to rediscover it: moving the gate to the front
 * door is a one-line change to [MINIMUM_PLAYING_AGE].
 */
object Eligibility {

    /**
     * Youngest age that may hold an account and play.
     *
     * Set from the brand system's 16-to-60 core user. See the conflict noted on
     * this object: the earlier build's legal analysis argues for 18 here.
     */
    const val MINIMUM_PLAYING_AGE = 16

    /**
     * Youngest age that may be awarded CASH.
     *
     * Non-cash prizes have no age floor above [MINIMUM_PLAYING_AGE] — the
     * approved copy promises a winning 16-year-old airtime, data or a match
     * ticket rather than nothing.
     */
    const val MINIMUM_CASH_AGE = 18

    /**
     * The oldest age treated as a real answer.
     *
     * A gate that excludes minors correctly can still accept any absurd date
     * without complaint — a year of 0000 makes somebody two thousand years old
     * and sails through. Not a safety failure, since nobody underage is
     * admitted, but it is junk in a column the NDPA treats as personal data and
     * which the age gate reads back. A refusal is also more use to the person: a
     * mistyped year is far likelier than a supercentenarian, and "check the
     * year" beats silently storing nonsense. 120 sits above the oldest verified
     * human age.
     */
    const val MAXIMUM_PLAUSIBLE_AGE = 120

    /** What a prize can be. Cash is last because it is the gated one. */
    enum class PrizeKind(val isCash: Boolean) {
        AIRTIME(false),
        DATA_BUNDLE(false),
        MERCHANDISE(false),
        MATCH_TICKET(false),
        EXPERIENCE(false),
        CASH(true)
    }

    data class Decision(val allowed: Boolean, val reasons: List<String> = emptyList()) {
        companion object {
            val yes = Decision(true)
            fun no(vararg reasons: String) = Decision(false, reasons.toList())
        }
    }

    /**
     * A promotional permit as held on file.
     *
     * @param states the states this permit reaches, via reciprocity or directly
     */
    data class Permit(
        val authority: String,
        val reference: String,
        /** ISO dates, compared lexicographically, which is valid for ISO-8601. */
        val validFrom: String,
        val validUntil: String,
        val states: Set<String>,
        val coversCash: Boolean
    )

    data class Entrant(
        /** Whole years. Computed from a date of birth by the caller's platform clock. */
        val age: Int?,
        val state: String? = null,
        val identityVerified: Boolean = false,
        val selfExcluded: Boolean = false,
        /** Staff and their households cannot win a promotion they administer. */
        val staff: Boolean = false
    )

    /**
     * May this person hold an account and play?
     *
     * Fails closed: an absent, impossible or implausible age is a refusal, not a
     * pass. An age gate that waves through bad input is not an age gate.
     */
    fun canPlay(entrant: Entrant): Decision {
        val age = entrant.age
            ?: return Decision.no("A date of birth is required to register.")
        return when {
            age < 0 -> Decision.no("That date of birth is in the future.")
            age < MINIMUM_PLAYING_AGE ->
                Decision.no("You have to be $MINIMUM_PLAYING_AGE or over to play.")
            age > MAXIMUM_PLAUSIBLE_AGE -> Decision.no("Please check the year you entered.")
            else -> Decision.yes
        }
    }

    /** Is this permit good for this state and this kind of prize, on this date? */
    fun permitCovers(
        permit: Permit?,
        state: String?,
        kind: PrizeKind,
        today: String
    ): Decision {
        if (permit == null) return Decision.no("No promotional permit on file.")
        if (today < permit.validFrom || today > permit.validUntil) {
            return Decision.no("Permit ${permit.reference} is not valid on $today.")
        }
        if (state != null && state !in permit.states) {
            return Decision.no("Permit ${permit.reference} does not cover $state.")
        }
        if (kind.isCash && !permit.coversCash) {
            return Decision.no("Permit ${permit.reference} does not cover cash prizes.")
        }
        return Decision.yes
    }

    /**
     * May this person be awarded this prize, right now?
     *
     * Returns EVERY blocker rather than the first, so an operator sees the whole
     * outstanding list instead of clearing one to discover the next.
     *
     * THE PERMIT CHECK ALWAYS RUNS. The earlier build guarded it with a condition
     * that was only ever true for cash, so airtime, data, jerseys and match
     * tickets — the entire non-cash schedule — were awardable with no permit at
     * all, or an expired one, or one that did not cover the winner's state. Every
     * test happened to supply a valid permit, so a whole suite passed over a
     * control that never fired. That is the shape of a gap green tests conceal,
     * and it is why there is a test below for the no-permit case specifically.
     */
    fun canAward(
        entrant: Entrant,
        kind: PrizeKind,
        valueNaira: Long,
        permit: Permit?,
        today: String,
        cashEnabled: Boolean = false
    ): Decision {
        val blockers = mutableListOf<String>()

        canPlay(entrant).let { if (!it.allowed) blockers += it.reasons }

        val age = entrant.age
        if (kind.isCash) {
            // The only age floor above the playing age, and it applies to cash
            // alone. A winning 16-year-old is owed a prize, not a refusal.
            if (age != null && age in 0 until MINIMUM_CASH_AGE) {
                blockers += "Cash prizes are for entrants aged $MINIMUM_CASH_AGE and over. " +
                    "Take airtime, data or a match ticket instead."
            }
            if (!cashEnabled) blockers += "Cash prizes are not enabled in this release."
            if (!entrant.identityVerified) {
                blockers += "Identity verification is required before a cash award."
            }
        }

        permitCovers(permit, entrant.state, kind, today)
            .let { if (!it.allowed) blockers += it.reasons }

        if (entrant.selfExcluded) blockers += "This account has opted out of prizes."
        if (entrant.staff) blockers += "Staff are not eligible for prizes."
        if (valueNaira < 0) blockers += "Prize value cannot be negative."

        return Decision(blockers.isEmpty(), blockers)
    }

    /**
     * One append-only line of the prize ledger.
     *
     * Awards are append-only. A reversal is a new compensating entry, never an
     * edit or a delete — because when a regulator or a sponsor asks what was
     * given to whom and on what basis, "we changed the row" is not an answer.
     */
    data class LedgerEntry(
        val id: String,
        val type: Type,
        val entrantId: String,
        val kind: PrizeKind,
        val valueNaira: Long,
        val awardedAt: String,
        val permitReference: String?,
        /** Why this was awarded: the league position, the competition, the basis. */
        val basis: String,
        /** Set only on a reversal, naming the entry it compensates. */
        val reversalOf: String? = null
    ) {
        enum class Type { AWARD, REVERSAL }
    }

    fun award(
        id: String,
        entrantId: String,
        kind: PrizeKind,
        valueNaira: Long,
        permit: Permit?,
        basis: String,
        at: String
    ) = LedgerEntry(
        id = id,
        type = LedgerEntry.Type.AWARD,
        entrantId = entrantId,
        kind = kind,
        valueNaira = valueNaira,
        awardedAt = at,
        permitReference = permit?.reference,
        basis = basis
    )

    fun reverse(id: String, original: LedgerEntry, basis: String, at: String) = LedgerEntry(
        id = id,
        type = LedgerEntry.Type.REVERSAL,
        entrantId = original.entrantId,
        kind = original.kind,
        valueNaira = original.valueNaira,
        awardedAt = at,
        permitReference = original.permitReference,
        basis = basis,
        reversalOf = original.id
    )
}
