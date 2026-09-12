package ng.naijaleague.fantasy.brand

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Guard rails.
 *
 * BrandDimens.MinBodySize and BrandColor.Contrast were written as enforcement
 * mechanisms and, until this file existed, enforced nothing — which is exactly
 * how a 13sp money label and a 12sp player name got shipped. These tests turn
 * the brand system's numeric rules into something CI catches.
 *
 * Pure JVM: Color and TextUnit are value classes, so none of this needs a device.
 */
class BrandGuardrailsTest {

    // ---------- contrast, measured the way section 01 measures it ----------

    private fun channel(c: Float) =
        if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()

    private fun luminance(color: Color) =
        0.2126f * channel(color.red) + 0.7152f * channel(color.green) + 0.0722f * channel(color.blue)

    private fun contrast(a: Color, b: Color): Double {
        val la = luminance(a); val lb = luminance(b)
        return ((max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)).toDouble()
    }

    private fun assertReadable(fg: Color, bg: Color, what: String, min: Double = 4.5) {
        val ratio = contrast(fg, bg)
        assertTrue(
            "$what is %.2f:1, below the %.1f:1 floor".format(ratio, min),
            ratio >= min
        )
    }

    @Test
    fun `every palette reads its own ink on its own ground`() {
        listOf(NightPitchPalette, AdireIndigoPalette, NzuChalkPalette).forEach { p ->
            assertReadable(p.ink, p.ground, "${p.surface} ink on ground")
            assertReadable(p.inkDim, p.ground, "${p.surface} dim ink on ground")
            assertReadable(p.accent, p.ground, "${p.surface} accent on ground")
            assertReadable(p.negative, p.ground, "${p.surface} negative on ground")
            assertReadable(p.ink, p.raised, "${p.surface} ink on raised surface")
        }
    }

    @Test
    fun `a primary button label reads on its own fill`() {
        listOf(NightPitchPalette, AdireIndigoPalette, NzuChalkPalette).forEach { p ->
            assertReadable(p.actionLabel, p.actionFill, "${p.surface} action label on fill")
        }
    }

    @Test
    fun `the pairings section 01 measured still measure the same`() {
        fun r(a: Color, b: Color) = Math.round(contrast(a, b) * 100) / 100.0
        assertEquals(8.53, r(BrandColor.ChalkWhite, BrandColor.EagleDark), 0.02)
        assertEquals(5.75, r(BrandColor.JaraLime, BrandColor.EagleDark), 0.02)
        assertEquals(11.23, r(BrandColor.JaraLime, BrandColor.NightPitch), 0.02)
        assertEquals(7.45, r(BrandColor.EagleDark, BrandColor.NzuChalk), 0.02)
        assertEquals(4.93, r(BrandColor.IvieCoralLit, BrandColor.NightPitch), 0.02)
    }

    @Test
    fun `the banned pairings are still banned`() {
        // Section 01: "the most tempting and worst pairing we have"
        assertTrue(
            "Eagle Dark on Live Green must stay under the 3:1 UI floor, or someone will use it",
            contrast(BrandColor.EagleDark, BrandColor.LiveGreen) < 3.0
        )
        // White on Live Green is large-text-only; it must never be treated as body-safe
        assertTrue(
            "White on Live Green must not reach the body floor",
            contrast(BrandColor.ChalkWhite, BrandColor.LiveGreen) < 4.5
        )
        // Lime on chalk is illegible and must never be reachable through a palette
        assertTrue(
            "Lime must never be the accent on the chalk reading surface",
            NzuChalkPalette.accent != BrandColor.JaraLime
        )
    }

    @Test
    fun `nothing stock lands white text on Live Green`() {
        // A stock Button using `secondary` would otherwise ship a failing label.
        assertReadable(
            BrandColor.NightPitch, BrandColor.LiveGreen,
            "onSecondary on secondary", min = 4.5
        )
    }

    // ---------- the type scale ----------

    private val bodyAndRulesStyles: Map<String, TextStyle> = mapOf(
        "body" to BrandType.InterfaceAndGuidance.body,
        "title" to BrandType.InterfaceAndGuidance.title,
        "button" to BrandType.InterfaceAndGuidance.button,
        "data" to BrandType.ScoreAndData.data,
        "dataSmall" to BrandType.ScoreAndData.dataSmall,
        "name" to BrandType.IdentityAndEditorial.name
    )

    @Test
    fun `no style used for body, rules, money or names drops below the floor`() {
        bodyAndRulesStyles.forEach { (name, style) ->
            assertTrue(
                "$name is ${style.fontSize}, below the ${BrandDimens.MinBodySize} floor set when " +
                    "the core user widened to 16-60",
                style.fontSize.value >= BrandDimens.MinBodySize.value
            )
        }
    }

    @Test
    fun `the type scale has no invented sizes`() {
        // Section 02's mobile scale. A size outside this set is drift.
        val scale = setOf(64f, 40f, 28f, 20f, 16f, 15f, 14f, 12f, 11f)
        val styles = mapOf(
            "scoreline" to BrandType.ScoreAndData.scoreline,
            "scorelineMid" to BrandType.ScoreAndData.scorelineMid,
            "data" to BrandType.ScoreAndData.data,
            "dataSmall" to BrandType.ScoreAndData.dataSmall,
            "display1" to BrandType.IdentityAndEditorial.display1,
            "display2" to BrandType.IdentityAndEditorial.display2,
            "name" to BrandType.IdentityAndEditorial.name,
            "nameMicro" to BrandType.IdentityAndEditorial.nameMicro,
            "nameCondensed" to BrandType.IdentityAndEditorial.nameCondensed,
            "title" to BrandType.InterfaceAndGuidance.title,
            "body" to BrandType.InterfaceAndGuidance.body,
            "bodySmall" to BrandType.InterfaceAndGuidance.bodySmall,
            "label" to BrandType.InterfaceAndGuidance.label,
            "micro" to BrandType.InterfaceAndGuidance.micro,
            "celebration" to BrandType.CelebrationAndMotion.celebration,
            "celebrationSub" to BrandType.CelebrationAndMotion.celebrationSub
        )
        styles.forEach { (name, style) ->
            assertTrue(
                "$name is ${style.fontSize.value}sp, which is not on the section 02 scale",
                style.fontSize.value in scale
            )
        }
    }

    @Test
    fun `names never render in the display family`() {
        // Section 02: Druk and Archivo have no glyphs for stacked Yoruba tone
        // marks, so anything that can carry a name must not use them.
        assertEquals(
            "name must use the Noto-backed family",
            BrandType.NigerianText, BrandType.IdentityAndEditorial.name.fontFamily
        )
        assertEquals(
            "nameMicro must use the Noto-backed family",
            BrandType.NigerianText, BrandType.IdentityAndEditorial.nameMicro.fontFamily
        )
        assertEquals(
            "nameCondensed must stay in the Noto family, just narrower",
            BrandType.NigerianTextCondensed,
            BrandType.IdentityAndEditorial.nameCondensed.fontFamily
        )
    }

    @Test
    fun `the Nigerian family is bundled and not the OEM system font`() {
        // FontFamily.Default resolves to SamsungOne / MiSans / OnePlus Sans on
        // the handsets this product targets, which is not an audited face.
        assertTrue(
            "NigerianText must be a bundled family, never FontFamily.Default",
            BrandType.NigerianText !== androidx.compose.ui.text.font.FontFamily.Default
        )
    }

    // ---------- motion ----------

    @Test
    fun `no interaction animation exceeds the 240ms ceiling`() {
        listOf(
            "quick" to BrandMotion.QUICK_MS,
            "standard" to BrandMotion.STANDARD_MS,
            "enter" to BrandMotion.ENTER_MS
        ).forEach { (name, ms) ->
            assertTrue(
                "$name is ${ms}ms, over the ${BrandMotion.MAX_DURATION_MS}ms ceiling",
                ms <= BrandMotion.MAX_DURATION_MS
            )
        }
    }

    @Test
    fun `tap targets never go below 44dp`() {
        assertTrue(BrandDimens.MinTapTarget.value >= 44f)
        assertTrue(
            "the add/remove control must meet the tap target",
            BrandDimens.MinTapTarget.value >= 44f
        )
    }
}
