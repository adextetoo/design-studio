package ng.naijaleague.fantasy.data

import ng.naijaleague.fantasy.rules.Position

/**
 * Real NPFL players, and where each name came from.
 *
 * WHY THIS FILE EXISTS AT ALL. A name in a fantasy game is a claim about a
 * real person. "Where did you get that" has to be answerable without
 * archaeology, and a wrong answer is not a cosmetic bug — it is telling a
 * stranger's employer that he plays somewhere he does not.
 *
 * THE SOURCING STANDARD, and it is narrower than it looks. A squad may be
 * entered from a CLUB-OFFICIAL source — the club's own site or its own
 * channel — or from a DATED REPORT that names a player in the body of the
 * story. Nothing else qualifies. What is rejected:
 *
 *   - AGGREGATORS (BeSoccer, FotMob, Sofascore, worldfootball, tribuna,
 *     AiScore, Goal.com). Complete lists, no timestamps, and a season label in
 *     a dropdown is a claim rather than a date. Checked against dated
 *     reporting they were wrong about positions, spellings and which club a
 *     player was at.
 *   - FAN SITES, however good. Kano Pillars' best-looking site states in its
 *     own footer that it is not affiliated with the club.
 *   - LAPSED CLUB DOMAINS. The address Shooting Stars still publish for
 *     contact now serves an online casino. A domain that was once official is
 *     not permanently trustworthy.
 *   - WIKIPEDIA SQUAD SECTIONS. Enyimba's states it is current as of February
 *     2023, and NPFL squads turn over heavily. Used here only to expand an
 *     abbreviated first name where a second source agreed.
 *   - "NOTABLE PLAYERS" SECTIONS, which are overwhelmingly alumni. Okocha,
 *     Mikel, Ahmed Musa and Yekini are history, not this season's squads, and
 *     putting a famous name in a fantasy pool because it appears on a club's
 *     Wikipedia page is how an app tells its most knowledgeable users it does
 *     not know the league.
 *
 * SO ONLY TWO CLUBS HAVE A SQUAD, and eighteen do not. That is the real state
 * of NPFL data in September 2026, and the app says so on the screen rather
 * than papering over it. The remaining clubs are filled with players that are
 * VISIBLY labelled as placeholders — see [SampleData] — which is the same
 * choice the earlier build made and for the same reason: inventing
 * plausible-sounding Nigerian names would make the demo look better and the
 * product worse.
 *
 * POSITION IS A SOURCED FIELD LIKE ANY OTHER. Thirteen real players are known
 * by name from dated transfer reporting that never says what they play. They
 * are recorded in [registered] with a null position and they do NOT enter the
 * selection pool, because a fantasy pool that guessed would be asking managers
 * to pick a goalkeeper who is a winger. [awaitingPosition] is the operator's
 * queue for exactly that.
 *
 * PRICES ARE NOT HERE. No market value is published for an NPFL player, so a
 * price is a game-balance parameter rather than a fact about anybody. Prices
 * live with the sample data.
 */
object NpflSquads {

    enum class SourceClass {
        /** The club's own website or its own channel. */
        CLUB_OFFICIAL,

        /** A news report carrying a 2026 date that names the player in the body. */
        DATED_REPORT
    }

    enum class Confidence { MEDIUM, LOW }

    data class SquadSource(
        val clubId: String,
        val sourceClass: SourceClass,
        val description: String,
        val url: String,
        /** ISO date the page was read. */
        val retrieved: String,
        val confidence: Confidence,
        /** Why it is not better than it is. Always populated. */
        val caveat: String,
        /** What some other source said instead. */
        val conflicts: String? = null
    )

    data class RegisteredPlayer(
        val clubId: String,
        val firstName: String,
        val lastName: String,
        /** Null when no source states it. Such a player stays out of the pool. */
        val position: Position?,
        /** As published. Null when the source gives none. */
        val squadNumber: Int?,
        /** Why this rendering, or what a source disputed. */
        val note: String? = null
    ) {
        val name: String get() = "$firstName $lastName"
    }

    val sources: List<SquadSource> = listOf(
        SquadSource(
            clubId = "ENY",
            sourceClass = SourceClass.CLUB_OFFICIAL,
            description = "Enyimba International FC official website, first-team page",
            url = "https://enyimbafc.net/team/",
            retrieved = "2026-09-07",
            confidence = Confidence.MEDIUM,
            caveat = "A club is the best authority on its own squad, and this was the " +
                "only current, complete list found anywhere. But the page carries no " +
                "\"as of\" date, so its currency is inferred rather than stated. " +
                "Check against a matchday teamsheet before launch.",
            conflicts = "Goal.com, labelled 2026/27, lists roughly forty names — far " +
                "too many for a first team, and reading like an accumulated roster. " +
                "It disagrees on position (Nwaodu as a midfielder), on spellings " +
                "(Adikwu/Adiukwu, Abdullahi/Abdullah), on who wears 31, and on the " +
                "manager."
        ),
        SquadSource(
            clubId = "IKO",
            sourceClass = SourceClass.CLUB_OFFICIAL,
            description = "Ikorodu City FC official website, players page",
            url = "https://ikoroducityfc.com/",
            retrieved = "2026-09-08",
            confidence = Confidence.LOW,
            caveat = "Club-official, and therefore admissible — but with real evidence " +
                "that this page is stale. Its footer reads \"Copyright (c) 2023\", and " +
                "twenty-nine names is a full registered list rather than a match squad. " +
                "Treat it as a starting point to correct, not a finished roster.",
            conflicts = "It still lists Temitope Folarin at 25 while a report dated 28 " +
                "August 2026 has Folarin Temitope among Rangers' new signings. Name " +
                "order varies in Nigerian usage, so that may or may not be the same " +
                "person. Rivio Ayemwenre appears here and also in transfer records " +
                "against Sporting Lagos. Both point the same way: this roster has not " +
                "been revised for 2026/27."
        )
    )

    /** Clubs with a club-official squad list. Two of twenty. */
    val clubsWithVerifiedSquad: Set<String> = sources.map { it.clubId }.toSet()

    fun sourceFor(clubId: String): SquadSource? = sources.firstOrNull { it.clubId == clubId }

    private fun gk(club: String, first: String, last: String, no: Int?, note: String? = null) =
        RegisteredPlayer(club, first, last, Position.GK, no, note)

    private fun df(club: String, first: String, last: String, no: Int?, note: String? = null) =
        RegisteredPlayer(club, first, last, Position.DEF, no, note)

    private fun md(club: String, first: String, last: String, no: Int?, note: String? = null) =
        RegisteredPlayer(club, first, last, Position.MID, no, note)

    private fun fw(club: String, first: String, last: String, no: Int?, note: String? = null) =
        RegisteredPlayer(club, first, last, Position.FWD, no, note)

    /** Named in a dated report, with no position stated anywhere. */
    private fun unplaced(club: String, first: String, last: String, note: String) =
        RegisteredPlayer(club, first, last, null, null, note)

    /**
     * Enyimba International, from the club's own first-team page.
     *
     * Ekene Awazie appears on that page at 30 and is NOT listed here: dated
     * September 2026 reporting has the former captain at Rivers United, and a
     * club page with no date loses to a report that has one. He is recorded
     * against Rivers instead. The same reasoning moves Hogan Umoh to Kun
     * Khalifat, which is why Enyimba's midfield is thin — that is the squad,
     * not a gap in the transcription.
     */
    private val enyimba = listOf(
        gk("ENY", "Nathaniel", "Asibe", 1,
            "club writes \"Asibe Nathaniel\"; Wikipedia gives Nathaniel Asibe"),
        gk("ENY", "Zari", "Abdullah", 12, "Goal.com spells this Abdullahi; the club spelling is kept"),
        gk("ENY", "Henry", "Ozoemena", 35, "Wikipedia (2023) had Henry Ani at 35"),

        df("ENY", "Odinaka", "Obi", 2),
        df("ENY", "Moses", "Adiukwu", 4, "Goal.com spells this Adikwu"),
        df("ENY", "Pascal", "Eze", 18, "club writes \"P Eze\"; Wikipedia gives Pascal Eze at 18"),
        df("ENY", "Obinna", "Leonard", 19),
        df("ENY", "Divine", "Ukadike", 28, "club writes \"Ukadike Divine\""),
        df("ENY", "Imoh", "Obot", 29),
        df("ENY", "Ifeanyi", "Chukwueme", 31,
            "Goal.com and Wikipedia both have Ihemekwele at 31; unresolved"),
        df("ENY", "David", "Ekele", 39),

        md("ENY", "Bernard", "Ovoke", 11),
        md("ENY", "Chigozie", "Ani", 21),
        md("ENY", "Chinedu", "Ufere", 22, "club writes \"Ufere Chinedu\""),
        md("ENY", "Eze", "Pinto", 27),
        md("ENY", "Siriki", "Siriki", 27, "the club page lists two players at 27; reproduced as published"),

        fw("ENY", "Mujeep", "Adefeso", null),
        fw("ENY", "Alade", "Balogun", 3),
        fw("ENY", "Chukwudi", "Nwaodu", 8, "club writes \"Nwaodu Chukwudi\"; Goal.com has him as a midfielder"),
        fw("ENY", "Abdulwasu", "Jimuo", 9),
        fw("ENY", "Jephtha", "Clinton", 33)
    )

    /** Ikorodu City, from the club's own players page. Stale, and said to be. */
    private val ikoroduCity = listOf(
        gk("IKO", "Aina", "Oluwadamilare", 20),
        gk("IKO", "Chinemerem", "Olughu", 31),
        gk("IKO", "Faith", "James", 32),

        df("IKO", "Godspower", "Anozie", 10,
            "club captain per a September 2026 report, which the club's own players " +
                "page does not list at all — the clearest single sign that the page is stale"),
        df("IKO", "Uzochukwu", "Okekeaniokete", 4),
        df("IKO", "Chibuike", "Ohaegbulam", 5),
        df("IKO", "Somto", "Nnoli", 6),
        df("IKO", "Waliu", "Ojetoye", 13, "club writes \"Waliu Kolapo Ojetoye\""),
        df("IKO", "Olalekan", "Osahon", 16),
        df("IKO", "Malik", "Olatunji", 17),
        df("IKO", "Adewale", "Adebayo", 21),
        df("IKO", "Marvelous", "Freedom", 22),
        df("IKO", "Jasmine", "Thompson", 26),
        df("IKO", "Adio", "Ganiyu", 33),
        df("IKO", "Leonard", "Ngenge", 36),
        df("IKO", "David", "Nnachi", 40),

        md("IKO", "Tosin", "Oyedokun", 3),
        md("IKO", "Shola", "Adelani", 7),
        md("IKO", "Salau", "Yusuf", 18),
        md("IKO", "Moses", "Sodeinde", 23),
        md("IKO", "Temitope", "Folarin", 25,
            "DISPUTED: a report dated 28 August 2026 has Folarin Temitope signing for " +
                "Rangers. Recorded, not resolved — name order varies."),
        md("IKO", "Kelechi", "Onwe", 30),
        md("IKO", "Rivio", "Ayemwenre", 35, "also appears in transfer records against Sporting Lagos"),
        md("IKO", "Malik", "Anofi", 37),
        md("IKO", "Malik", "Afoke", 38),

        fw("IKO", "ThankGod", "Chilaka", 9),
        fw("IKO", "Solomon", "Alade", 11),
        fw("IKO", "Chinonso", "Chineme", 14),
        fw("IKO", "Solomon", "Emmanuel", 19),
        fw("IKO", "Samuel", "Ezekiel", 39)
    )

    /**
     * Players named in dated 2026 transfer reporting.
     *
     * These are not squads. They are the individual moves the press wrote
     * about, which for eighteen of twenty clubs is every current-season player
     * fact that exists. Position is given only where a report gives it.
     */
    private val transfers2026 = listOf(
        // Rivers United — strengthening for a CAF Champions League campaign.
        gk("RIV", "Clinton", "Andy", null, "signed 2026; reported as a goalkeeper"),
        fw("RIV", "Alex", "Oyowah", 10,
            "reported as a forward wearing 10 in the most recent confirmed squad list, " +
                "dated February 2024 — the number may not still be current"),
        fw("RIV", "Ekene", "Awazie", null,
            "former Enyimba captain, signed 2026. Enyimba's own undated page still " +
                "lists him at 30; the dated report wins"),
        unplaced("RIV", "Chikamso", "Amaefula", "signed 2026; no position reported"),

        // Plateau United
        df("PLA", "Ikouwem", "Udoh", null,
            "reported as a left-back, joined on a free transfer from Shooting Stars"),

        // Kun Khalifat
        md("KUN", "Hogan", "Umoh", null, "reported as a midfielder, signed from Enyimba"),

        // Rangers International
        unplaced("RAN", "Azeez", "Falolu", "signed 2026 from Katsina United; no position reported"),
        unplaced("RAN", "Ifeanyi", "Okechukwu",
            "signed to 2028; earlier spells at Niger Tornadoes, Enyimba and Nasarawa " +
                "United. No position reported"),

        // Kano Pillars — eight confirmed signings, not one position among them.
        unplaced("KAN", "Rabiu", "Ali", "club captain; no position in any source found"),
        unplaced("KAN", "Ubong", "Friday", "signed 2026 from Rivers United"),
        unplaced("KAN", "Fabian", "Nworie", "signed 2026 from Remo Stars"),
        unplaced("KAN", "Obassa", "Adeniyi", "signed 2026 from Remo Stars"),
        unplaced("KAN", "Tejiri", "Emonena", "signed 2026 on a two-year deal"),
        unplaced("KAN", "Abdulraheem", "Shola", "signed 2026 on a two-year deal"),
        unplaced("KAN", "Oloye", "Felix", "signed 2026 on a two-year deal"),
        unplaced("KAN", "Abdallah", "Sulaiman", "signed 2026 on a two-year deal"),
        unplaced("KAN", "Zayyad", "Musa", "signed 2026"),

        // Sporting Lagos
        unplaced("SPL", "Ebenezer", "Harcourt",
            "academy product who became Nigeria's youngest ever senior international " +
                "at fifteen in 2025. Whether he is a first-team regular is unclear")
    )

    /** Every real player this app can name, with or without a position. */
    val registered: List<RegisteredPlayer> = enyimba + ikoroduCity + transfers2026

    /** Real players the selection pool can use. */
    fun positioned(): List<RegisteredPlayer> = registered.filter { it.position != null }

    /** Real players held back until somebody sources a position. */
    fun awaitingPosition(): List<RegisteredPlayer> = registered.filter { it.position == null }

    fun forClub(clubId: String): List<RegisteredPlayer> = registered.filter { it.clubId == clubId }

    /**
     * Stable id for a real player: club, then surname, then a discriminator when
     * one club has two of the same surname. Derived rather than hand-assigned so
     * adding a player cannot silently renumber an existing one.
     */
    fun idFor(player: RegisteredPlayer): String {
        val base = "${player.clubId}-${player.lastName.lowercase()}"
        val sameName = registered.filter {
            it.clubId == player.clubId && it.lastName == player.lastName
        }
        if (sameName.size == 1) return base
        return "$base${sameName.indexOf(player) + 1}"
    }
}
