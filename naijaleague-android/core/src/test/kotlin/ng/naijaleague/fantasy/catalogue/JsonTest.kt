package ng.naijaleague.fantasy.catalogue

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The parser is the half of this integration most likely to be wrong, because
 * it is the half a real network gets to choose the input for.
 */
class JsonTest {

    @Test
    fun `reads the shapes the API actually sends`() {
        val parsed = Json.parse(
            """{"clubs":[{"id":"ENY","capacity":25000,"groundshare":false,"note":null}]}"""
        ).obj()!!
        val club = parsed["clubs"].arr()!!.first().obj()!!
        assertEquals("ENY", club.str("id"))
        assertEquals(25000, club.int("capacity"))
        assertEquals(false, club.bool("groundshare"))
        assertNull(club.str("note"))
    }

    @Test
    fun `numeric strings are numbers`() {
        // node-postgres returns NUMERIC and BIGINT as strings rather than lose
        // precision, so `strength` and `market_value_eur` arrive quoted. A
        // client that only accepted JSON numbers would read every one as null.
        val row = Json.parse("""{"strength":"4.5","market_value_eur":"5100000"}""").obj()!!
        assertEquals(4.5, row.num("strength"))
        assertEquals(5_100_000L, row.long("market_value_eur"))
    }

    @Test
    fun `a null is not a zero and not an empty string`() {
        val row = Json.parse("""{"selected_by_percent":null,"note":"","city":"   "}""").obj()!!
        assertNull(row.num("selected_by_percent"))
        // Blank strings read as absent. A club whose note is "" has no note,
        // and rendering an empty source line is worse than rendering none.
        assertNull(row.str("note"))
        assertNull(row.str("city"))
    }

    @Test
    fun `a genuine zero survives`() {
        // The distinction the whole player merge turns on: 0% owned is a fact
        // that pays a 1.5x multiplier, and must never be confused with unknown.
        val row = Json.parse("""{"selected_by_percent":0}""").obj()!!
        assertEquals(0.0, row.num("selected_by_percent"))
    }

    @Test
    fun `a wrong type reads as absent rather than throwing`() {
        // The server is allowed to change. A field that arrives as the wrong
        // shape should cost that field, not the whole catalogue.
        val row = Json.parse("""{"capacity":"not a number","groundshare":"yes"}""").obj()!!
        assertNull(row.int("capacity"))
        assertNull(row.bool("groundshare"))
    }

    @Test
    fun `escapes decode`() {
        val row = Json.parse("""{"name":"Shooting\tStars \"3SC\"","x":"Ògbónna"}""").obj()!!
        assertEquals("Shooting\tStars \"3SC\"", row.str("name"))
        assertEquals("Ògbónna", row.str("x"))
    }

    @Test
    fun `an HTML error page is not a document`() {
        // A captive portal or a proxy interstitial is the realistic failure
        // here, and it must read as a failure rather than as an empty result.
        assertFailsWith<Json.ParseException> { Json.parse("<html><body>Sign in</body></html>") }
    }

    @Test
    fun `a truncated response is a failure`() {
        // A connection dropped mid-body. Half an array must never read as a
        // complete one, or a manager loses half their club list silently.
        assertFailsWith<Json.ParseException> { Json.parse("""{"clubs":[{"id":"ENY"},{"id":"R""") }
    }

    @Test
    fun `a valid document with rubbish after it is refused`() {
        // The specific shape of an interstitial appended to a real response.
        assertFailsWith<Json.ParseException> { Json.parse("""{"clubs":[]}<html>""") }
    }

    @Test
    fun `runaway nesting fails instead of taking the process with it`() {
        // Unbounded recursion on a hostile body is a crash, not an error, and a
        // crash on Android takes the app down rather than falling back.
        val bomb = "[".repeat(5_000) + "]".repeat(5_000)
        val failure = assertFailsWith<Json.ParseException> { Json.parse(bomb) }
        assertTrue(failure.message!!.contains("nested deeper"), failure.message!!)
    }

    @Test
    fun `a bare control character in a string is refused`() {
        assertFailsWith<Json.ParseException> { Json.parse("{\"name\":\"Enyimba\nFC\"}") }
    }

    @Test
    fun `an empty body is a failure, not an empty catalogue`() {
        assertFailsWith<Json.ParseException> { Json.parse("") }
    }
}
