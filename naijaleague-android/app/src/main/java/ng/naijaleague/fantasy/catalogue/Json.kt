package ng.naijaleague.fantasy.catalogue

/**
 * A JSON reader, written here rather than depended on.
 *
 * WHY NOT A LIBRARY. `rules/`, `data/` and this package are deliberately
 * Android-free and dependency-free: that is what lets the whole engine be
 * tested on the JVM in seconds with no emulator, and it is the reason this
 * project can prove anything at all about itself. org.json is on the Android
 * platform but not on the JVM, so depending on it would put the parsing half of
 * this integration beyond the reach of the tests — exactly the half most likely
 * to be wrong. kotlinx.serialization works in both, at the cost of a Gradle
 * plugin and a dependency, for payloads that are objects of scalars and arrays
 * of objects.
 *
 * So: about two hundred lines, no dependencies, and a test file that hits it
 * with the malformed input a real network eventually delivers.
 *
 * WHAT IT DOES NOT DO. No writing, no streaming, and no numeric tower — every
 * number is a [Double] and the accessors below narrow it. That is the whole of
 * the JSON this client needs, and a parser that does less has less to get wrong.
 *
 * DEPTH IS BOUNDED. A hostile or broken response of ten thousand open brackets
 * would otherwise recurse until the stack gives out, which on Android takes the
 * process with it. [MAX_DEPTH] turns that into an ordinary parse failure, which
 * every caller here already handles.
 */
internal object Json {

    /** Deeper than any real payload, shallower than the stack. */
    private const val MAX_DEPTH = 64

    class ParseException(message: String) : Exception(message)

    /**
     * Parse one JSON document.
     *
     * @return a [Map], [List], [String], [Double], [Boolean], or null
     * @throws ParseException on anything that is not one well-formed document
     */
    fun parse(text: String): Any? {
        val reader = Reader(text)
        reader.skipWhitespace()
        val value = reader.readValue(0)
        reader.skipWhitespace()
        // Trailing content means this was not one document. A valid object
        // followed by an HTML error page must not read as the object: that is
        // how a captive portal's interstitial becomes "live data".
        if (!reader.exhausted) reader.fail("trailing content after the document")
        return value
    }

    private class Reader(private val text: String) {
        private var at = 0

        val exhausted: Boolean get() = at >= text.length

        fun fail(what: String): Nothing = throw ParseException("$what at offset $at")

        fun skipWhitespace() {
            while (at < text.length && text[at].isJsonWhitespace()) at++
        }

        fun readValue(depth: Int): Any? {
            if (depth > MAX_DEPTH) fail("nested deeper than $MAX_DEPTH")
            if (exhausted) fail("expected a value, found the end of the document")
            return when (val c = text[at]) {
                '{' -> readObject(depth)
                '[' -> readArray(depth)
                '"' -> readString()
                't' -> readLiteral("true", true)
                'f' -> readLiteral("false", false)
                'n' -> readLiteral("null", null)
                else -> if (c == '-' || c in '0'..'9') readNumber() else fail("unexpected '$c'")
            }
        }

        private fun readObject(depth: Int): Map<String, Any?> {
            at++
            val out = LinkedHashMap<String, Any?>()
            skipWhitespace()
            if (!exhausted && text[at] == '}') { at++; return out }
            while (true) {
                skipWhitespace()
                if (exhausted || text[at] != '"') fail("expected a key")
                val key = readString()
                skipWhitespace()
                if (exhausted || text[at] != ':') fail("expected ':'")
                at++
                skipWhitespace()
                out[key] = readValue(depth + 1)
                skipWhitespace()
                if (exhausted) fail("unterminated object")
                when (text[at]) {
                    ',' -> at++
                    '}' -> { at++; return out }
                    else -> fail("expected ',' or '}'")
                }
            }
        }

        private fun readArray(depth: Int): List<Any?> {
            at++
            val out = ArrayList<Any?>()
            skipWhitespace()
            if (!exhausted && text[at] == ']') { at++; return out }
            while (true) {
                skipWhitespace()
                out += readValue(depth + 1)
                skipWhitespace()
                if (exhausted) fail("unterminated array")
                when (text[at]) {
                    ',' -> at++
                    ']' -> { at++; return out }
                    else -> fail("expected ',' or ']'")
                }
            }
        }

        private fun readString(): String {
            at++
            val out = StringBuilder()
            while (true) {
                if (exhausted) fail("unterminated string")
                when (val c = text[at]) {
                    '"' -> { at++; return out.toString() }
                    '\\' -> { at++; out.append(decodeEscape()) }
                    else -> {
                        // A raw control character inside a string is malformed
                        // JSON, and letting it through would put a newline in
                        // the middle of a club name.
                        if (c < ' ') fail("control character in a string")
                        out.append(c)
                        at++
                    }
                }
            }
        }

        private fun decodeEscape(): Char {
            if (exhausted) fail("unterminated escape")
            val c = text[at]
            at++
            return when (c) {
                '"' -> '"'
                '\\' -> '\\'
                '/' -> '/'
                'b' -> '\b'
                'f' -> ''
                'n' -> '\n'
                'r' -> '\r'
                't' -> '\t'
                'u' -> {
                    if (at + 4 > text.length) fail("truncated unicode escape")
                    val hex = text.substring(at, at + 4)
                    val code = hex.toIntOrNull(16) ?: fail("bad unicode escape '$hex'")
                    at += 4
                    // Surrogates are returned as they come. A Kotlin String is
                    // UTF-16 already, so a well-formed pair reassembles itself
                    // when the two halves land next to each other.
                    code.toChar()
                }
                else -> fail("unknown escape")
            }
        }

        private fun readLiteral(word: String, value: Any?): Any? {
            if (!text.startsWith(word, at)) fail("expected '$word'")
            at += word.length
            return value
        }

        private fun readNumber(): Double {
            val start = at
            if (!exhausted && text[at] == '-') at++
            while (!exhausted && text[at] in '0'..'9') at++
            if (!exhausted && text[at] == '.') {
                at++
                while (!exhausted && text[at] in '0'..'9') at++
            }
            if (!exhausted && (text[at] == 'e' || text[at] == 'E')) {
                at++
                if (!exhausted && (text[at] == '+' || text[at] == '-')) at++
                while (!exhausted && text[at] in '0'..'9') at++
            }
            val slice = text.substring(start, at)
            return slice.toDoubleOrNull() ?: throw ParseException("bad number '$slice'")
        }
    }

    private fun Char.isJsonWhitespace() =
        this == ' ' || this == '\t' || this == '\n' || this == '\r'
}

/*
 * The accessors.
 *
 * Every one returns null rather than throwing when a field is absent or the
 * wrong type, because THE SERVER IS ALLOWED TO ADD AND CHANGE FIELDS. A client
 * that throws on an unexpected shape breaks the app the first time somebody
 * runs a migration; a missing field should cost one club's nickname, not the
 * whole catalogue.
 *
 * A Postgres null arrives as a JSON null, which is indistinguishable here from
 * a field nobody sent. Both mean "the server is not telling us", which is the
 * same thing to every caller in this package.
 */

@Suppress("UNCHECKED_CAST")
internal fun Any?.obj(): Map<String, Any?>? = this as? Map<String, Any?>

internal fun Any?.arr(): List<Any?>? = this as? List<Any?>

internal fun Map<String, Any?>.str(key: String): String? =
    (this[key] as? String)?.trim()?.takeIf { it.isNotEmpty() }

internal fun Map<String, Any?>.num(key: String): Double? = when (val v = this[key]) {
    is Double -> v
    // node-postgres hands back NUMERIC and BIGINT as strings rather than lose
    // precision, so `strength` and `market_value_eur` arrive quoted. They are
    // numbers the moment anybody reads them.
    is String -> v.toDoubleOrNull()
    else -> null
}

internal fun Map<String, Any?>.int(key: String): Int? = num(key)?.let {
    if (it.isFinite() && it >= Int.MIN_VALUE.toDouble() && it <= Int.MAX_VALUE.toDouble()) {
        it.toInt()
    } else {
        null
    }
}

internal fun Map<String, Any?>.long(key: String): Long? = num(key)?.let {
    if (it.isFinite() && it >= Long.MIN_VALUE.toDouble() && it <= Long.MAX_VALUE.toDouble()) {
        it.toLong()
    } else {
        null
    }
}

internal fun Map<String, Any?>.bool(key: String): Boolean? = this[key] as? Boolean
