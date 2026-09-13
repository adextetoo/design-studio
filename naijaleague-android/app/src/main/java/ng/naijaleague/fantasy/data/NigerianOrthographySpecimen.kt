package ng.naijaleague.fantasy.data

/**
 * A type specimen for the three orthographies §02 says the app must set, kept
 * deliberately separate from the player pool.
 *
 * WHY IT IS NOT IN THE PLAYER POOL ANY MORE. It used to be: the sample squad
 * was filled with invented names carrying Yoruba under-dots, Igbo dotted
 * vowels and Hausa hooked letters, so that a font failing to set them would
 * fail visibly on the first screen. Good instinct, wrong place. Those names
 * read as real players to exactly the users who know the NPFL best, and the
 * typography argument does not need a fake squad to make it — it needs strings.
 * These are the strings. The pool holds real people or labelled stand-ins.
 *
 * WHAT EACH LINE IS FOR. The hard case is not a dotted vowel, which most faces
 * have, and not a tone mark, which many do. It is the two STACKED: a dotted
 * vowel carrying a separate combining tone mark above it. Most display faces —
 * Druk among them — have no glyph for the pair and no mark-positioning rules
 * to compose one, so they either drop the mark or print it through the letter.
 * That is why §02 says a display face that cannot set a player's name is not a
 * candidate, and why anything rendering a name goes through
 * BrandType.NigerianText.
 */
object NigerianOrthographySpecimen {

    /**
     * Yoruba: under-dots, and tone marks stacked on top of them.
     *
     * Words and places rather than person-shaped names, so nothing here can be
     * mistaken for squad data if it turns up in a screenshot.
     */
    const val YORUBA = "Ọ̀ṣọ́ọ̀sì · Àgbà Ògbóni · Èkó Akété · Ẹ̀kọ àti ẹ̀wà · Ìbàdàn"

    /** Igbo: dotted vowels, the syllabic nasal, and tone marks over both. */
    const val IGBO = "Ị̀gbò · Ụ̀mụ̀ńna · Ọ̀gbọ́nna · Ṅnọ̀ọ̀ · Ǹdị̀ Igbo"

    /**
     * Hausa: hooked consonants.
     *
     * These are LETTERS, not decorated Latin, and each is a single code point —
     * so unlike the two lines above they do not test mark stacking at all. They
     * test something else: whether the face has the glyph. A font missing ƙ
     * draws a box, which is a louder failure than a dropped tone mark and an
     * easier one to miss in review, because nobody reviewing in English reads
     * these lines closely.
     */
    const val HAUSA = "Ƙanƙara · Ɗan Ƙasa · Ƴanci · Ɓarawo · Sarkin Ƙano"

    val all: List<String> = listOf(YORUBA, IGBO, HAUSA)

    /** Every mark class §02 enumerates, lowercased for containment checks. */
    val requiredMarks: List<String> = listOf(
        "ọ", "ẹ", "ṣ", "ị", "ụ", "ń", "ƙ", "ɗ", "ƴ", "ɓ", "ṅ", "à", "ó", "é"
    )

    /** The stacked pairs: a dotted vowel plus a combining tone mark. */
    val stackedPairs: List<String> = listOf("Ọ̀", "ọ̀", "ọ́", "ẹ̀", "ụ̀", "ị̀")

    /**
     * The lines that stack a combining mark over a dotted vowel — the case most
     * display faces fail. Hausa is deliberately not among them: its hooked
     * letters are precomposed single code points, so it tests glyph coverage
     * rather than mark positioning.
     */
    val stackingLines: List<String> = listOf(YORUBA, IGBO)

    /** Hausa's hooked letters, each of which must be one code point. */
    val hausaHooks: List<Char> = listOf('Ƙ', 'ƙ', 'Ɗ', 'ɗ', 'Ƴ', 'ƴ', 'Ɓ', 'ɓ')
}
