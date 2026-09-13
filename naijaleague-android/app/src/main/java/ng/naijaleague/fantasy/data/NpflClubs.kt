package ng.naijaleague.fantasy.data

import ng.naijaleague.fantasy.rules.Club

/**
 * The twenty clubs of the 2026/27 Nigeria Premier Football League, with the
 * facts each one is actually sourced for.
 *
 * WHERE THIS COMES FROM. Three bodies of research, merged here:
 *
 *   1. The Transfermarkt league table (current-value column, 15/08/2026
 *      snapshot) — market value and registered squad size. Transcribed by
 *      hand, so NpflClubsTest checks the transcription against the totals the
 *      source publishes.
 *   2. `research/clubs.json` in the earlier NPFL Fantasy repository — city,
 *      state, stadium, capacity, founding year, titles and coach, each field
 *      carrying its own verification flag, compiled 4 September 2026.
 *   3. `packages/core/src/clubIdentity.js` in the same repository — kit
 *      colours read off RSSSF's Nigeria colours page and club Wikipedia
 *      articles, each with its own source and confidence.
 *
 * WHY THE LEAGUE IS CALLED WHAT IT IS CALLED. Officially the Nigeria *Premier*
 * Football League since the NFF's 79th Annual General Assembly (Uyo, September
 * 2023) renamed it from Nigeria Professional Football League. The abbreviation
 * NPFL survived the rename, which is why almost everyone still writes it and
 * why the app does too.
 *
 * TWO CLUBS DO NOT PLAY AT HOME THIS SEASON, and the engine has to know:
 * Rangers International are at the MKO Abiola Sports Arena in Abeokuta — some
 * 600km from Enugu — while Nnamdi Azikiwe Stadium is renovated, and Doma
 * United play their Nasarawa "home" fixtures at Pantami Stadium in Gombe.
 * A fixture list calls those home games. A supporter in the ground does not.
 * See [ClubRecord.playsHomeAtNeutralGround] and the Ground Man chip.
 *
 * CONFIDENCE IS CARRIED, NOT FLATTENED. Club identity is certain for all
 * twenty. Stadium, capacity and founding year are well sourced for the sixteen
 * continuing clubs and thinner for the four promoted. Coaches turn over fast
 * enough in this league that a Wikipedia infobox is evidence of last season.
 * Kit colours are the weakest of the lot — see [KitColours].
 */
object NpflClubs {

    /** How good the evidence behind one field is. */
    enum class Confidence { HIGH, MEDIUM, LOW, NONE }

    /** Whether a club was in last season's top flight or came up into it. */
    enum class Standing { CONTINUING, PROMOTED }

    /**
     * A club's kit colours, or an honest admission that nobody found them.
     *
     * THE SOURCING PROBLEM. There is no current, authoritative kit register for
     * the NPFL. The best colour-specific reference is RSSSF's Nigeria page — a
     * real reference work that carries the note "Last updated: 29 Jan 2010".
     * Sixteen years stale, from an era when several of these clubs had
     * different sponsors and two were not in this division. Wikipedia's club
     * articles are current but mention kits in passing prose rather than
     * stating colours as data.
     *
     * SO THE TWO SOURCES WERE COMPARED RATHER THAN RANKED, and the rule is:
     *
     *   - Both name the same pair of colours → render it. Two independent
     *     readings agreeing is the strongest evidence available here.
     *   - Both name the same pair but disagree on which is primary → render
     *     the more recent reading and record the swap. Nobody is wrong about
     *     a club's colours, only about which one the shirt is mostly made of.
     *   - They name DIFFERENT colours → [conflicted]. Do not pick a side and
     *     do not render it. RSSSF has Rangers in red and Enyimba in blue;
     *     the Wikipedia articles make both green. One of those readings is
     *     wrong and this file is not the place to decide which.
     *   - Only one source has anything → render it at its own confidence.
     *   - Neither has anything → nulls, and the UI falls back to its generated
     *     brand mark, which is honestly neutral rather than confidently wrong.
     *
     * Three clubs sit in that last group — Barau, Doma United and Inter Lagos,
     * all recently promoted, none with a findable kit reference. They are the
     * argument for an operator console: somebody who has simply SEEN these
     * teams play can supply in ten seconds what a week of searching did not.
     *
     * HEX IS A RENDERING CHOICE, NOT A CLAIM. The sources say "green" and
     * "yellow", never "#00843D". The values below are ordinary readings of
     * those words, picked to hold contrast against all three brand surfaces.
     */
    data class KitColours(
        val primary: Long?,
        val secondary: Long?,
        /** What the sources actually said, in words. */
        val words: String?,
        val confidence: Confidence,
        /** Set when the two sources name different colours. Never rendered. */
        val conflicted: Boolean = false,
        val note: String? = null
    ) {
        val renderable: Boolean get() = primary != null && !conflicted
    }

    data class ClubRecord(
        val club: Club,
        val state: String,
        /** Where home fixtures are actually played in 2026/27. */
        val stadium: String,
        /** The ground of record, when it is not the one being played in. */
        val homeGroundOfRecord: String? = null,
        val capacity: Int,
        /** Null for Warri Wolves: genuinely not found, not omitted. */
        val founded: Int?,
        val leagueTitles: Int,
        val titlesNote: String? = null,
        val nickname: String? = null,
        val coach: String? = null,
        val coachConfidence: Confidence = Confidence.NONE,
        val kit: KitColours,
        val standing: Standing,
        /**
         * Fixture-difficulty seed, 1.0 to 5.0, from where the club finished in
         * 2025/26. Meant to be retuned from live results, never treated as a
         * fact about the club.
         */
        val strength: Double,
        /** Transfermarkt current value in euros — league reference, not a price. */
        val marketValueEur: Long,
        /** Registered squad size per Transfermarkt, not the fantasy-eligible count. */
        val squadSize: Int,
        val note: String? = null
    ) {
        val id: String get() = club.id
        val playsHomeAtNeutralGround: Boolean get() = homeGroundOfRecord != null
    }

    private val GREEN = 0xFF00843DL
    private val WHITE = 0xFFFFFFFFL
    private val RED = 0xFFC8102EL
    private val YELLOW = 0xFFFFD100L
    private val BLUE = 0xFF003DA5L
    private val SKY = 0xFF4FA8E0L
    private val ORANGE = 0xFFE4572EL

    /** Both sources name the same pair. The strongest case available. */
    private fun agreed(primary: Long, secondary: Long?, words: String) =
        KitColours(primary, secondary, words, Confidence.MEDIUM)

    /** Same pair, sources disagree on which is primary. Recency wins; say so. */
    private fun ordered(primary: Long, secondary: Long, words: String, note: String) =
        KitColours(primary, secondary, words, Confidence.LOW, note = note)

    /** One source only. */
    private fun single(primary: Long, secondary: Long?, words: String, note: String) =
        KitColours(primary, secondary, words, Confidence.LOW, note = note)

    /** The sources name different colours. Not rendered. */
    private fun contested(words: String, note: String) =
        KitColours(null, null, words, Confidence.NONE, conflicted = true, note = note)

    private val UNSOURCED = KitColours(
        null, null, null, Confidence.NONE,
        note = "No kit source found. The generated mark is used instead of a guess."
    )

    /**
     * Ordered by Transfermarkt market value, as that source orders it.
     *
     * Keeping the order of a published table makes the transcription checkable
     * by eye against the source as well as by sum in the test.
     */
    val all: List<ClubRecord> = listOf(
        ClubRecord(
            club = Club("RAN", "Rangers International FC", "Rangers", "Enugu", playsHomeAtNeutralGround = true),
            state = "Enugu State",
            stadium = "MKO Abiola Sports Arena, Abeokuta",
            homeGroundOfRecord = "Nnamdi Azikiwe Stadium, Enugu",
            capacity = 22_000,
            founded = 1970,
            leagueTitles = 9,
            titlesNote = "1974, 1975, 1977, 1981, 1982, 1984, 2016, 2024, 2025/26 — " +
                "defending champions, on 68 points",
            nickname = "Flying Antelopes",
            coach = "Fidelis Ilechukwu",
            coachConfidence = Confidence.HIGH,
            kit = contested(
                "RSSSF: red with white sleeves. Wikipedia: green and white.",
                "The least settled entry in the table and the first an operator should fix."
            ),
            standing = Standing.CONTINUING,
            strength = 5.0,
            marketValueEur = 2_560_000,
            squadSize = 42,
            note = "Playing every 2026/27 home fixture roughly 600km from Enugu while " +
                "Nnamdi Azikiwe Stadium is renovated, so their home advantage is " +
                "materially weaker than the fixture list implies. Competing on three " +
                "fronts: NPFL, President Federation Cup and CAF Champions League."
        ),
        ClubRecord(
            club = Club("RIV", "Rivers United FC", "Rivers Utd", "Port Harcourt"),
            state = "Rivers State",
            stadium = "Adokiye Amiesimaka Stadium",
            capacity = 38_000,
            founded = 2016,
            leagueTitles = 1,
            titlesNote = "2021/22, with a then-record 77 points",
            nickname = "The Pride of Rivers",
            coach = "Finidi George",
            coachConfidence = Confidence.HIGH,
            kit = single(WHITE, BLUE, "White home shirt", "Wikipedia prose; no secondary confirmed."),
            standing = Standing.CONTINUING,
            strength = 4.5,
            marketValueEur = 2_460_000,
            squadSize = 35,
            note = "Runners-up in 2025/26 on 67 points, same win total as Rangers."
        ),
        ClubRecord(
            club = Club("BEN", "Bendel Insurance FC", "Insurance", "Benin City"),
            state = "Edo State",
            stadium = "Samuel Ogbemudia Stadium",
            capacity = 12_000,
            founded = 1972,
            leagueTitles = 2,
            titlesNote = "1973, 1979 (Nigeria Premier League). Federation Cup 2023.",
            nickname = "Benin Arsenal",
            coach = "Greg Ikhenoba",
            coachConfidence = Confidence.LOW,
            kit = agreed(GREEN, WHITE, "Green and white"),
            standing = Standing.CONTINUING,
            strength = 4.0,
            marketValueEur = 2_200_000,
            squadSize = 43
        ),
        ClubRecord(
            club = Club("BAR", "Barau FC", "Barau", "Kano"),
            state = "Kano State",
            stadium = "Sani Abacha Stadium, Kano",
            capacity = 16_000,
            founded = 2024,
            leagueTitles = 0,
            nickname = null,
            coach = "Eugene Agagbe",
            coachConfidence = Confidence.LOW,
            kit = UNSOURCED,
            standing = Standing.CONTINUING,
            strength = 2.5,
            marketValueEur = 1_740_000,
            squadSize = 48,
            note = "Founded 2024 by Senator Barau I. Jibrin. Traditional home is " +
                "Dambatta; Sani Abacha Stadium in Kano has been the match venue since " +
                "2025/26. Promoted for 2025/26 and still here — a club easily and " +
                "wrongly remembered as having gone down with Wikki Tourists."
        ),
        ClubRecord(
            club = Club("PLA", "Plateau United FC", "Plateau Utd", "Jos"),
            state = "Plateau State",
            stadium = "New Jos Stadium",
            capacity = 60_000,
            founded = 1975,
            leagueTitles = 1,
            titlesNote = "2017. Nigerian FA Cup 1999. Founded as JIB Strikers, " +
                "renamed Plateau United in 1991.",
            nickname = "Peace Boys",
            coach = "Gbenga Ogunbote",
            coachConfidence = Confidence.MEDIUM,
            kit = agreed(GREEN, YELLOW, "Green and yellow"),
            standing = Standing.CONTINUING,
            strength = 4.5,
            marketValueEur = 1_720_000,
            squadSize = 56,
            note = "Much the largest ground in the league. One report had a coaching " +
                "change under discussion; Ogunbote took the season opener."
        ),
        ClubRecord(
            club = Club("3SC", "Shooting Stars SC", "3SC", "Ibadan"),
            state = "Oyo State",
            stadium = "Lekan Salami Stadium",
            capacity = 10_000,
            founded = 1950,
            leagueTitles = 5,
            titlesNote = "1976, 1980, 1983, 1995, 1998. African Cup Winners' Cup 1976 — " +
                "the first international trophy won by a Nigerian club.",
            nickname = "Oluyole Warriors",
            coach = "Salisu Yusuf",
            coachConfidence = Confidence.HIGH,
            kit = ordered(
                WHITE, BLUE, "White with stripes at home, blue away",
                "RSSSF leads on blue; the Wikipedia article leads on white."
            ),
            standing = Standing.CONTINUING,
            strength = 3.5,
            marketValueEur = 1_700_000,
            squadSize = 41,
            note = "Third in 2025/26 on 60 points, ending a 27-year continental absence. " +
                "Their published contact domain now serves an online casino — a club " +
                "domain that was once official is not permanently trustworthy."
        ),
        ClubRecord(
            club = Club("KAN", "Kano Pillars FC", "Pillars", "Kano"),
            state = "Kano State",
            stadium = "Sani Abacha Stadium",
            capacity = 16_000,
            founded = 1990,
            leagueTitles = 4,
            titlesNote = "2007/08, 2012, 2013, 2014",
            nickname = "Sai Masu Gida",
            coach = "Daniel Ogunmodede",
            coachConfidence = Confidence.LOW,
            kit = contested(
                "RSSSF: yellow with green sleeves. Wikipedia: green with white sides.",
                "The club's best-looking website says in its own footer that it is an " +
                    "unaffiliated fan site, so it settles nothing."
            ),
            standing = Standing.CONTINUING,
            strength = 4.0,
            marketValueEur = 1_700_000,
            squadSize = 45,
            note = "Rebuilt heavily for 2026/27 — eight signings confirmed by name."
        ),
        ClubRecord(
            club = Club("ENY", "Enyimba International FC", "Enyimba", "Aba"),
            state = "Abia State",
            stadium = "Enyimba International Stadium",
            capacity = 16_000,
            founded = 1976,
            leagueTitles = 9,
            titlesNote = "2001, 2002, 2003, 2005, 2007, 2009/10, 2015, 2019, 2023. " +
                "First Nigerian club to win the CAF Champions League, back to back in " +
                "2003 and 2004.",
            nickname = "The People's Elephant",
            coach = null,
            coachConfidence = Confidence.NONE,
            kit = contested(
                "RSSSF: blue with white edges. Wikipedia: green and white.",
                "A supporter in Aba will tell you which is right. Two secondary " +
                    "sources disagreeing is not the place to settle it."
            ),
            standing = Standing.CONTINUING,
            strength = 5.0,
            marketValueEur = 1_490_000,
            squadSize = 40,
            note = "One automated pass produced a coach's name that could not be " +
                "corroborated. Left blank rather than risk naming the wrong person."
        ),
        ClubRecord(
            club = Club("IKO", "Ikorodu City FC", "Ikorodu", "Ikorodu"),
            state = "Lagos State",
            stadium = "Onikan Stadium (Mobolaji Johnson Arena)",
            capacity = 10_000,
            founded = 2022,
            leagueTitles = 0,
            nickname = "Oga Boys",
            coach = "Ali Kandil",
            coachConfidence = Confidence.LOW,
            kit = agreed(WHITE, ORANGE, "White home shirt with red and orange trim"),
            standing = Standing.CONTINUING,
            strength = 3.0,
            marketValueEur = 1_280_000,
            squadSize = 46,
            note = "Founded January 2022, promoted 2024, fourth in 2025/26."
        ),
        ClubRecord(
            club = Club("ABW", "Abia Warriors FC", "Abia", "Umuahia"),
            state = "Abia State",
            stadium = "Umuahia Township Stadium",
            capacity = 5_000,
            founded = 1993,
            leagueTitles = 0,
            nickname = "The Ochendo Warriors",
            coach = "Imama Amapakabo",
            coachConfidence = Confidence.LOW,
            kit = agreed(RED, WHITE, "Red and white"),
            standing = Standing.CONTINUING,
            strength = 3.0,
            marketValueEur = 1_230_000,
            squadSize = 40,
            note = "Started as NEPA FC, then Ejoor Babes, then Orji Uzor Kalu FC."
        ),
        ClubRecord(
            club = Club("KAT", "Katsina United FC", "Katsina Utd", "Katsina"),
            state = "Katsina State",
            stadium = "Muhammadu Dikko Stadium",
            capacity = 35_000,
            founded = 1994,
            leagueTitles = 0,
            titlesNote = "No top-flight title. FA Cup runners-up three years running, " +
                "1995 to 1997, and never won it.",
            nickname = "The Chanji Boys",
            coach = "Henry Makinwa",
            coachConfidence = Confidence.LOW,
            kit = agreed(GREEN, WHITE, "Green and white"),
            standing = Standing.CONTINUING,
            strength = 3.0,
            marketValueEur = 1_200_000,
            squadSize = 43,
            note = "Disbanded after relegation in 2001, resurrected as Spotlights FC " +
                "in 2009, back to Katsina United in 2016."
        ),
        ClubRecord(
            club = Club("WAR", "Warri Wolves FC", "Wolves", "Warri"),
            state = "Delta State",
            stadium = "Warri Township Stadium",
            capacity = 20_000,
            founded = null,
            leagueTitles = 0,
            titlesNote = "Division 1B champions 2009 — a promotion, not a top-flight title.",
            nickname = "The Seasiders",
            coach = "Napoleon Aluma",
            coachConfidence = Confidence.LOW,
            kit = single(BLUE, YELLOW, "Blue with a yellow right sleeve", "RSSSF only."),
            standing = Standing.CONTINUING,
            strength = 2.5,
            marketValueEur = 1_070_000,
            squadSize = 51,
            note = "Formerly Nigeria Port Authority FC, renamed in 2007 on returning to " +
                "Warri from Lagos. The founding year of the original works team was " +
                "not found — a genuine gap, not an omission."
        ),
        ClubRecord(
            club = Club("NAS", "Nasarawa United FC", "Nasarawa Utd", "Lafia"),
            state = "Nasarawa State",
            stadium = "Lafia Township Stadium",
            capacity = 10_000,
            founded = 2003,
            leagueTitles = 0,
            nickname = "Solid Miners",
            coach = null,
            coachConfidence = Confidence.NONE,
            kit = agreed(SKY, WHITE, "Light blue and white"),
            standing = Standing.CONTINUING,
            strength = 3.0,
            marketValueEur = 1_020_000,
            squadSize = 45,
            note = "Sixth in 2025/26. Salisu Yusuf was linked here in March 2026 and " +
                "went to Shooting Stars instead, so the 2026/27 coach is unknown."
        ),
        ClubRecord(
            club = Club("NIT", "Niger Tornadoes FC", "Tornadoes", "Minna"),
            state = "Niger State",
            stadium = "Bako Kontagora Stadium",
            capacity = 5_000,
            founded = 1977,
            leagueTitles = 0,
            titlesNote = "Nigerian FA Cup 2000; runners-up 2017 on penalties.",
            nickname = "Ikon Allah",
            coach = "Daniel Aquino",
            coachConfidence = Confidence.LOW,
            kit = ordered(
                YELLOW, WHITE, "Yellow with horizontal stripes",
                "RSSSF reads white with a yellow hoop; Wikipedia leads on yellow."
            ),
            standing = Standing.CONTINUING,
            strength = 2.5,
            marketValueEur = 1_010_000,
            squadSize = 44
        ),
        ClubRecord(
            club = Club("KUN", "Kun Khalifat FC", "Kun Khalifat", "Owerri"),
            state = "Imo State",
            stadium = "Dan Anyiam Stadium",
            capacity = 12_000,
            founded = 2020,
            leagueTitles = 0,
            nickname = null,
            coach = null,
            coachConfidence = Confidence.NONE,
            kit = agreed(RED, WHITE, "Red home, white away, black and gold third"),
            standing = Standing.CONTINUING,
            strength = 2.5,
            marketValueEur = 855_000,
            squadSize = 52,
            note = "Capacity is reported as 10,000 in some places and 12,000 in others; " +
                "the discrepancy is unresolved. The club unveiled a new technical crew " +
                "for 2026/27 without the head coach being named in any source found."
        ),
        ClubRecord(
            club = Club("KWA", "Kwara United FC", "Kwara Utd", "Ilorin"),
            state = "Kwara State",
            stadium = "Kwara State Stadium",
            capacity = 18_000,
            founded = 1997,
            leagueTitles = 0,
            titlesNote = "Federation Cup 2025. Roots to 1974 as Kwara State Water " +
                "Corporation FC.",
            nickname = "Harmony Boys",
            coach = "Tunde Sanni",
            coachConfidence = Confidence.LOW,
            kit = ordered(
                GREEN, WHITE, "Green and white",
                "RSSSF reads white with green edges; Wikipedia leads on green."
            ),
            standing = Standing.CONTINUING,
            strength = 3.5,
            marketValueEur = 855_000,
            squadSize = 42
        ),
        ClubRecord(
            club = Club("SPL", "Sporting Lagos FC", "Sporting", "Lagos"),
            state = "Lagos State",
            stadium = "Onikan Stadium (Mobolaji Johnson Arena)",
            capacity = 10_000,
            founded = 2022,
            leagueTitles = 0,
            titlesNote = "Nigeria National League champions 2025/26, promoted on goal " +
                "difference through the Super Four.",
            nickname = null,
            coach = "Jeffrey Buter",
            coachConfidence = Confidence.LOW,
            kit = agreed(0xFF0057B8L, YELLOW, "Blue with yellow"),
            standing = Standing.PROMOTED,
            strength = 2.5,
            marketValueEur = 735_000,
            squadSize = 37,
            note = "Founded by Shola Akinlade, co-founder of Paystack. Their academy " +
                "produced Ebenezer Harcourt, who at fifteen became Nigeria's youngest " +
                "ever senior international in 2025."
        ),
        ClubRecord(
            club = Club("RAB", "Ranchers Bees FC", "Ranchers", "Kaduna"),
            state = "Kaduna State",
            stadium = "Ranchers Bees Stadium (Kaduna Township Stadium)",
            capacity = 10_000,
            founded = 1974,
            leagueTitles = 0,
            nickname = null,
            coach = null,
            coachConfidence = Confidence.NONE,
            kit = single(
                WHITE, BLUE, "White with blue edges",
                "RSSSF only. Do NOT infer yellow and black from the Bees nickname — " +
                    "no source supports it."
            ),
            standing = Standing.PROMOTED,
            strength = 2.0,
            marketValueEur = 605_000,
            squadSize = 28,
            note = "Back in the top flight for the first time since 2009/10, a " +
                "sixteen-year wait, on 20 points from 11 NNL matches. Roots as Kaduna " +
                "Bees FC in 1964, merged into DIC Bees United in 1974. Transfermarkt " +
                "spells the name Rancher's Bees; the club does not use the apostrophe."
        ),
        ClubRecord(
            club = Club("DOM", "Doma United FC", "Doma", "Doma", playsHomeAtNeutralGround = true),
            state = "Nasarawa State",
            stadium = "Pantami Stadium, Gombe",
            homeGroundOfRecord = "Doma, Nasarawa State",
            capacity = 12_000,
            founded = 1994,
            leagueTitles = 0,
            nickname = null,
            coach = "Deji Ayeni",
            coachConfidence = Confidence.HIGH,
            kit = UNSOURCED,
            standing = Standing.PROMOTED,
            strength = 2.0,
            marketValueEur = 225_000,
            squadSize = 26,
            note = "Doma is in Nasarawa State — one automated pass put the club in " +
                "Gombe, which is where the matches are played and not where the club " +
                "is from. Ayeni signed a two-year deal as Technical Adviser and says " +
                "the club is not under title pressure."
        ),
        ClubRecord(
            club = Club("INT", "Inter Lagos FC", "Inter", "Lagos"),
            state = "Lagos State",
            stadium = "Onikan Stadium (Mobolaji Johnson Arena)",
            capacity = 10_000,
            founded = 2023,
            leagueTitles = 0,
            nickname = null,
            coach = null,
            coachConfidence = Confidence.NONE,
            kit = UNSOURCED,
            standing = Standing.PROMOTED,
            strength = 2.0,
            marketValueEur = 25_000,
            squadSize = 25,
            note = "Founded 2023 by Lanre Vigo, Olumide Fayankin and Gatumi Aliyu. " +
                "First ever top-flight promotion, sealed with a 1-1 draw against " +
                "Smart City to top NNL Conference A. Lost the season opener 2-1 at " +
                "Shooting Stars."
        )
    )

    /** Totals Transfermarkt publishes, kept here so the transcription is testable. */
    const val PUBLISHED_SQUAD_TOTAL = 829
    const val PUBLISHED_VALUE_TOTAL_EUR = 25_650_000L

    /** The four clubs relegated at the end of 2025/26, kept so the absence is deliberate. */
    val relegatedAfter2025_26 = listOf(
        "Remo Stars", "El-Kanemi Warriors", "Bayelsa United", "Wikki Tourists"
    )

    val clubs: List<Club> = all.map { it.club }

    private val byId = all.associateBy { it.id }

    fun record(id: String): ClubRecord =
        byId[id] ?: error("no such club: $id")

    fun club(id: String): Club = record(id).club

    fun name(id: String): String = record(id).club.name

    /** Clubs whose home fixtures are not played at home this season. */
    fun displaced(): List<ClubRecord> = all.filter { it.playsHomeAtNeutralGround }

    /** Clubs with no renderable kit colour, for an operator's attention list. */
    fun awaitingColours(): List<ClubRecord> = all.filterNot { it.kit.renderable }
}
