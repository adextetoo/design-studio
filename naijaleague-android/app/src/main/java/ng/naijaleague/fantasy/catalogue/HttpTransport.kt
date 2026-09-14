package ng.naijaleague.fantasy.catalogue

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * The one thing in this package that touches a socket.
 *
 * `HttpURLConnection` rather than a client library, for the same reason
 * [Json] is hand-written: two GETs of a public JSON endpoint do not justify a
 * dependency, and the platform has had this class since API 1. Everything
 * worth testing lives on the other side of [Catalogue.Transport], so this file
 * is deliberately the least interesting one here.
 *
 * FOUR THINGS IT REFUSES TO DO, each of which a default would do for it:
 *
 *  1. WAIT. Both timeouts are set. `HttpURLConnection`'s default is zero,
 *     meaning wait forever, and a manager on a stalled connection would watch
 *     nothing happen rather than see the researched data the app already holds.
 *  2. FOLLOW A REDIRECT OFF HTTPS. Cross-protocol redirects are not followed by
 *     this class, which is the behaviour we want and not an accident — the API
 *     308s plain GETs to https in production, and a client that quietly walked
 *     that redirect backwards would be one config mistake away from reading a
 *     catalogue over plaintext.
 *  3. READ AN UNBOUNDED BODY. [MAX_BYTES] caps it. The twenty clubs and a few
 *     hundred players are tens of kilobytes; anything past the cap is a wrong
 *     endpoint or a hostile one, and buffering it whole would be the app's
 *     problem, not the server's.
 *  4. SEND ANYTHING. No credential, no device identifier, no user agent beyond
 *     the platform default. These are public read endpoints and the app has
 *     nothing to prove to them.
 */
class HttpTransport(
    private val connectTimeoutMs: Int = 4_000,
    private val readTimeoutMs: Int = 6_000
) : Catalogue.Transport {

    private companion object {
        /** Comfortably above a full catalogue, far below anything alarming. */
        const val MAX_BYTES = 4 * 1024 * 1024
    }

    override fun get(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IllegalStateException("HTTP $code")
            return connection.inputStream.use { stream ->
                val buffer = ByteArrayOutputStream()
                val chunk = ByteArray(16 * 1024)
                while (true) {
                    val read = stream.read(chunk)
                    if (read <= 0) break
                    if (buffer.size() + read > MAX_BYTES) {
                        throw IllegalStateException("response larger than ${MAX_BYTES / 1024}KB")
                    }
                    buffer.write(chunk, 0, read)
                }
                buffer.toString(Charsets.UTF_8.name())
            }
        } finally {
            connection.disconnect()
        }
    }
}
