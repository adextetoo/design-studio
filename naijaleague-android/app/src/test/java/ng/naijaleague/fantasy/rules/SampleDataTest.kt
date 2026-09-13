package ng.naijaleague.fantasy.rules

import ng.naijaleague.fantasy.data.NigerianOrthographySpecimen
import ng.naijaleague.fantasy.data.NpflClubs
import ng.naijaleague.fantasy.data.NpflSquads
import ng.naijaleague.fantasy.data.SampleData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    fun `the example squad spends 88 point 5 of the 100 million`() {
        assertEquals(88_500_000L, SampleData.squad.spend)
        assertEquals(11_500_000L, SampleData.squad.budgetRemaining)
    }

    @Test
    fun `the example squad is a 4-3-3 across twelve clubs`() {
        assertEquals("4-3-3", SampleData.squad.formation)
        assertEquals(12, SampleData.squad.allPlayers.map { it.clubId }.distinct().size)
        SampleData.squad.allPlayers.groupBy { it.clubId }.forEach { (club, players) ->
            assertTrue(players.size <= 2, "$club has ${players.size}")
        }
    }

    @Test
    fun `eight of the fifteen are real players and the rest are labelled as not`() {
        // Five clubs have a player whose club AND position are both sourced, and
        // the club cap is two, so eight is what the evidence actually supports.
        // If this number rises, somebody has sourced a squad — good. If it rises
        // without a source landing in NpflSquads, somebody has invented a player.
        val real = SampleData.squad.allPlayers.filterNot { it.isPlaceholder }
        assertEquals(8, real.size, "real players in the example squad: ${real.map { it.name }}")
        val named = NpflSquads.registered.map { it.name }.toSet()
        real.forEach {
            assertTrue(it.name in named, "${it.name} is marked real but is in no sourced squad")
        }
    }

    @Test
    fun `every placeholder says on its face that it is one`() {
        // The whole argument for shipping stand-ins instead of invented names is
        // that a user can tell. A stand-in that reads like a person defeats it.
        val stand = SampleData.pool.filter { it.isPlaceholder }
        assertTrue(stand.isNotEmpty())
        stand.forEach {
            assertTrue(
                it.name.contains(it.position.short),
                "placeholder '${it.name}' does not announce itself as a stand-in"
            )
            assertTrue(it.sourceNote != null, "placeholder ${it.id} carries no explanation")
            assertEquals(null, it.squadNumber, "a stand-in cannot have a shirt number")
        }
    }

    @Test
    fun `no real player in the pool lacks a sourced position`() {
        val unplaced = NpflSquads.awaitingPosition().map { it.name }.toSet()
        val leaked = SampleData.pool.filter { it.name in unplaced }
        assertTrue(
            leaked.isEmpty(),
            "players with no sourced position must stay out of the pool: ${leaked.map { it.name }}"
        )
        // And they must not be silently dropped either — they are operator work.
        assertTrue(SampleData.awaitingPosition.isNotEmpty())
    }

    @Test
    fun `the club table matches the totals the source publishes`() {
        // The league table is transcribed by hand from Transfermarkt, so the
        // cheapest guard against a slipped digit is the source's own totals.
        assertEquals(20, SampleData.table.size)
        assertEquals(
            SampleData.PUBLISHED_SQUAD_TOTAL,
            SampleData.table.sumOf { it.squadSize }
        )
        assertEquals(
            20,
            SampleData.table.map { it.club.id }.distinct().size,
            "club ids must be unique — the UI keys badges and fixtures off them"
        )
        val value = SampleData.table.sumOf { it.marketValueEur }
        val drift = kotlin.math.abs(value - SampleData.PUBLISHED_VALUE_TOTAL_EUR)
        assertTrue(
            drift <= 100_000L,
            "market values sum to $value against a published $" +
                "${SampleData.PUBLISHED_VALUE_TOTAL_EUR}; the source rounds each " +
                "club for display, but $drift is more than that explains"
        )
    }

    @Test
    fun `Rangers and Ranchers Bees do not share an id`() {
        // The earlier build abbreviated Ranchers Bees to RAN and Rangers to ENR.
        // This one uses RAN for Rangers. Merging the two datasets by abbreviation
        // would have put Ranchers Bees' record on the defending champions, and
        // nothing downstream would have complained.
        assertEquals("Rangers International FC", NpflClubs.name("RAN"))
        assertEquals("Ranchers Bees FC", NpflClubs.name("RAB"))
        assertEquals(9, NpflClubs.record("RAN").leagueTitles)
        assertEquals(0, NpflClubs.record("RAB").leagueTitles)
    }

    @Test
    fun `the two clubs not playing at home are marked as such`() {
        val displaced = NpflClubs.displaced().map { it.id }.toSet()
        assertEquals(setOf("RAN", "DOM"), displaced)
        // Rangers' record must not quietly claim Enugu as the venue.
        val rangers = NpflClubs.record("RAN")
        assertTrue(rangers.stadium.contains("Abeokuta"))
        assertEquals("Nnamdi Azikiwe Stadium, Enugu", rangers.homeGroundOfRecord)
        // And a fixture list calling it a home game must know better.
        val atHome = SampleData.fixtures.first { it.homeClubId == "RAN" }
        assertTrue(atHome.homeAtNeutralGround)
        assertFalse(SampleData.fixtures.first { it.homeClubId == "BEN" }.homeAtNeutralGround)
    }

    @Test
    fun `no contested kit colour is offered for rendering`() {
        // Two secondary sources disagreeing is not a colour. Rangers, Enyimba and
        // Kano Pillars are in that position; the UI must fall back to its mark.
        val contested = NpflClubs.all.filter { it.kit.conflicted }.map { it.id }
        assertEquals(listOf("RAN", "KAN", "ENY"), contested)
        contested.forEach {
            assertFalse(NpflClubs.record(it).kit.renderable, "$it offers a contested colour")
            assertTrue(NpflClubs.record(it).kit.words != null, "$it must record what was said")
        }
        // And the unsourced three are honest about having nothing.
        val blank = NpflClubs.all.filter { it.kit.words == null }.map { it.id }
        assertEquals(listOf("BAR", "DOM", "INT"), blank)
    }

    @Test
    fun `no example player is attached to a club that left the league`() {
        val live = SampleData.clubs.map { it.id }.toSet()
        val stale = SampleData.pool.filterNot { it.clubId in live }
        assertTrue(stale.isEmpty(), "players on departed clubs: ${stale.map { it.name }}")
        SampleData.fixtures.forEach {
            assertTrue(it.homeClubId in live, "fixture home ${it.homeClubId}")
            assertTrue(it.awayClubId in live, "fixture away ${it.awayClubId}")
        }
        // Remo Stars were champions in 2024/25 and are not in this division.
        assertTrue("Remo Stars" in NpflClubs.relegatedAfter2025_26)
        assertTrue(SampleData.clubs.none { it.name.contains("Remo") })
    }

    @Test
    fun `Awazie is at Rivers United, not on Enyimba's own squad page`() {
        // The one place a dated report is allowed to overrule a club's own site:
        // Enyimba's undated page still lists their former captain at 30.
        val awazie = NpflSquads.registered.first { it.lastName == "Awazie" }
        assertEquals("RIV", awazie.clubId)
        assertTrue(NpflSquads.forClub("ENY").none { it.lastName == "Awazie" })
        assertTrue(awazie.note!!.contains("dated report"))
    }

    @Test
    fun `every club id in the pool resolves to a real club`() {
        val ids = SampleData.clubs.map { it.id }.toSet()
        SampleData.pool.forEach { assertTrue(it.clubId in ids, "${it.name} -> ${it.clubId}") }
        NpflSquads.registered.forEach { assertTrue(it.clubId in ids, "${it.name} -> ${it.clubId}") }
    }

    @Test
    fun `the type specimen carries the diacritics the type system has to support`() {
        // Yoruba under-dots and tone marks, Igbo dotted vowels, Hausa hooks (§02).
        // These live in a specimen and not in the player pool on purpose: a font
        // test does not need invented players, and invented players read as real
        // to the users who know this league best.
        val specimen = NigerianOrthographySpecimen.all.joinToString(" ").lowercase()
        val missing = NigerianOrthographySpecimen.requiredMarks.filterNot { specimen.contains(it) }
        assertTrue(
            missing.isEmpty(),
            "the specimen must exercise every mark class in section 02; missing: $missing"
        )
    }

    @Test
    fun `the specimen includes a stacked combining mark, which is the hard case`() {
        // The reason §02 says a display face that cannot set a player's name is
        // not a candidate. "Ọ̀gbọ́nna" is not precomposed: it is a dotted vowel
        // with a separate combining tone mark stacked on top, and most display
        // faces — Druk included — have no glyph for that pair. Anything that
        // renders names must go through BrandType.NigerianText.
        fun stacks(line: String) =
            line.any { ch -> Character.getType(ch) == Character.NON_SPACING_MARK.toInt() }

        assertEquals(
            NigerianOrthographySpecimen.stackingLines,
            NigerianOrthographySpecimen.all.filter(::stacks),
            "exactly the Yoruba and Igbo lines should stack marks"
        )

        // Hausa is the other failure mode, and it is not a stacking one: each
        // hooked letter is a single code point, so the question is whether the
        // face has the glyph at all. A font missing it draws a box.
        assertFalse(stacks(NigerianOrthographySpecimen.HAUSA))
        NigerianOrthographySpecimen.hausaHooks.forEach { hook ->
            assertEquals(
                1, hook.toString().length,
                "$hook must be one code point, not a composed sequence"
            )
        }
        assertTrue(
            NigerianOrthographySpecimen.hausaHooks.any { NigerianOrthographySpecimen.HAUSA.contains(it) }
        )
    }

    @Test
    fun `no real player's name carries a diacritic the sources did not use`() {
        // The sources write these names in plain Latin. Adding marks to make them
        // look more Nigerian would be inventing a spelling for a real person.
        val marked = NpflSquads.registered.filter { rp ->
            rp.name.any { ch -> Character.getType(ch) == Character.NON_SPACING_MARK.toInt() }
        }
        assertTrue(marked.isEmpty(), "names respelled with marks: ${marked.map { it.name }}")
    }

    @Test
    fun `the gameweek scores and the away bonus is doing real work`() {
        val squad = SampleData.squad
        val gw = Scoring.scoreSquad(squad, SampleData.gameweek12, SampleData.activeChip)
        assertEquals(11, gw.playerScores.size, "only the XI scores without Owambe")
        println("GW${SampleData.gameweekNumber} total = ${gw.points}")

        // The 1.8%-owned away forward must out-score the 61.4%-owned captain.
        val differential = gw.playerScores.first { it.playerId == "IKO-chilaka" }
        val captain = gw.playerScores.first { it.playerId == "RIV-awazie" }
        println("Chilaka (1.8% owned, 2 away goals) = ${differential.finalPoints}")
        println("Awazie (61.4% owned captain, blank) = ${captain.finalPoints}")
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
            "Chilaka's report is still open, so the total must read as provisional"
        )
    }

    @Test
    fun `the mini league table is consistent`() {
        SampleData.miniLeague.forEachIndexed { i, e -> assertEquals(i + 1, e.rank) }
        val totals = SampleData.miniLeague.map { it.totalPoints }
        assertEquals(totals.sortedDescending(), totals, "ranks must follow total points")
    }
}
