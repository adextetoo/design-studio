package ng.naijaleague.fantasy.rules

/**
 * Where a stat came from, and what that entitles it to overwrite.
 *
 * WHY THE APP NEEDS THIS AT ALL. The NPFL has no single statistics feed. Data
 * arrives from several places of very different quality, and recording only the
 * number throws away the thing that decides conflicts: a value read off a
 * matchday teamsheet and a value someone inferred from a highlights package are
 * not equally good, and when they disagree the system must not settle it by
 * whichever was written last. This is the spine under the provisional-until-
 * Monday challenge window in §05 — a challenge is only answerable if you know
 * what the original claim was based on.
 *
 * FOUR TIERS, in descending authority for what they are entitled to speak to:
 *
 *   1. OFFICIAL — the league player register and the matchday teamsheet.
 *      Authoritative, but NARROW. A teamsheet settles who started, who came on
 *      and when, and therefore appearances and minutes. It says nothing
 *      whatever about assists or saves and must not be allowed to claim it
 *      does. One sheet of paper resolves most of what hours of video review
 *      would be spent extracting, which is why this tier is worth chasing
 *      before any of the others.
 *
 *   2. LIVE — a steward capturing the match as it is played, through the
 *      structured entry form, under dual entry. The primary source for in-play
 *      events and the workhorse of the pipeline. Capturing live costs roughly a
 *      third of reviewing the same match on video, and the points settle at the
 *      whistle rather than two days later.
 *
 *   3. SIGNAL — club social media and similar public posts. Deliberately NOT a
 *      stat source. A club announcing a signing is a reason to go and check the
 *      register; it is not evidence of who is registered. Clubs announce
 *      arrivals loudly and departures quietly or not at all, so a pipeline that
 *      trusted these posts would silently accumulate players who have already
 *      left. This tier raises tasks. [canWriteStats] refuses it.
 *
 *   4. VIDEO — post-match review of recorded footage. Expensive, and it arrives
 *      late, so it is reserved for ADJUDICATION: settling a dispute where two
 *      live stewards disagreed. In that narrow role it outranks the live
 *      entries it is adjudicating, because somebody has gone back and looked.
 *      It never outranks a teamsheet on the teamsheet's own ground.
 *
 * So the precedence for in-play events is VIDEO > LIVE, and for appearance data
 * OFFICIAL beats everything. That is not one linear ranking, which is exactly
 * why authority is modelled per category rather than as a single number.
 */
object Provenance {

    enum class Tier { OFFICIAL, LIVE, SIGNAL, VIDEO }

    /** The two kinds of fact, which different sources are good at. */
    enum class Category {
        /** Did he play, and for how long. A teamsheet settles this outright. */
        APPEARANCE,

        /** Goals, assists, saves, cards. What happened on the pitch. */
        IN_PLAY
    }

    /** May a source at this tier write a stat at all? */
    fun canWriteStats(tier: Tier): Boolean = tier != Tier.SIGNAL

    /**
     * Authority of a tier over one category. Higher wins; zero means silent.
     *
     * OFFICIAL is 100 on appearances and 0 on in-play events, which is the whole
     * point of modelling this per category: a teamsheet is the best evidence
     * there is about who played and no evidence at all about who assisted.
     */
    fun authority(tier: Tier, category: Category): Int = when (category) {
        Category.APPEARANCE -> when (tier) {
            Tier.OFFICIAL -> 100
            Tier.VIDEO -> 60
            Tier.LIVE -> 50
            Tier.SIGNAL -> 0
        }
        Category.IN_PLAY -> when (tier) {
            Tier.VIDEO -> 80
            Tier.LIVE -> 70
            Tier.OFFICIAL -> 0
            Tier.SIGNAL -> 0
        }
    }

    /**
     * May [incoming] overwrite a value already recorded by [existing]?
     *
     * Equal authority does NOT overwrite. Two live stewards disagreeing is the
     * dispute that video review exists to settle; letting the second entry win
     * by arriving second would quietly discard the disagreement instead of
     * escalating it.
     */
    fun mayOverwrite(existing: Tier, incoming: Tier, category: Category): Boolean =
        canWriteStats(incoming) &&
            authority(incoming, category) > authority(existing, category)

    /**
     * Does a value from this tier need a second pair of eyes before it counts?
     *
     * Live capture is entered under dual entry, so a single live entry is a
     * claim rather than a result. That is what keeps the challenge window in
     * §05 answerable rather than decorative.
     */
    fun needsSecondEntry(tier: Tier): Boolean = tier == Tier.LIVE

    /** What a tier is called on screen, when the app shows where a number came from. */
    fun label(tier: Tier): String = when (tier) {
        Tier.OFFICIAL -> "Teamsheet"
        Tier.LIVE -> "Scorer at the ground"
        Tier.SIGNAL -> "Club announcement"
        Tier.VIDEO -> "Video review"
    }
}
