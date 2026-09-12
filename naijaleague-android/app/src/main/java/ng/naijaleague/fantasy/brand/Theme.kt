package ng.naijaleague.fantasy.brand

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The theme — brand system v1.1, sections 01, 02 and 13.
 *
 * The app's default world is dark. That is not a fashion choice: most of this
 * product is used at night, with one hand, on a mid-range Android screen at
 * half brightness to save battery (§01). Night Pitch is the ground.
 *
 * The chalk surface is not a "light mode" — it is the *reading* surface, used
 * deliberately for rules, long-form and receipts (§01, §13). Both are declared
 * here so a screen can ask for the right one instead of inventing colours.
 */

enum class BrandSurface { NIGHT_PITCH, ADIRE_INDIGO, NZU_CHALK }

/**
 * Surface-aware tokens. A composable reads these rather than reaching for a
 * raw colour, so the same component is legible on any of the three grounds and
 * the banned pairings in BrandColor.Contrast cannot happen by accident.
 */
data class BrandPalette(
    val surface: BrandSurface,
    val ground: Color,
    val raised: Color,
    val ink: Color,
    val inkDim: Color,
    val rule: Color,
    val accent: Color,
    val accentInk: Color,
    val honours: Color,
    val negative: Color,
    val isDark: Boolean
) {
    /** Primary action fill and label, resolved for this ground (§01). */
    val actionFill: Color get() = BrandColor.primaryActionFill(isDark)
    val actionLabel: Color get() = BrandColor.primaryActionLabel(isDark)
}

val NightPitchPalette = BrandPalette(
    surface = BrandSurface.NIGHT_PITCH,
    ground = BrandColor.NightPitch,
    raised = BrandColor.NightPitchRaised,
    ink = BrandColor.ChalkWhite,
    inkDim = BrandColor.HazeDim,
    rule = BrandColor.JaraLime.copy(alpha = 0.20f),
    accent = BrandColor.JaraLime,
    accentInk = BrandColor.NightPitch,
    honours = BrandColor.IfeBrass,
    negative = BrandColor.IvieCoralLit,
    isDark = true
)

val AdireIndigoPalette = BrandPalette(
    surface = BrandSurface.ADIRE_INDIGO,
    ground = BrandColor.AdireIndigo,
    raised = BrandColor.AdireIndigoRaised,
    ink = BrandColor.ChalkWhite,
    inkDim = Color(0xFFB9C0E0),
    rule = BrandColor.ChalkWhite.copy(alpha = 0.20f),
    accent = BrandColor.JaraLime,
    accentInk = BrandColor.AdireIndigo,
    honours = BrandColor.IfeBrass,
    negative = BrandColor.IvieCoralLit,
    isDark = true
)

/** The reading surface. Note the accent is Eagle Dark — lime does not exist on chalk. */
val NzuChalkPalette = BrandPalette(
    surface = BrandSurface.NZU_CHALK,
    ground = BrandColor.NzuChalk,
    raised = BrandColor.NzuChalkRaised,
    ink = BrandColor.NightPitch,
    inkDim = BrandColor.ChalkInkDim,
    rule = BrandColor.NightPitch.copy(alpha = 0.16f),
    accent = BrandColor.EagleDark,
    accentInk = BrandColor.ChalkWhite,
    honours = BrandColor.UliClay,
    negative = BrandColor.IvieCoral,
    isDark = false
)

val LocalBrandPalette: ProvidableCompositionLocal<BrandPalette> =
    staticCompositionLocalOf { NightPitchPalette }

private val darkScheme = darkColorScheme(
    primary = BrandColor.JaraLime,
    onPrimary = BrandColor.NightPitch,
    primaryContainer = BrandColor.EagleDark,
    onPrimaryContainer = BrandColor.ChalkWhite,
    secondary = BrandColor.LiveGreen,
    // White on Live Green is 3.57:1 — large text only. Night Pitch keeps any
    // stock component that lands on `secondary` legible at label sizes.
    onSecondary = BrandColor.NightPitch,
    tertiary = BrandColor.IfeBrass,
    onTertiary = BrandColor.NightPitch,
    background = BrandColor.NightPitch,
    onBackground = BrandColor.ChalkWhite,
    surface = BrandColor.NightPitch,
    onSurface = BrandColor.ChalkWhite,
    surfaceVariant = BrandColor.NightPitchRaised,
    onSurfaceVariant = BrandColor.HazeDim,
    error = BrandColor.IvieCoralLit,
    onError = BrandColor.NightPitch,
    outline = BrandColor.HarmattanHaze.copy(alpha = 0.45f),
    outlineVariant = BrandColor.HarmattanHaze.copy(alpha = 0.20f)
)

private val chalkScheme = lightColorScheme(
    primary = BrandColor.EagleDark,
    onPrimary = BrandColor.ChalkWhite,
    primaryContainer = BrandColor.EagleDark,
    onPrimaryContainer = BrandColor.ChalkWhite,
    secondary = BrandColor.LiveGreen,
    onSecondary = BrandColor.NightPitch,
    tertiary = BrandColor.UliClay,
    onTertiary = BrandColor.ChalkWhite,
    background = BrandColor.NzuChalk,
    onBackground = BrandColor.NightPitch,
    surface = BrandColor.NzuChalk,
    onSurface = BrandColor.NightPitch,
    surfaceVariant = BrandColor.NzuChalkRaised,
    onSurfaceVariant = BrandColor.ChalkInkDim,
    error = BrandColor.IvieCoral,
    onError = BrandColor.ChalkWhite,
    outline = BrandColor.NightPitch.copy(alpha = 0.28f),
    outlineVariant = BrandColor.NightPitch.copy(alpha = 0.16f)
)

/**
 * Wrap the app in this.
 *
 * There is deliberately no dynamic colour. Material You would repaint the brand
 * in whatever colour the handset's wallpaper happens to be, which for a brand
 * whose entire equity is one specific green is a straight loss.
 */
@Composable
fun NaijaLeagueTheme(
    surface: BrandSurface = BrandSurface.NIGHT_PITCH,
    content: @Composable () -> Unit
) {
    val palette = when (surface) {
        BrandSurface.NIGHT_PITCH -> NightPitchPalette
        BrandSurface.ADIRE_INDIGO -> AdireIndigoPalette
        BrandSurface.NZU_CHALK -> NzuChalkPalette
    }
    CompositionLocalProvider(LocalBrandPalette provides palette) {
        MaterialTheme(
            colorScheme = if (palette.isDark) darkScheme else chalkScheme,
            typography = BrandType.material,
            content = content
        )
    }
}
