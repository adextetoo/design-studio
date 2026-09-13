package ng.naijaleague.fantasy.brand

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The illustration assets, checked as assets rather than trusted as files.
 *
 * These vectors are the app's backgrounds, and the thing that makes them work
 * across three surfaces is that they name their colours instead of carrying
 * them. A tile that hard-codes #04231A is correct on Night Pitch and invisible
 * on Nzu Chalk, and nobody finds that by reading — they find it on the Rules
 * tab, in review, if they are lucky.
 *
 * Gradle runs unit tests with the module directory as the working directory.
 */
class IllustrationAssetTest {

    private val moduleDir = File(".").absoluteFile.parentFile!!
    private val drawableDir = File(moduleDir, "src/main/res/drawable")
    private val colorsXml = File(moduleDir, "src/main/res/values/colors.xml")

    private fun illustrations(): List<File> {
        assertTrue("no drawable dir at ${drawableDir.absolutePath}", drawableDir.isDirectory)
        return drawableDir.listFiles { f: File -> f.name.startsWith("il_") && f.extension == "xml" }
            ?.sortedBy { it.name }
            .orEmpty()
    }

    private fun declaredColours(): Map<String, String> =
        Regex("""<color name="([^"]+)">(#[0-9A-Fa-f]{6,8})</color>""")
            .findAll(colorsXml.readText())
            .associate { it.groupValues[1] to it.groupValues[2].uppercase() }

    @Test
    fun `the motif set is present and each one is a vector`() {
        val files = illustrations()
        assertEquals(
            "the illustration system is five assets: four motif traditions plus the pitch",
            listOf(
                "il_adire_eleko.xml", "il_arewa_lattice.xml", "il_nsibidi_marks.xml",
                "il_pitch_arcs.xml", "il_uli_linework.xml"
            ),
            files.map { it.name }
        )
        files.forEach { f ->
            val text = f.readText()
            assertTrue("${f.name} is not a <vector>", text.contains("<vector"))
            assertTrue("${f.name} has no viewportWidth", text.contains("android:viewportWidth"))
            assertTrue("${f.name} has no viewportHeight", text.contains("android:viewportHeight"))
            assertTrue("${f.name} declares no path", text.contains("<path"))
        }
    }

    @Test
    fun `no illustration bakes a brand colour as hex`() {
        // The whole reason these are references. A baked hex is right on one
        // surface and wrong on the other two, and it silently un-themes the app.
        val offenders = mutableListOf<String>()
        illustrations().forEach { f ->
            Regex("""android:(fillColor|strokeColor|tint)="([^"]+)"""")
                .findAll(f.readText())
                .forEach { m ->
                    val value = m.groupValues[2]
                    val ok = value.startsWith("@color/") ||
                        value == "@android:color/white" ||
                        value == "@android:color/transparent"
                    if (!ok) offenders += "${f.name}: ${m.groupValues[1]}=$value"
                }
        }
        assertTrue(
            "illustrations must reference colours, not carry them: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `every illustration is tinted from a colour that actually exists`() {
        val declared = declaredColours().keys
        illustrations().forEach { f ->
            val tint = Regex("""android:tint="@color/([^"]+)"""").find(f.readText())
            assertTrue("${f.name} has no @color tint", tint != null)
            assertTrue(
                "${f.name} tints from @color/${tint!!.groupValues[1]}, which colors.xml does not define",
                tint.groupValues[1] in declared
            )
        }
    }

    @Test
    fun `every illustration is drawn back, not over the content`() {
        // A background at full strength is a foreground. §13: the motif is the
        // ground the scores sit on, and a score has to win every time.
        illustrations().forEach { f ->
            val alpha = Regex("""android:alpha="([0-9.]+)"""").find(f.readText())
            assertTrue("${f.name} sets no alpha, so it will draw at full strength", alpha != null)
            val value = alpha!!.groupValues[1].toFloat()
            assertTrue("${f.name} draws at $value, too strong for a background", value <= 0.20f)
            assertTrue("${f.name} draws at $value, which is invisible", value >= 0.04f)
        }
    }

    @Test
    fun `the XML palette has not drifted from the Compose one`() {
        // colors.xml is a second copy of brand/Color.kt — necessary, because the
        // vectors and the window background cannot read Kotlin. Two copies of a
        // palette is exactly the kind of thing that silently diverges.
        fun hex(color: androidx.compose.ui.graphics.Color): String {
            val argb = color.value shr 32
            return "#" + argb.toString(16).uppercase().padStart(8, '0')
        }

        val declared = declaredColours()
        val expected = mapOf(
            "eagle_dark" to BrandColor.EagleDark,
            "live_green" to BrandColor.LiveGreen,
            "jara_lime" to BrandColor.JaraLime,
            "chalk_white" to BrandColor.ChalkWhite,
            "night_pitch" to BrandColor.NightPitch,
            "night_pitch_raised" to BrandColor.NightPitchRaised,
            "adire_indigo" to BrandColor.AdireIndigo,
            "adire_indigo_raised" to BrandColor.AdireIndigoRaised,
            "nzu_chalk" to BrandColor.NzuChalk,
            "nzu_chalk_raised" to BrandColor.NzuChalkRaised,
            "ife_brass" to BrandColor.IfeBrass,
            "ivie_coral" to BrandColor.IvieCoral,
            "ivie_coral_lit" to BrandColor.IvieCoralLit,
            "ivie_coral_on_indigo" to BrandColor.IvieCoralOnIndigo,
            "uli_clay" to BrandColor.UliClay,
            "harmattan_haze" to BrandColor.HarmattanHaze,
            "haze_dim" to BrandColor.HazeDim,
            "chalk_ink_dim" to BrandColor.ChalkInkDim
        )
        expected.forEach { (name, token) ->
            assertEquals("@color/$name has drifted from brand/Color.kt", hex(token), declared[name])
        }
        assertEquals(
            "colors.xml carries a colour the Compose palette does not define",
            expected.keys.sorted(), declared.keys.sorted()
        )
    }
}
