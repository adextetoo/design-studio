package ng.naijaleague.fantasy.rules

import ng.naijaleague.fantasy.data.SampleData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The example squad the app opens on must itself be legal. A demo squad that
 * breaks the club cap or the budget would ship a screen that contradicts the
 * rules page, which is the sort of thing nobody notices until a user does.
 */
class SampleDataTest {

    @Test
    fun `the example squad obeys every rule it displays`() {
        val squad = SampleData.squad
        assertEquals(emptyList(), squad.validate(), "example squad is illegal: ${squad.validate()}")
    }

    @Test
    fun `the example squad spends 98 of the 100 million`() {
        assertEquals(98_000_000L, SampleData.squad.spend)
        assertEquals(2_000_000L, SampleData.squad.budgetRemaining)
    }

    @Test
    fun `the example squad is a 4-3-3 across ten clubs`() {
        assertEquals("4-3-3", SampleData.squad.formation)
        assertEquals(10, SampleData.squad.allPlayers.map { it.clubId }.distinct().size)
        SampleData.squad.allPlayers.groupBy { it.clubId }.forEach { (club, players) ->
            assertTrue(players.size <= 2, "$club has ${players.size}")
        }
    }

    @Test
    fun `every club id in the pool resolves to a real club`() {
        val ids = SampleData.clubs.map { it.id }.toSet()
        SampleData.pool.forEach { assertTrue(it.clubId in ids, "${it.name} -> ${it.clubId}") }
    }

    @Test
    fun `player names carry the diacritics the type system has to support`() {
        // Yoruba under-dots and tone marks, Igbo dotted vowels, Hausa hooks (§02).
        val marks = listOf("ọ", "ẹ", "ṣ", "ị", "ụ", "ń", "ƙ", "ɗ", "ƴ", "ɓ", "ṅ", "à", "ó", "é")
        val pool = SampleData.pool.joinToString(" ") { it.name }.lowercase()
        val missing = marks.filterNot { pool.contains(it) }
        assertTrue(
            missing.isEmpty(),
            "the sample set must exercise every mark class in section 02; missing: $missing"
        )
    }

    @Test
    fun `the sample set includes a stacked combining mark, which is the hard case`() {
        // The reason §02 says a display face that cannot set a player's name is
        // not a candidate. "Ọ̀gbọ́nna" is not precomposed: it is a dotted vowel
        // with a separate combining tone mark stacked on top, and most display
        // faces — Druk included — have no glyph for that pair. Anything that
        // renders names must go through BrandType.NigerianText.
        val stacked = SampleData.pool.filter { player ->
            player.name.any { ch -> Character.getType(ch) == Character.NON_SPACING_MARK.toInt() }
        }
        assertTrue(
            stacked.isNotEmpty(),
            "no name in the sample set uses a combining mark, so the stacking case goes untested"
        )
        println("names with stacked combining marks: " + stacked.joinToString { it.name })
    }

    @Test
    fun `the gameweek scores and the away bonus is doing real work`() {
        val squad = SampleData.squad
        val gw = Scoring.scoreSquad(squad, SampleData.gameweek12, SampleData.activeChip)
        assertEquals(11, gw.playerScores.size, "only the XI scores without Owambe")
        println("GW${SampleData.gameweekNumber} total = ${gw.points}")

        // The 1.8%-owned away forward must out-score the 61.4%-owned captain.
        val differential = gw.playerScores.first { it.playerId == "fw3" }
        val captain = gw.playerScores.first { it.playerId == "fw1" }
        println("fw3 (1.8% owned, 2 away goals) = ${differential.finalPoints}")
        println("fw1 (61.4% owned captain, blank) = ${captain.finalPoints}")
        assertTrue(
            differential.finalPoints > captain.finalPoints,
            "the whole argument of the product: being right and alone beats being safe"
        )
        assertEquals(DifferentialTier.UNDER_2, differential.differentialTier)

        // Away returns must be visibly itemised, never folded into the base.
        val awayLines = gw.playerScores.flatMap { it.lines }.filter { it.label.startsWith("Away Day Bonus") }
        println("away bonus lines = ${awayLines.size}, points = ${awayLines.sumOf { it.points }}")
        assertTrue(awayLines.isNotEmpty())
    }

    @Test
    fun `points stay provisional while any scorer report is open to challenge`() {
        val gw = Scoring.scoreSquad(SampleData.squad, SampleData.gameweek12, null)
        assertTrue(
            gw.isProvisional(SampleData.gameweek12),
            "fw3's report is still open, so the total must read as provisional"
        )
    }

    @Test
    fun `the mini league table is consistent`() {
        SampleData.miniLeague.forEachIndexed { i, e -> assertEquals(i + 1, e.rank) }
        val totals = SampleData.miniLeague.map { it.totalPoints }
        assertEquals(totals.sortedDescending(), totals, "ranks must follow total points")
    }
}
