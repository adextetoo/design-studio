package ng.naijaleague.fantasy.catalogue

import ng.naijaleague.fantasy.data.NpflClubs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * What a server is allowed to tell this app.
 *
 * These are the rules that keep the connection from being a downgrade. The app
 * ships a researched, cited catalogue; the console can improve on it and must
 * not be able to quietly replace it with a seeder's generated output, nor to
 * invent a scoring input.
 */
class CatalogueTest {

    /**
     * A club row shaped like the API's, with only the fields under test set.
     *
     * Keyed by the SERVER'S SLUG, because that is what the server sends. These
     * fixtures used this app's three-letter codes until a real server showed
     * that it does not use them, and a fixture that speaks the client's own
     * language cannot catch that.
     */
    private fun clubJson(
        slug: String,
        colourConfidence: String? = null,
        primary: String? = null,
        identityEdited: Boolean = false,
        nickname: String? = null,
        stadium: String? = null,
        capacity: Int? = null
    ): String = buildString {
        append("""{"id":"$slug"""")
        append(""","name":"Server Name","short_name":"Server","abbr":"ZZZ"""")
        colourConfidence?.let {
            // Exactly as catalogueService writes it: the sentinel in the
            // confidence, the word "admin" in the source.
            append(""","colour_confidence":"$it","colour_source":"admin"""")
        }
        primary?.let { append(""","primary_color":"$it"""") }
        append(""","identity_admin_edited":$identityEdited""")
        nickname?.let { append(""","nickname":"$it"""") }
        stadium?.let { append(""","stadium":"$it"""") }
        capacity?.let { append(""","capacity":$it""") }
        append("}")
    }

    private fun playerJson(
        id: String,
        clubId: String = "enyimba",
        position: String? = "MID",
        name: String? = "Console Player",
        price: Long? = 6_000_000,
        placeholder: Boolean = false,
        dataSource: String? = "admin_verified",
        ownership: String = "12.5",
        status: String? = "active"
    ): String = buildString {
        append("""{"id":"$id","club_id":"$clubId"""")
        position?.let { append(""","position":"$it"""") }
        name?.let { append(""","display_name":"$it"""") }
        price?.let { append(""","price":$it""") }
        append(""","is_placeholder":$placeholder""")
        dataSource?.let { append(""","data_source":"$it"""") }
        status?.let { append(""","status":"$it"""") }
        append(""","selected_by_percent":$ownership""")
        append("}")
    }

    private fun load(clubs: String, players: String): Catalogue.Loaded =
        Catalogue.load("https://api.test") { url ->
            if (url.endsWith("/clubs")) """{"clubs":[$clubs]}""" else """{"players":[$players]}"""
        }

    // ---- The floor ----

    @Test
    fun `an unreachable server leaves the app exactly as it shipped`() {
        val loaded = Catalogue.load("https://api.test") { throw java.net.SocketTimeoutException() }
        assertEquals(NpflClubs.all, loaded.clubs)
        assertTrue(loaded.consolePlayers.isEmpty())
        assertTrue(loaded.status is CatalogueStatus.Unreachable)
    }

    @Test
    fun `no configured console is a state, not a failure`() {
        val loaded = Catalogue.load("") { error("must not be called") }
        assertEquals(NpflClubs.all, loaded.clubs)
        assertEquals(CatalogueStatus.NotConfigured, loaded.status)
    }

    @Test
    fun `a server with no clubs does not empty the league`() {
        val loaded = load(clubs = "", players = "")
        assertEquals(20, loaded.clubs.size)
        assertEquals(CatalogueStatus.Empty, loaded.status)
    }

    @Test
    fun `a reason put in front of a manager never carries the host`() {
        // This string is rendered on a screen somebody can screenshot.
        val loaded = Catalogue.load("https://api.internal.example:8443") {
            throw java.net.ConnectException("Connection refused to api.internal.example:8443")
        }
        val status = loaded.status as CatalogueStatus.Unreachable
        assertFalse(status.summary.contains("api.internal"), status.summary)
        assertFalse(status.summary.contains("8443"), status.summary)
    }

    // ---- Clubs ----

    @Test
    fun `a seeded club changes nothing`() {
        // The server is seeded from the same core data these constants were
        // checked against. Taking its word would be a round trip to reach the
        // same fact with worse provenance.
        val loaded = load(
            clubs = clubJson("enyimba", nickname = "Server Nickname", stadium = "Server Stadium"),
            players = ""
        )
        val before = NpflClubs.all.first { it.id == "ENY" }
        assertEquals(before, loaded.clubs.first { it.id == "ENY" })
    }

    @Test
    fun `an operator's edit is taken`() {
        val loaded = load(
            clubs = clubJson("enyimba", identityEdited = true, nickname = "The People's Elephant", capacity = 20000),
            players = ""
        )
        val club = loaded.clubs.first { it.id == "ENY" }
        assertEquals("The People's Elephant", club.nickname)
        assertEquals(20000, club.capacity)
        // Untouched fields keep the researched value rather than being nulled
        // out by a partial row.
        assertEquals(NpflClubs.all.first { it.id == "ENY" }.stadium, club.stadium)
    }

    @Test
    fun `an operator fills a kit nobody could source`() {
        // Barau, Doma United and Inter Lagos have no findable kit reference at
        // all. NpflClubs names them as the argument for having a console.
        val before = NpflClubs.all.first { it.id == "BAR" }
        assertFalse(before.kit.renderable, "BAR is expected to start unsourced")

        val loaded = load(
            clubs = clubJson("barau-fc", colourConfidence = "admin_verified", primary = "#1B7F3B"),
            players = ""
        )
        val kit = loaded.clubs.first { it.id == "BAR" }.kit
        assertTrue(kit.renderable)
        assertEquals(0xFF1B7F3BL, kit.primary)
        assertEquals(NpflClubs.Confidence.HIGH, kit.confidence)
    }

    @Test
    fun `an operator settles a contested kit`() {
        // Rangers: RSSSF says red, Wikipedia says green, and NpflClubs refuses
        // to pick a side. Somebody who has seen them play can.
        val before = NpflClubs.all.first { it.id == "RAN" }
        assertTrue(before.kit.conflicted, "RAN is expected to start contested")

        val loaded = load(
            clubs = clubJson("enugu-rangers", colourConfidence = "admin_verified", primary = "#00843D"),
            players = ""
        )
        val after = loaded.clubs.first { it.id == "RAN" }
        assertFalse(after.kit.conflicted)
        assertTrue(after.kit.renderable)
        assertTrue(after.kit.note!!.contains("settled it"), after.kit.note!!)
        assertEquals(1, (loaded.status as CatalogueStatus.Connected).report.contestedKitsSettled)
    }

    @Test
    fun `a seeded colour cannot launder a guess into a fact`() {
        val loaded = load(
            clubs = clubJson("enugu-rangers", colourConfidence = "low", primary = "#C8102E"),
            players = ""
        )
        assertTrue(loaded.clubs.first { it.id == "RAN" }.kit.conflicted)
    }

    @Test
    fun `a malformed colour leaves the compiled-in value alone`() {
        // Falling through to black would be a confident claim. The app would
        // rather render its generated mark than a wrong shirt.
        for (bad in listOf("", "#", "#12345", "#GGGGGG", "rgb(0,0,0)", "green")) {
            assertNull(Merge.hexToArgb(bad), "accepted '$bad'")
        }
        assertEquals(0xFF00843DL, Merge.hexToArgb("#00843D"))
        assertEquals(0xFF00843DL, Merge.hexToArgb("00843D"))
        assertEquals(0x8000843DL, Merge.hexToArgb("#8000843D"))
    }

    @Test
    fun `a club the app does not know is ignored rather than added`() {
        // A relegated club left in somebody's database must not walk back into
        // the division through this wire.
        val loaded = load(clubs = clubJson("remo-stars", identityEdited = true), players = "")
        assertEquals(20, loaded.clubs.size)
        assertTrue(loaded.clubs.none { it.id == "REMO" })
    }

    // ---- Players ----

    @Test
    fun `a verified player arrives as a real person`() {
        val loaded = load(clubs = clubJson("enyimba"), players = playerJson("p1"))
        val player = loaded.consolePlayers.single()
        assertEquals("Console Player", player.name)
        assertFalse(player.isPlaceholder)
        assertEquals(12.5, player.ownershipPct)
        assertNotNull(player.sourceNote)
    }

    @Test
    fun `the server's own stand-ins are not imported`() {
        // The app already generates labelled stand-ins for the eighteen clubs
        // that publish nothing. A second set carries no more information.
        val loaded = load(
            clubs = clubJson("enyimba"),
            players = listOf(
                playerJson("p1", placeholder = true, dataSource = "generated"),
                playerJson("p2", placeholder = false, dataSource = "generated"),
                playerJson("p3", placeholder = true, dataSource = "admin_verified")
            ).joinToString(",")
        )
        assertTrue(loaded.consolePlayers.isEmpty())
        assertEquals(3, (loaded.status as CatalogueStatus.Connected).report.serverPlaceholdersIgnored)
    }

    @Test
    fun `a missing data_source is treated as a stand-in, never as a person`() {
        // An older server that does not send the column must not have its
        // players promoted into the pool by a client's default.
        val loaded = load(clubs = clubJson("enyimba"), players = playerJson("p1", dataSource = null))
        assertTrue(loaded.consolePlayers.isEmpty())
    }

    @Test
    fun `unknown ownership never becomes zero`() {
        // THE TRAP THIS WHOLE FILE EXISTS FOR. Section 8 pays 1.5x on a player
        // under 2% owned. The API sends null when no gameweek is open to
        // measure against; defaulting that to 0 would hand a player a fifty per
        // cent scoring bonus that nothing measured.
        val loaded = load(clubs = clubJson("enyimba"), players = playerJson("p1", ownership = "null"))
        assertTrue(loaded.consolePlayers.isEmpty())
        assertEquals(1, (loaded.status as CatalogueStatus.Connected).report.heldBackUnmeasured)
    }

    @Test
    fun `a measured zero is a fact and is kept`() {
        // 0 means a gameweek is open and nobody holds them, which is a genuine
        // differential and a genuine 1.5x. It must not be held back with the
        // unmeasured ones.
        val loaded = load(clubs = clubJson("enyimba"), players = playerJson("p1", ownership = "0"))
        assertEquals(0.0, loaded.consolePlayers.single().ownershipPct)
    }

    @Test
    fun `a player with no position stays out of the pool`() {
        // The same rule the researched data already follows: thirteen players
        // are confirmed by name with no position stated anywhere, and none of
        // them is in the pool.
        val loaded = load(clubs = clubJson("enyimba"), players = playerJson("p1", position = "COACH"))
        assertTrue(loaded.consolePlayers.isEmpty())
        assertEquals(1, (loaded.status as CatalogueStatus.Connected).report.heldBackUnnamed)
    }

    @Test
    fun `a player with no usable price stays out`() {
        for (price in listOf<Long?>(null, 0, -1)) {
            val loaded = load(clubs = clubJson("enyimba"), players = playerJson("p1", price = price))
            assertTrue(loaded.consolePlayers.isEmpty(), "accepted price $price")
        }
    }

    @Test
    fun `a player an operator marked unavailable is held back`() {
        // Player carries no availability, so an injured player imported here
        // would sit in the pool looking perfectly fit. Holding them back keeps
        // the operator's word instead of dropping it on the floor.
        for (state in listOf("injured", "suspended", "unavailable")) {
            val loaded = load(clubs = clubJson("enyimba"), players = playerJson("p1", status = state))
            assertTrue(loaded.consolePlayers.isEmpty(), "imported a $state player")
            assertEquals(
                1,
                (loaded.status as CatalogueStatus.Connected).report.heldBackUnavailable
            )
        }
    }

    @Test
    fun `the console has no shirt number to give`() {
        // The players table has no such column. The token renders the name
        // alone, which is honest — the app must not imply a number it was never
        // told, and the researched players that DO have one still show it.
        val loaded = load(clubs = clubJson("enyimba"), players = playerJson("p1"))
        assertNull(loaded.consolePlayers.single().squadNumber)
    }

    @Test
    fun `a console id cannot collide with a researched one`() {
        val loaded = load(clubs = clubJson("enyimba"), players = playerJson("ENY-asibe"))
        assertEquals("console-ENY-asibe", loaded.consolePlayers.single().id)
    }

    // ---- What the manager is told ----

    @Test
    fun `every status has a sentence`() {
        val states = listOf(
            CatalogueStatus.NotConfigured,
            CatalogueStatus.Empty,
            CatalogueStatus.Unreachable("a network error"),
            (load(clubs = clubJson("enyimba"), players = playerJson("p1")).status)
        )
        states.forEach {
            assertTrue(it.summary.length > 20, "thin summary: ${it.summary}")
            assertTrue(it.summary.trim().endsWith("."), "unpunctuated: ${it.summary}")
        }
    }

    @Test
    fun `the summary says what was held back and why`() {
        val loaded = load(
            clubs = clubJson("enyimba"),
            players = listOf(playerJson("p1"), playerJson("p2", ownership = "null")).joinToString(",")
        )
        val summary = loaded.status.summary
        assertTrue(summary.contains("1 real player"), summary)
        assertTrue(summary.contains("held back"), summary)
        assertTrue(summary.contains("multiplier"), summary)
    }

    @Test
    fun `a connected console that has added nothing says so plainly`() {
        val loaded = load(clubs = clubJson("enyimba"), players = "")
        assertTrue(loaded.status.summary.contains("not added anything"), loaded.status.summary)
    }
}
