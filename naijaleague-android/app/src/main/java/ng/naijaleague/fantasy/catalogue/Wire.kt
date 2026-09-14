package ng.naijaleague.fantasy.catalogue

import ng.naijaleague.fantasy.rules.Position

/**
 * What the admin console's API actually sends, named after its own columns.
 *
 * These are transcriptions of `packages/api/sql/schema.sql` and the migrations
 * on top of it, not a design. Where a name here looks wrong for this app —
 * `data_source`, `is_placeholder`, `identity_admin_edited` — it is the server's
 * name, kept so the two can be read side by side. [Merge] is where they turn
 * into this app's own vocabulary.
 *
 * ONLY THE PUBLIC READ ROUTES ARE MODELLED. `GET /clubs` and `GET /players`
 * carry no `requireRole`, so the app reads them with no credential at all and
 * ships no token. Everything an operator does — creating a player, correcting a
 * fixture, resolving a dispute — stays behind the console's own login, where it
 * belongs. This app is a reader of the catalogue, never a writer of it, and it
 * cannot become one by accident because nothing here can sign a request.
 */
internal object Wire {

    /**
     * A club as the console holds it.
     *
     * The identity fields carry their own provenance, which is the whole reason
     * this integration can be safe: `identity_admin_edited` is written only by
     * `updateClub()`, and `colour_source` distinguishes an operator's reading of
     * a shirt from the seeder's own guess. [Merge] reads those flags rather than
     * inferring anything from a field being non-empty — the exact mistake the
     * server's own migration 005 was written to fix.
     */
    data class Club(
        val id: String,
        val name: String?,
        val shortName: String?,
        val abbr: String?,
        val city: String?,
        val state: String?,
        val stadium: String?,
        val homeStadiumOfRecord: String?,
        val capacity: Int?,
        val groundshare: Boolean,
        val strength: Double?,
        val status: String?,
        val confidence: String?,
        val note: String?,
        val primaryColor: String?,
        val secondaryColor: String?,
        val colourWords: String?,
        val colourSource: String?,
        val colourConfidence: String?,
        val nickname: String?,
        val identityNote: String?,
        val identityAdminEdited: Boolean
    )

    /**
     * A player as the console holds it.
     *
     * NO SHIRT NUMBER. The `players` table has no such column — the importer
     * reads one out of a spreadsheet and nothing stores it — so a console
     * player reaches the pitch without the number the app's researched players
     * carry, and the token renders the name alone. Modelling a field the server
     * does not have would have read as "the server sent nothing" forever, which
     * is a slower way to find this out.
     *
     * `is_placeholder` and `data_source` come through unfiltered — the API's own
     * module doc says so, and adds that "a client that hides them is a client
     * choice; the API always tells the truth". This client does not hide them.
     * They are the two fields the whole merge turns on.
     */
    data class Player(
        val id: String,
        val clubId: String,
        val position: Position?,
        val firstName: String?,
        val lastName: String?,
        val displayName: String?,
        /** Raw naira, same units as the app's own prices. */
        val priceNaira: Long?,
        /** `active`, `injured`, `suspended` or `unavailable`. */
        val status: String?,
        val isPlaceholder: Boolean,
        val dataSource: String?,
        /**
         * `selected_by_percent`, and NULL IS NOT ZERO.
         *
         * The API is explicit: null means no current gameweek exists to measure
         * against, 0 means one does and nobody holds this player. Those "must
         * not collapse into the same number" — and in this app they especially
         * must not, because §08 pays 1.5x on a player under 2% owned. A null
         * read as zero is a fabricated fifty per cent scoring bonus.
         */
        val ownershipPct: Double?
    )

    /**
     * `GET /clubs` -> `{ "clubs": [ ... ] }`.
     *
     * A row whose club this app does not know is dropped rather than defaulted:
     * there is nothing useful to do with a club that is not in the division, and
     * inventing a key for it would put a phantom in the merge.
     */
    fun clubs(body: String): List<Club> =
        rows(body, "clubs").mapNotNull { row ->
            // The server's key is a slug; this app's is a three-letter code,
            // and the server's own `abbr` is not reliably that code. See ClubIds.
            val id = ClubIds.appId(row.str("id"), row.str("abbr")) ?: return@mapNotNull null
            Club(
                id = id,
                name = row.str("name"),
                shortName = row.str("short_name"),
                abbr = row.str("abbr"),
                city = row.str("city"),
                state = row.str("state"),
                stadium = row.str("stadium"),
                homeStadiumOfRecord = row.str("home_stadium_of_record"),
                capacity = row.int("capacity"),
                groundshare = row.bool("groundshare") ?: false,
                strength = row.num("strength"),
                status = row.str("status"),
                confidence = row.str("confidence"),
                note = row.str("note"),
                primaryColor = row.str("primary_color"),
                secondaryColor = row.str("secondary_color"),
                colourWords = row.str("colour_words"),
                colourSource = row.str("colour_source"),
                colourConfidence = row.str("colour_confidence"),
                nickname = row.str("nickname"),
                identityNote = row.str("identity_note"),
                identityAdminEdited = row.bool("identity_admin_edited") ?: false
            )
        }

    /** `GET /players` -> `{ "players": [ ... ] }`. */
    fun players(body: String): List<Player> =
        rows(body, "players").mapNotNull { row ->
            val id = row.str("id") ?: return@mapNotNull null
            // players.club_id is the same slug the clubs route keys on.
            val clubId = ClubIds.appId(row.str("club_id"), null) ?: return@mapNotNull null
            Player(
                id = id,
                clubId = clubId,
                position = position(row.str("position")),
                firstName = row.str("first_name"),
                lastName = row.str("last_name"),
                displayName = row.str("display_name"),
                priceNaira = row.long("price"),
                status = row.str("status"),
                // Absent reads as placeholder, never as real. An old server that
                // does not send the column must not have its players promoted
                // into the pool as verified people by a client's default.
                isPlaceholder = row.bool("is_placeholder") ?: true,
                dataSource = row.str("data_source"),
                // Absent and JSON-null are the same thing here and both mean
                // unmeasured, which is what the merge refuses to guess at.
                ownershipPct = row.num("selected_by_percent")
            )
        }

    private fun rows(body: String, key: String): List<Map<String, Any?>> {
        val document = Json.parse(body).obj() ?: return emptyList()
        return document[key].arr().orEmpty().mapNotNull { it.obj() }
    }

    /**
     * The server's four position codes are this app's four, and the CHECK
     * constraint on the column says there are no others. An unknown value still
     * reads as null rather than as a guess, and such a player stays out of the
     * pool exactly as an unpositioned researched player does.
     */
    private fun position(code: String?): Position? = when (code?.uppercase()) {
        "GK" -> Position.GK
        "DEF" -> Position.DEF
        "MID" -> Position.MID
        "FWD" -> Position.FWD
        else -> null
    }
}
