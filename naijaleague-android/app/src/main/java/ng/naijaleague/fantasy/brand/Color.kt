package ng.naijaleague.fantasy.brand

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * NaijaLeague Fantasy palette — brand system v1.1, section 01.
 *
 * Every value here is verbatim from the brand system. Do not invent a shade; if
 * a screen needs a colour that is not in this file, the screen is wrong or the
 * brand system needs a new token.
 *
 * The contrast ratios in the comments are measured against WCAG 2.1, not
 * estimated, and they are the reason some obvious-looking pairings are banned.
 */
object BrandColor {

    // ---------- Core ----------

    /** Eagle Dark. The Super Eagles shirt and the flag's outer bands. */
    val EagleDark = Color(0xFF00583C)

    /** Live Green. "Something is happening right now" — a match in play. */
    val LiveGreen = Color(0xFF009B68)

    /** Jara Lime. Gains, bonuses, the number going up. The one loud voice. */
    val JaraLime = Color(0xFF9FE773)

    /** Chalk White. Type on green, dividers, air. */
    val ChalkWhite = Color(0xFFFFFFFF)

    /** Night Pitch. Not black — a green so deep it reads as black under floodlights. */
    val NightPitch = Color(0xFF04231A)

    /** A lifted Night Pitch, for cards that need to separate from the ground. */
    val NightPitchRaised = Color(0xFF0A3226)

    // ---------- Extended, drawn from the motifs ----------

    /** Adire Indigo. Editorial and story surfaces; away fixtures. */
    val AdireIndigo = Color(0xFF1F2A60)
    val AdireIndigoRaised = Color(0xFF2A3675)

    /** Ife Brass. Honours only — captain, rank 1, Gaffer Pass, trophies. */
    val IfeBrass = Color(0xFFD8A43C)

    /** Ivie Coral. Deductions, cards, the final hour. For light grounds. */
    val IvieCoral = Color(0xFFC9372C)

    /** Ivie Coral, lit for dark grounds. Ivie Coral itself only reaches 3.23:1 on Night Pitch. */
    val IvieCoralLit = Color(0xFFE8604F)

    /**
     * Ivie Coral, lifted again for the Adire Indigo ground.
     *
     * Indigo is lighter than Night Pitch, so the same coral loses contrast
     * against it: IvieCoralLit measures 4.93:1 on Night Pitch but only 3.99:1
     * on Indigo, under the 4.5:1 body floor. §01 already defines two coral
     * steps for two ground luminances; Indigo is a third ground and needs its
     * own. 4.93:1 on Indigo.
     *
     * Found by BrandGuardrailsTest the first time it was able to run.
     */
    val IvieCoralOnIndigo = Color(0xFFF07A66)

    /** Uli Clay. Form and heat data, where red would read as failure. */
    val UliClay = Color(0xFFA8552F)

    /** Nzu Chalk. The reading surface — rules, long-form, receipts. */
    val NzuChalk = Color(0xFFF4EFE6)
    val NzuChalkRaised = Color(0xFFEAE3D6)

    /** Harmattan Haze. A neutral with green in it, so nothing looks unplugged. */
    val HarmattanHaze = Color(0xFFBFC9C2)

    /** Secondary type on the darkest ground. 8.42:1 on Night Pitch. */
    val HazeDim = Color(0xFFA9BDB2)

    /** Ink for chalk surfaces' secondary type. 4.95:1 on Nzu Chalk. */
    val ChalkInkDim = Color(0xFF5A6B62)

    // ---------- Rules that are not optional (§01) ----------

    /**
     * Measured contrast, for anyone tempted to improvise:
     *
     *  White       on EagleDark   8.53:1  any size
     *  JaraLime    on EagleDark   5.75:1  any size — the default points pairing
     *  JaraLime    on NightPitch 11.23:1  the best on the palette; big numbers live here
     *  White       on LiveGreen    3.57:1  LARGE TEXT ONLY (24sp+, or 19sp bold)
     *  EagleDark   on LiveGreen    2.39:1  BANNED — the most tempting pairing we have
     *  IfeBrass    on EagleDark    3.78:1  large only; on NightPitch it is 7.37:1, put brass there
     *  IvieCoral   on NightPitch   3.23:1  use IvieCoralLit (4.93:1) for deduction text
     *  IvieCoralLit on AdireIndigo  3.99:1  under the floor — use IvieCoralOnIndigo (4.93:1)
     *  EagleDark   on NzuChalk     7.45:1  the reading pairing
     *  JaraLime    on NzuChalk     ~1.3:1  NEVER. Lime does not exist on chalk.
     */
    object Contrast {
        const val MIN_BODY = 4.5
        const val MIN_LARGE = 3.0

        /** Large-text threshold in sp, per WCAG: 24sp, or 19sp when bold. */
        const val LARGE_SP = 24
        const val LARGE_SP_BOLD = 19
    }

    /**
     * The primary action colour, resolved per surface.
     *
     * The brand system gives Eagle Dark the "buttons" role, which is right on a
     * chalk ground (white on Eagle Dark, 8.53:1) but disappears on Night Pitch.
     * So: on dark surfaces the primary action is Jara Lime with Night Pitch
     * text (11.23:1), which also puts the loud voice exactly where the brand
     * says it belongs — on the thing the manager is meant to do next.
     */
    fun primaryActionFill(onDarkSurface: Boolean) = if (onDarkSurface) JaraLime else EagleDark
    fun primaryActionLabel(onDarkSurface: Boolean) = if (onDarkSurface) NightPitch else ChalkWhite

    // ---------- contrast, measured the way §01 measures it ----------
    //
    // This lives here rather than in the guard-rail test because production code
    // needs it too: a club kit colour is not from the brand palette, so nothing
    // can be precomputed about what text is legible on it. Two implementations
    // of the same WCAG formula would be two chances to get it wrong, and the one
    // in the test is the one that would stay right.

    private fun channel(c: Float): Float =
        if (c <= 0.04045f) c / 12.92f
        else ((c + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()

    /** WCAG 2.1 relative luminance. */
    fun relativeLuminance(color: Color): Float =
        0.2126f * channel(color.red) + 0.7152f * channel(color.green) + 0.0722f * channel(color.blue)

    /** WCAG 2.1 contrast ratio, 1.0 to 21.0. Order does not matter. */
    fun contrastRatio(a: Color, b: Color): Double {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        return ((max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)).toDouble()
    }

    /**
     * Whichever of the two brand inks is more legible on this background.
     *
     * For colours that are not ours — a club's kit — so a badge can carry a real
     * colour without guessing whether to put white or black on it. Both options
     * are brand tokens; the choice between them is arithmetic.
     */
    fun legibleInkOn(background: Color): Color =
        if (contrastRatio(ChalkWhite, background) >= contrastRatio(NightPitch, background)) {
            ChalkWhite
        } else {
            NightPitch
        }

    /** Does this pairing clear the §01 body-text floor? */
    fun clearsBodyFloor(foreground: Color, background: Color): Boolean =
        contrastRatio(foreground, background) >= 4.5
}
