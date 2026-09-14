package ng.naijaleague.fantasy.catalogue

import ng.naijaleague.fantasy.data.NpflClubs
import ng.naijaleague.fantasy.rules.Player

/**
 * What the console is allowed to tell this app, and what it is not.
 *
 * THE COMPILED-IN DATA IS THE FLOOR, NOT A CACHE. `NpflClubs` and `NpflSquads`
 * are researched and cited in docs/SOURCES.md — twenty clubs with sourced
 * grounds, capacities and honours, and fifty-six players whose club and
 * position both come from a club-official page or a dated 2026 report. A naive
 * integration replaces all of that with whatever a server happens to return,
 * and the moment it does, cited facts are silently swapped for a seeder's
 * generated output and nobody can tell. That is a downgrade wearing the costume
 * of an upgrade, and it is the specific failure this file exists to prevent.
 *
 * SO PROVENANCE DECIDES, NOT RECENCY. The server carries its own provenance
 * flags and they are load-bearing on both sides of this wire:
 *
 *   - `clubs.identity_admin_edited` is written only by the console's
 *     `updateClub()`. Migration 005 added it precisely because COALESCE "cannot
 *     tell WHO wrote the value it is preserving", and its own comment says a
 *     correction in the repository "could never reach an existing database"
 *     while provenance was inferred from non-emptiness. This client makes the
 *     same distinction for the same reason, and in the same direction.
 *   - `clubs.colour_source` distinguishes an operator's reading of a shirt from
 *     the seeder's guess at one.
 *   - `players.is_placeholder` and `players.data_source` come through
 *     unfiltered; the API's own module doc says "a client that hides them is a
 *     client choice; the API always tells the truth". This client does not hide
 *     them. They are what the whole player merge turns on.
 *
 * An operator's edit wins, because a person who has seen these teams play is a
 * better source than a week of searching — `NpflClubs.KitColours` says so in as
 * many words, and names Barau, Doma United and Inter Lagos as "the argument for
 * an operator console". Everything else the server sends loses, because it came
 * from the same seed as the app's own constants and there is nothing to gain by
 * taking the round trip.
 *
 * NOTHING HERE TOUCHES THE SCORING ENGINE. The merge produces the same
 * `ClubRecord` and `Player` types the app already renders, so a console-supplied
 * player is scored by exactly the code that scores a researched one, and a
 * console-supplied stand-in is labelled by exactly the code that labels a
 * generated one. There is no second path for server data to be treated
 * differently, which is the only way to be sure it is not.
 */
internal object Merge {

    /**
     * What the merge produced, and a count of every decision it made.
     *
     * The counts are not diagnostics — [CatalogueReport] is rendered to the
     * manager, because "where did this come from" is a question this product
     * answers on screen rather than in a log file.
     */
    data class Merged(
        val clubs: List<NpflClubs.ClubRecord>,
        /** Real people the console supplied. Stand-ins are not imported. */
        val players: List<Player>,
        val report: CatalogueReport
    )

    /**
     * The sentinel an operator's edit leaves behind, and IT IS IN
     * `colour_confidence`, NOT `colour_source`.
     *
     * This client checked `colour_source` first, on the strength of migration
     * 005's prose — "'admin_verified' is a value only updateClub() can write" —
     * which does not say which column. It is the confidence: `catalogueService`
     * sets `colour_confidence = 'admin_verified'` and `colour_source = 'admin'`,
     * while `colour_source` otherwise carries free text like
     * `rsssf-nigeria-colours-2010`. Reading the wrong one meant every operator
     * colour was silently declined, which is the failure mode this whole file
     * is built to avoid — so it is written down here rather than left to the
     * next person to rediscover.
     */
    private const val ADMIN_VERIFIED = "admin_verified"

    /** The only player status this app can render without qualifying it. */
    private const val ACTIVE = "active"

    /** `data_source` values that mean a row is about an actual person. */
    private val REAL_SOURCES = setOf(ADMIN_VERIFIED, "league_feed")

    fun apply(clubs: List<Wire.Club>, players: List<Wire.Player>): Merged {
        val byId = clubs.associateBy { it.id }
        var identitiesTaken = 0
        var kitsTaken = 0
        var kitsSettled = 0

        val mergedClubs = NpflClubs.all.map { record ->
            val wire = byId[record.id] ?: return@map record
            val kit = mergeKit(record, wire)
            if (kit !== record.kit) {
                kitsTaken++
                if (record.kit.conflicted) kitsSettled++
            }

            // Identity is taken only where an operator actually edited it. A
            // server that has merely been seeded knows nothing this app does
            // not, so taking its stadium would be a round trip to reach the
            // same fact with worse provenance.
            val identity = wire.identityAdminEdited
            if (identity) identitiesTaken++

            record.copy(
                kit = kit,
                nickname = if (identity) wire.nickname ?: record.nickname else record.nickname,
                stadium = if (identity) wire.stadium ?: record.stadium else record.stadium,
                homeGroundOfRecord =
                    if (identity) wire.homeStadiumOfRecord ?: record.homeGroundOfRecord
                    else record.homeGroundOfRecord,
                capacity = if (identity) wire.capacity ?: record.capacity else record.capacity,
                note = if (identity) wire.identityNote ?: record.note else record.note
            )
        }

        var placeholdersIgnored = 0
        var unpriced = 0
        var unmeasured = 0
        var unnamed = 0
        var unavailable = 0
        val mergedPlayers = ArrayList<Player>()

        for (wire in players) {
            // A stand-in from the server is not imported. The app already
            // generates its own labelled stand-ins for the eighteen clubs that
            // publish nothing, and a second set carrying no more information
            // would only double the noise. Counted, so the number is visible
            // rather than mysterious.
            if (wire.isPlaceholder || wire.dataSource !in REAL_SOURCES) {
                placeholdersIgnored++
                continue
            }
            val position = wire.position
            val name = wire.displayName
                ?: listOfNotNull(wire.firstName, wire.lastName).joinToString(" ").ifBlank { null }
            if (position == null || name == null) { unnamed++; continue }

            val price = wire.priceNaira
            if (price == null || price <= 0) { unpriced++; continue }

            // An operator marking somebody injured, suspended or unavailable is
            // making a statement, and this app has nowhere to show it: Player
            // carries no availability, so an injured player imported here would
            // sit in the pool looking perfectly fit. Holding them back keeps the
            // operator's word rather than dropping it on the floor.
            if (wire.status != null && wire.status != ACTIVE) { unavailable++; continue }

            // Ownership is a SCORING INPUT here, not decoration: §08's
            // differential multiplier reads it, and 0% pays 1.5x. The server
            // distinguishes "nobody holds them" (a genuine 0, and a genuine
            // 1.5x) from "no gameweek is open to measure against" (null), and
            // this client must not collapse the two — defaulting a null to zero
            // would hand a player a fifty per cent bonus that nothing measured.
            // So an unmeasured player waits rather than entering the pool.
            val ownership = wire.ownershipPct
            if (ownership == null) { unmeasured++; continue }

            mergedPlayers += Player(
                id = "console-${wire.id}",
                name = name,
                clubId = wire.clubId,
                position = position,
                priceNaira = price,
                ownershipPct = ownership,
                // The console has no shirt number to give — see Wire.Player.
                squadNumber = null,
                isPlaceholder = false,
                sourceNote = sourceNote(wire)
            )
        }

        return Merged(
            clubs = mergedClubs,
            players = mergedPlayers,
            report = CatalogueReport(
                clubIdentitiesFromConsole = identitiesTaken,
                kitsFromConsole = kitsTaken,
                contestedKitsSettled = kitsSettled,
                playersFromConsole = mergedPlayers.size,
                serverPlaceholdersIgnored = placeholdersIgnored,
                heldBackUnnamed = unnamed,
                heldBackUnpriced = unpriced,
                heldBackUnmeasured = unmeasured,
                heldBackUnavailable = unavailable
            )
        )
    }

    /**
     * A club's colours, taken only from an operator.
     *
     * This is the case the console was wanted for. Three clubs — Barau, Doma
     * United, Inter Lagos — have no findable kit reference at all and currently
     * render a generated mark. Three more — Rangers, Kano Pillars, Enyimba —
     * are `conflicted`, where two sources name different colours and
     * `NpflClubs` refuses to pick a side because "this file is not the place to
     * decide which". A console operator IS the place: somebody who has seen the
     * team play settles in ten seconds what a week of searching did not. So an
     * admin-verified colour both fills a blank and resolves a conflict, and the
     * note says which of the two happened.
     *
     * A colour the SEEDER wrote changes nothing — it arrives with a
     * `colour_confidence` of high, medium or low and a `colour_source` naming
     * the reference it came from, such as `rsssf-nigeria-colours-2010`. That is
     * the same stale reading this app's own constants were already checked
     * against, so accepting it would launder a guess into a fact by routing it
     * through a database.
     */
    private fun mergeKit(record: NpflClubs.ClubRecord, wire: Wire.Club): NpflClubs.KitColours {
        if (wire.colourConfidence != ADMIN_VERIFIED) return record.kit
        val primary = hexToArgb(wire.primaryColor) ?: return record.kit
        val secondary = hexToArgb(wire.secondaryColor)
        return NpflClubs.KitColours(
            primary = primary,
            secondary = secondary,
            words = wire.colourWords ?: record.kit.words,
            // An operator who has seen the shirt outranks two stale references
            // disagreeing with each other, which is the whole point of asking
            // one. HIGH is not flattery: it is the best evidence class this
            // field has ever had.
            confidence = NpflClubs.Confidence.HIGH,
            conflicted = false,
            note = if (record.kit.conflicted) {
                "Two sources named different colours; an operator settled it in the console."
            } else {
                "Supplied by an operator in the console."
            }
        )
    }

    /**
     * `#RRGGBB` or `#AARRGGBB` to the opaque ARGB long the palette wants.
     *
     * Strict on purpose. A malformed colour must not fall through to black — a
     * black shirt is a confident claim, and the whole point of `KitColours` is
     * that the app would rather render its generated mark than a wrong one. So
     * anything unparseable returns null and the compiled-in value stands.
     */
    fun hexToArgb(hex: String?): Long? {
        val body = hex?.trim()?.removePrefix("#") ?: return null
        if (body.length != 6 && body.length != 8) return null
        if (!body.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) return null
        val value = body.toLongOrNull(16) ?: return null
        return if (body.length == 6) 0xFF000000L or value else value
    }

    private fun sourceNote(wire: Wire.Player): String = when (wire.dataSource) {
        ADMIN_VERIFIED -> "Entered by an operator in the admin console."
        else -> "From the league feed, through the admin console."
    }
}
