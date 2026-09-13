package ng.naijaleague.fantasy.brand

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Copy drift guard.
 *
 * strings.xml used to hold 34 strings that nothing referenced, while the same
 * copy sat hardcoded in composables — and the two had already diverged on the
 * onboarding headline. The file claimed the board-locked lines could not be
 * edited without a board decision while having no authority over anything the
 * user actually saw.
 *
 * These tests read the real resource file and the real sources, so the claim is
 * now enforced rather than asserted in a comment.
 *
 * Gradle runs unit tests with the module directory as the working directory.
 */
class BrandCopyTest {

    private val moduleDir = File(".").absoluteFile.parentFile!!
    private val stringsXml = File(moduleDir, "src/main/res/values/strings.xml")
    private val sourceDir = File(moduleDir, "src/main/java")

    private fun strings(): Map<String, String> {
        assertTrue("strings.xml not found at ${stringsXml.absolutePath}", stringsXml.exists())
        val text = stringsXml.readText()
        return Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(text)
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    /** Kotlin sources with KDoc/comment lines stripped, so quoting the brand system is allowed. */
    private fun code(): String = sourceDir.walkTopDown()
        .filter { it.extension == "kt" }
        .joinToString("\n") { file ->
            file.readLines()
                .filterNot { it.trimStart().startsWith("*") || it.trimStart().startsWith("//") }
                .joinToString("\n")
        }

    @Test
    fun `the board-locked lines are exactly what the board approved`() {
        val s = strings()
        assertEquals("NaijaLeague Fantasy", s["app_name"])
        assertEquals("NaijaLeague", s["app_name_short"])
        assertEquals("Build Your NPFL Dream Team", s["tagline"])
        assertEquals("Fantasy football for real NPFL fans", s["endline"])
        assertEquals("Table no dey lie.", s["campaign_line"])
    }

    @Test
    fun `no composable hardcodes a string that strings xml already owns`() {
        val source = code()
        val duplicated = strings().filter { (_, value) ->
            // compare on the first line, since multi-line copy is escaped in XML
            val probe = value.replace("\\'", "'").substringBefore("\\n").take(40)
            probe.length > 14 && source.contains(probe)
        }.keys
        assertTrue(
            "these are defined in strings.xml AND hardcoded in a composable, so they will " +
                "drift apart: $duplicated",
            duplicated.isEmpty()
        )
    }

    @Test
    fun `every string in the file is actually used`() {
        val source = code()
        val manifest = File(moduleDir, "src/main/AndroidManifest.xml").readText()
        // Asserted by this test file rather than referenced from a composable.
        val heldForTests = setOf("app_name_short", "tagline", "campaign_line")
        val orphans = strings().keys.filterNot { name ->
            source.contains("R.string.$name") ||
                manifest.contains("@string/$name") ||
                name in heldForTests
        }
        assertTrue("strings.xml defines copy nothing uses: $orphans", orphans.isEmpty())
    }

    @Test
    fun `the word Draft never reaches a user`() {
        val s = strings()
        val offenders = s.filterValues { it.contains("draft", ignoreCase = true) }.keys
        assertTrue("the board replaced \"Draft\" with \"Choose\": $offenders", offenders.isEmpty())
    }

    @Test
    fun `the endline is never used in onboarding`() {
        // §12: "real fans" is an invitation, never a test — and never in
        // onboarding, an empty state, or aimed at a user.
        val onboarding = File(sourceDir, "ng/naijaleague/fantasy/ui/screens/OnboardingScreen.kt")
        assertTrue(onboarding.exists())
        assertTrue(
            "OnboardingScreen must not render the endline",
            !onboarding.readText().contains("R.string.endline")
        )
    }
}
