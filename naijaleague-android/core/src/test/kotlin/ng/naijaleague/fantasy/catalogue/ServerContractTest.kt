package ng.naijaleague.fantasy.catalogue

import ng.naijaleague.fantasy.data.NpflClubs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The contract, taken from a running server rather than from reading its source.
 *
 * The payloads below were captured from `packages/api` actually running — real
 * Postgres, all nine migrations, `npm run seed` — and pasted in unedited. That
 * matters, because reading the schema is how three separate mistakes got into
 * this client and only running it found them:
 *
 *  1. THE JOIN KEY. The server's primary key is a slug (`enugu-rangers`), not
 *     this app's three-letter code, and its `abbr` column — which looks like a
 *     drop-in — disagrees with the app for five of the twenty clubs. Matching on
 *     either alone silently dropped a quarter of the division.
 *  2. `squad_number` DOES NOT EXIST. The importer parses one out of a
 *     spreadsheet and nothing ever stores it. A client field for it would have
 *     read as "the server sent nothing" forever.
 *  3. `strength` ARRIVES QUOTED. node-postgres returns NUMERIC as a string to
 *     avoid precision loss, so a client that only accepted JSON numbers reads
 *     every club's strength as null.
 *
 * If these payloads and the live API ever part company, this file is where it
 * shows up — which is the point of pasting them in rather than describing them.
 */
class ServerContractTest {

    /** Verbatim from `GET /clubs`, two rows, every field the server sends. */
    private val clubsBody = """
    {"clubs":[
      {"id":"enugu-rangers","name":"Rangers International","short_name":"Rangers","abbr":"RAN",
       "city":"Enugu","state":"Enugu","stadium":"MKO Abiola Sports Arena","home_stadium_of_record":
       "Nnamdi Azikiwe Stadium","capacity":28000,"groundshare":true,"strength":"4.0",
       "status":"continuing","confidence":"high","note":null,
       "created_at":"2026-09-14T07:51:26.386Z","primary_color":null,"secondary_color":null,
       "colour_words":null,"colour_source":null,"colour_confidence":null,
       "website_url":"https://npfl.com.ng/club/rangers-international-fc/","facebook_url":null,
       "x_url":null,"instagram_url":null,"nickname":"The Flying Antelopes","identity_note":null,
       "updated_at":"2026-09-14T07:51:26.386Z","identity_admin_edited":false,
       "added_by_operator":false,"transfermarkt_url":null},
      {"id":"shooting-stars","name":"Shooting Stars","short_name":"3SC","abbr":"SSC",
       "city":"Ibadan","state":"Oyo","stadium":"Lekan Salami Stadium","home_stadium_of_record":null,
       "capacity":12000,"groundshare":false,"strength":"3.5","status":"continuing",
       "confidence":"high","note":null,"created_at":"2026-09-14T07:51:26.386Z",
       "primary_color":"#FFFFFF","secondary_color":"#C8102E","colour_words":"White and red",
       "colour_source":"rsssf-nigeria-colours-2010","colour_confidence":"medium","website_url":null,"facebook_url":null,
       "x_url":null,"instagram_url":null,"nickname":"Oluyole Warriors","identity_note":null,
       "updated_at":"2026-09-14T07:51:26.386Z","identity_admin_edited":false,
       "added_by_operator":false,"transfermarkt_url":null}
    ]}
    """.trimIndent()

    /** Verbatim from `GET /players?clubId=enyimba`, one row. */
    private val playersBody = """
    {"players":[
      {"id":"d9209f9b-aa4f-4584-af14-903cf703180c","club_id":"enyimba","position":"FWD",
       "first_name":"Jephtha","last_name":"Clinton","display_name":"Jephtha Clinton",
       "price":11500000,"status":"active","is_placeholder":false,"data_source":"admin_verified",
       "created_at":"2026-09-14T07:51:26.386Z","updated_at":"2026-09-14T07:51:26.386Z",
       "status_note":null,"expected_return":null,"status_updated_at":null,
       "status_updated_by":null,"date_of_birth":null,"market_value_eur":null,
       "market_value_source":null,"selected_by_percent":null,"form":null}
    ]}
    """.trimIndent()

    @Test
    fun `a real clubs payload parses`() {
        val clubs = Wire.clubs(clubsBody)
        assertEquals(2, clubs.size)
        val rangers = clubs.first { it.id == "RAN" }
        assertEquals("Nnamdi Azikiwe Stadium", rangers.homeStadiumOfRecord)
        assertEquals(28000, rangers.capacity)
        assertTrue(rangers.groundshare)
        // Quoted NUMERIC. A client that only took JSON numbers reads null here.
        assertEquals(4.0, rangers.strength)
    }

    @Test
    fun `the slug is translated even where abbr disagrees`() {
        // Shooting Stars are SSC on the server and 3SC in this app. Joining on
        // abbr would have dropped them, and four others, in silence.
        val clubs = Wire.clubs(clubsBody)
        assertNotNull(clubs.firstOrNull { it.id == "3SC" }, "Shooting Stars did not join")
        assertNull(clubs.firstOrNull { it.id == "SSC" }, "joined on the server's abbr")
    }

    @Test
    fun `every club the real server serves maps to a club in this app`() {
        // The twenty slugs as `GET /clubs` returned them from a seeded database.
        val live = listOf(
            "abia-warriors", "barau-fc", "bendel-insurance", "doma-united", "enugu-rangers",
            "enyimba", "ikorodu-city", "inter-lagos", "kano-pillars", "katsina-united",
            "kun-khalifat", "kwara-united", "nasarawa-united", "niger-tornadoes",
            "plateau-united", "ranchers-bees", "rivers-united", "shooting-stars",
            "sporting-lagos", "warri-wolves"
        )
        assertEquals(20, live.size)
        assertEquals(live.toSet(), ClubIds.knownSlugs, "the map and the server disagree")
        live.forEach { slug ->
            val id = ClubIds.appId(slug, null)
            assertNotNull(id, "$slug maps to nothing")
            assertTrue(NpflClubs.all.any { it.id == id }, "$slug maps to unknown club $id")
        }
        assertTrue(ClubIds.everyClubIsMapped, "the map is not one-to-one onto this app's clubs")
    }

    @Test
    fun `a real player payload parses and joins to a club`() {
        val players = Wire.players(playersBody)
        val player = players.single()
        assertEquals("ENY", player.clubId)
        assertEquals("Jephtha Clinton", player.displayName)
        assertEquals(11_500_000L, player.priceNaira)
        assertEquals("active", player.status)
        assertEquals("admin_verified", player.dataSource)
    }

    @Test
    fun `a freshly seeded server contributes no players, and says so`() {
        // THE BEHAVIOUR ON A NEW INSTALL, and it is correct rather than broken.
        // selected_by_percent is null until a gameweek is open with entries in
        // it — the API sends null for "no gameweek to measure against" — and
        // this app will not put a player in the pool with an invented ownership,
        // because ownership pays a multiplier. So a fresh console improves club
        // records and holds its players back until the season is running.
        val loaded = Catalogue.load("https://api.test") { url ->
            if (url.endsWith("/clubs")) clubsBody else playersBody
        }
        assertTrue(loaded.consolePlayers.isEmpty())
        val report = (loaded.status as CatalogueStatus.Connected).report
        assertEquals(1, report.heldBackUnmeasured)
        assertTrue(loaded.status.summary.contains("held back"), loaded.status.summary)
    }

    @Test
    fun `the operator sentinel lives in colour_confidence, not colour_source`() {
        // Read out of a real database after a real PATCH through /admin/clubs:
        // catalogueService writes colour_confidence='admin_verified' and
        // colour_source='admin'. This client checked colour_source, on the
        // strength of migration 005's prose, and silently declined every
        // operator colour as a result — the exact failure the merge exists to
        // prevent, arrived at from the other direction.
        val edited = clubsBody
            .replace(
                "\"colour_source\":null,\"colour_confidence\":null",
                "\"colour_source\":\"admin\",\"colour_confidence\":\"admin_verified\""
            )
            .replace("\"primary_color\":null", "\"primary_color\":\"#00843D\"")

        val loaded = Catalogue.load("https://api.test") { url ->
            if (url.endsWith("/clubs")) edited else playersBody
        }
        val rangers = loaded.clubs.first { it.id == "RAN" }
        assertTrue(rangers.kit.renderable, "the operator's colour was declined")
        assertEquals(0xFF00843DL, rangers.kit.primary)
        assertEquals(1, (loaded.status as CatalogueStatus.Connected).report.contestedKitsSettled)
    }

    @Test
    fun `a seeded reference colour is still declined`() {
        // Shooting Stars carry colour_source='rsssf-nigeria-colours-2010' on the
        // server — the same stale 2010 reading this app already weighed and
        // recorded a confidence for. It must not come through just because it
        // is sitting in a database.
        val loaded = Catalogue.load("https://api.test") { url ->
            if (url.endsWith("/clubs")) clubsBody else playersBody
        }
        assertEquals(
            NpflClubs.all.first { it.id == "3SC" }.kit,
            loaded.clubs.first { it.id == "3SC" }.kit
        )
    }

    @Test
    fun `a seeded server changes none of the twenty clubs`() {
        // Nothing in this payload is admin-edited, so the researched records
        // must come through untouched — colours included. Shooting Stars carry
        // an rsssf colour on the server; this app is not to take it.
        val loaded = Catalogue.load("https://api.test") { url ->
            if (url.endsWith("/clubs")) clubsBody else playersBody
        }
        assertEquals(NpflClubs.all, loaded.clubs)
    }
}
