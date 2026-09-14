package ng.naijaleague.fantasy.catalogue

import ng.naijaleague.fantasy.data.NpflClubs

/**
 * The two systems do not name clubs the same way, and neither key joins alone.
 *
 * The console's primary key is a slug — `enugu-rangers`, `shooting-stars` — and
 * this app's is a three-letter code. The obvious join is the server's `abbr`
 * column, which looks like the app's id and is NOT it: fifteen of the twenty
 * agree and five do not.
 *
 *     app   server
 *     IKO   IKC     Ikorodu City
 *     INT   INL     Inter Lagos
 *     KAN   KNP     Kano Pillars
 *     3SC   SSC     Shooting Stars
 *     RAB   RBE     Ranchers Bees
 *
 * Joining on `abbr` would therefore have worked for most of the league and
 * silently dropped those five — including Kano Pillars, Shooting Stars, and
 * Ikorodu City, which is one of only two clubs with a squad this app can stand
 * behind. A merge that quietly does nothing for a quarter of the division is
 * worse than one that does nothing at all, because nobody goes looking.
 *
 * So the mapping is written down, keyed by the server's SLUG rather than its
 * abbreviation: a slug is the actual primary key, it is stable, and it is
 * legible enough that a wrong line here is visible on sight. [everyClubIsMapped]
 * holds it to the real server's list.
 */
internal object ClubIds {

    /** Server slug to this app's club id. */
    private val toApp: Map<String, String> = mapOf(
        "abia-warriors" to "ABW",
        "barau-fc" to "BAR",
        "bendel-insurance" to "BEN",
        "doma-united" to "DOM",
        "enugu-rangers" to "RAN",
        "enyimba" to "ENY",
        "ikorodu-city" to "IKO",
        "inter-lagos" to "INT",
        "kano-pillars" to "KAN",
        "katsina-united" to "KAT",
        "kun-khalifat" to "KUN",
        "kwara-united" to "KWA",
        "nasarawa-united" to "NAS",
        "niger-tornadoes" to "NIT",
        "plateau-united" to "PLA",
        "ranchers-bees" to "RAB",
        "rivers-united" to "RIV",
        "shooting-stars" to "3SC",
        "sporting-lagos" to "SPL",
        "warri-wolves" to "WAR"
    )

    /**
     * This app's id for a club the server named, or null if it is not one of
     * the twenty.
     *
     * Falls back to the abbreviation for a slug nobody has mapped, since that
     * is right three times in four and a club the app does not recognise is
     * dropped anyway. It is a fallback, not the mechanism: a new club belongs
     * in the map above, and [everyClubIsMapped] is what says so.
     */
    fun appId(slug: String?, abbr: String?): String? {
        val mapped = slug?.let { toApp[it] }
        if (mapped != null) return mapped
        return abbr?.takeIf { code -> NpflClubs.all.any { it.id == code } }
    }

    /** The slugs this map knows, for the test that checks them against a real server. */
    val knownSlugs: Set<String> get() = toApp.keys

    /** Every club the map points at exists in this app. */
    val everyClubIsMapped: Boolean
        get() = toApp.values.all { id -> NpflClubs.all.any { it.id == id } } &&
            toApp.values.toSet().size == toApp.size
}
