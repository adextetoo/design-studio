package ng.naijaleague.fantasy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ng.naijaleague.fantasy.catalogue.Catalogue
import ng.naijaleague.fantasy.catalogue.CatalogueStatus
import ng.naijaleague.fantasy.catalogue.HttpTransport
import ng.naijaleague.fantasy.data.NpflClubs

/**
 * The catalogue the app is currently rendering.
 *
 * WHY A HOLDER AND NOT A REPOSITORY WITH A VIEWMODEL. There is one read, it
 * happens once per process, it cannot fail in a way the user has to act on, and
 * every screen wants the same answer. A ViewModel per screen would each hold
 * their own copy of twenty clubs and disagree with each other during the second
 * the fetch lands. `NaijaLeagueRoot` has no navigation graph for the same kind
 * of reason (§13), and this is the same call in the same spirit.
 *
 * THE APP IS COMPLETE BEFORE THIS RUNS AND STAYS COMPLETE IF IT FAILS. The
 * initial value is the researched catalogue that ships in the APK, so the pitch
 * draws on the first frame with no spinner, no skeleton and no empty state. If
 * the console answers, club records are swapped in and Compose recomposes what
 * changed. If it does not, nothing happens at all and [status] says why on the
 * Profile screen. There is no failure path here that a manager has to do
 * anything about, because there is nothing they are waiting for.
 */
object LiveCatalogue {

    private var loaded by mutableStateOf(
        Catalogue.Loaded.offline(CatalogueStatus.NotConfigured)
    )

    /**
     * Indexed, because [record] is called for every token on the pitch, every
     * row of the pool and every crest in the fixture list. Rebuilt whenever the
     * catalogue is replaced, which is at most once.
     */
    private var index: Map<String, NpflClubs.ClubRecord> =
        NpflClubs.all.associateBy { it.id }

    /** What to tell the manager about where this data came from. */
    val status: CatalogueStatus get() = loaded.status

    /** Real people an operator has entered in the console. Never stand-ins. */
    val consolePlayers get() = loaded.consolePlayers

    val clubs: List<NpflClubs.ClubRecord> get() = loaded.clubs

    /**
     * One club, live.
     *
     * Falls back to the compiled-in record rather than throwing on an id the
     * catalogue does not hold: a jersey is not the place to discover a data
     * problem, and the constant is always there.
     */
    fun record(clubId: String): NpflClubs.ClubRecord =
        index[clubId] ?: NpflClubs.record(clubId)

    /**
     * Read the console, once, off the main thread.
     *
     * A plain thread rather than a coroutine scope: this is one request with no
     * cancellation story worth having — if the process dies mid-fetch there is
     * nothing to clean up, and if the user leaves the app the answer is still
     * wanted when they come back. Fire and forget is the honest shape of it.
     */
    fun start(baseUrl: String) {
        if (baseUrl.isBlank()) return   // NotConfigured is already the state
        Thread({
            val result = Catalogue.load(baseUrl, HttpTransport())
            loaded = result
            index = result.clubs.associateBy { it.id }
        }, "catalogue").apply { isDaemon = true }.start()
    }
}
