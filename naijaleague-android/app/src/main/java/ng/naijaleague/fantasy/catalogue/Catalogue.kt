package ng.naijaleague.fantasy.catalogue

import ng.naijaleague.fantasy.data.NpflClubs
import ng.naijaleague.fantasy.rules.Player

/**
 * The admin console's catalogue, as this app reads it.
 *
 * WHAT THIS CONNECTS. `packages/api` in the earlier repository serves an
 * operator console at `/console` over about thirty-six admin endpoints, and
 * until now nothing an operator did there reached a handset: the app rendered
 * compiled-in constants, so a club colour typed into the console at nine in the
 * morning was still invisible at nine at night. This is the wire between them.
 *
 * IT IS A READER, NEVER A WRITER. Only `GET /clubs` and `GET /players` are used,
 * and neither carries `requireRole` on the server, so the app sends no
 * credential and ships no token. Creating a player, correcting a fixture,
 * resolving a dispute — all of it stays behind the console's own login. Nothing
 * in this package can sign a request, so the app cannot become a writer by
 * accident or by a later careless edit.
 *
 * A FAILURE IS NOT AN ERROR STATE. The app already holds a complete, researched
 * catalogue; the console can only improve on it. So an unreachable server, a
 * timeout, a captive portal serving an HTML login page, a 500, a migration that
 * renamed a column — every one of them ends the same way, with the compiled-in
 * data standing and [CatalogueStatus] saying so. There is no spinner blocking a
 * pitch and no empty screen, because there is nothing to wait for.
 *
 * THE TRANSPORT IS A SEAM. [Transport] exists so that everything interesting
 * here — fetching, parsing, merging, refusing — is pure Kotlin that runs under
 * `gradle test` on the JVM in milliseconds. The one implementation that touches
 * a socket is small enough to read in a sitting and has no logic worth testing.
 */
object Catalogue {

    /** One HTTP GET. The only thing in this package that touches a network. */
    fun interface Transport {
        /**
         * @return the response body on 2xx
         * @throws Exception on anything else — the caller treats every failure
         *   identically, so there is nothing to be gained by a typed hierarchy
         */
        fun get(url: String): String
    }

    /**
     * Fetch, merge, and never throw.
     *
     * @param baseUrl the console's API root, e.g. `https://api.example.ng`
     */
    fun load(baseUrl: String, transport: Transport): Loaded {
        val root = baseUrl.trimEnd('/')
        if (root.isEmpty()) return Loaded.offline(CatalogueStatus.NotConfigured)

        val clubs = try {
            Wire.clubs(transport.get("$root/clubs"))
        } catch (e: Exception) {
            return Loaded.offline(CatalogueStatus.Unreachable(describe(e)))
        }
        val players = try {
            Wire.players(transport.get("$root/players"))
        } catch (e: Exception) {
            return Loaded.offline(CatalogueStatus.Unreachable(describe(e)))
        }

        // A server that answers with no clubs at all is a server with nothing to
        // say — an empty database, or a response this client failed to
        // understand. Either way the compiled-in twenty stand, rather than the
        // app showing a league with no teams in it.
        if (clubs.isEmpty()) return Loaded.offline(CatalogueStatus.Empty)

        val merged = Merge.apply(clubs, players)
        return Loaded(
            clubs = merged.clubs,
            consolePlayers = merged.players,
            status = CatalogueStatus.Connected(merged.report)
        )
    }

    /**
     * The catalogue the app should render.
     *
     * [clubs] is always twenty, always complete, and always safe to render: it
     * is the compiled-in list with whatever an operator improved folded in.
     */
    data class Loaded(
        val clubs: List<NpflClubs.ClubRecord>,
        val consolePlayers: List<Player>,
        val status: CatalogueStatus
    ) {
        companion object {
            fun offline(status: CatalogueStatus) =
                Loaded(NpflClubs.all, emptyList(), status)
        }
    }

    /**
     * A short, non-technical reason, safe to put in front of a manager.
     *
     * Deliberately does not include the exception's message: those carry host
     * names, ports, and occasionally a query string, and this string is
     * rendered on a screen the user can screenshot. The class name says what
     * kind of failure it was, which is all anybody needs from it here.
     */
    private fun describe(e: Exception): String = when (e) {
        is Json.ParseException -> "the server sent something this app could not read"
        else -> e::class.simpleName ?: "a network error"
    }
}

/**
 * Where the catalogue on screen came from.
 *
 * This product states its sources on the screens that use them — the pool says
 * how many clubs publish a squad, the club page says when a colour is contested,
 * and a stand-in says on its face that it is one. Reaching a server changes
 * where some of those facts come from, so it has to be sayable too. Every state
 * below has a sentence a manager can read.
 */
sealed interface CatalogueStatus {

    /** What the Profile screen shows for this state. */
    val summary: String

    /** No base URL was built in. The app is exactly what it always was. */
    data object NotConfigured : CatalogueStatus {
        override val summary =
            "Not connected to an operator console. Everything here is the researched " +
                "data that ships with the app."
    }

    /** A server was configured and could not be read. */
    data class Unreachable(val reason: String) : CatalogueStatus {
        override val summary =
            "Could not reach the operator console — $reason. Showing the researched data " +
                "that ships with the app, which is complete on its own."
    }

    /** Reached, and it had nothing. */
    data object Empty : CatalogueStatus {
        override val summary =
            "The operator console has no clubs recorded yet. Showing the researched data " +
                "that ships with the app."
    }

    data class Connected(val report: CatalogueReport) : CatalogueStatus {
        override val summary = report.summary
    }
}

/**
 * What the console actually contributed, counted.
 *
 * Rendered, not logged. A manager looking at a club colour that was blank last
 * week is entitled to know an operator supplied it, and a manager who can see
 * that four players are being held back is looking at an honest gap rather than
 * a bug.
 */
data class CatalogueReport(
    val clubIdentitiesFromConsole: Int,
    val kitsFromConsole: Int,
    /** Colours where two sources disagreed and an operator settled it. */
    val contestedKitsSettled: Int,
    val playersFromConsole: Int,
    /** Server rows that are its own stand-ins. Not imported; the app has its own. */
    val serverPlaceholdersIgnored: Int,
    val heldBackUnnamed: Int,
    val heldBackUnpriced: Int,
    /** Real players with no ownership figure yet. See [Merge] for why they wait. */
    val heldBackUnmeasured: Int,
    /** Injured, suspended or otherwise marked unavailable by an operator. */
    val heldBackUnavailable: Int
) {
    val heldBack: Int
        get() = heldBackUnnamed + heldBackUnpriced + heldBackUnmeasured + heldBackUnavailable

    val contributed: Boolean get() = clubIdentitiesFromConsole > 0 || kitsFromConsole > 0 || playersFromConsole > 0

    val summary: String get() = buildString {
        if (!contributed) {
            append(
                "Connected to the operator console. It has not added anything to the " +
                    "researched data yet."
            )
        } else {
            append("Connected to the operator console. ")
            val parts = ArrayList<String>()
            if (playersFromConsole > 0) {
                parts += "$playersFromConsole real ${plural(playersFromConsole, "player")}"
            }
            if (kitsFromConsole > 0) {
                parts += "$kitsFromConsole club ${plural(kitsFromConsole, "colour")}" +
                    if (contestedKitsSettled > 0) " ($contestedKitsSettled contested)" else ""
            }
            if (clubIdentitiesFromConsole > 0) {
                parts += "$clubIdentitiesFromConsole club ${plural(clubIdentitiesFromConsole, "record")}"
            }
            append(parts.joinToString(", ")).append(" came from an operator.")
        }
        if (heldBack > 0) {
            append(
                " $heldBack more ${plural(heldBack, "player")} the console has are held " +
                    "back: this app will not show a player it cannot name, price, or " +
                    "measure ownership for, because ownership pays a multiplier."
            )
        }
    }

    private fun plural(n: Int, word: String) = if (n == 1) word else "${word}s"
}
