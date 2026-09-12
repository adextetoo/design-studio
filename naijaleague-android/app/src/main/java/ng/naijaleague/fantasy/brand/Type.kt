package ng.naijaleague.fantasy.brand

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ng.naijaleague.fantasy.R

/**
 * Typography — brand system v1.1, section 02.
 *
 * The licensed pairing is Söhne (interface and data) with Druk Condensed and
 * Druk Wide (display). Neither is redistributable, so this ships the brand
 * system's own named open-source fallbacks — Inter and Archivo Expanded — as
 * bundled variable fonts, with the width and weight axes pinned in XML
 * (the res/font XML resources) to the exact instances the brand system specifies.
 *
 * Bundled rather than downloaded on purpose. It removes the Play Services
 * dependency, and it means type renders correctly with no network — which is
 * the same principle as the rest of the product: it works when your data is
 * finished.
 *
 * TO INSTALL THE LICENSED FACES:
 *   1. Drop the .otf files into app/src/main/res/font/ as sohne_buch.otf,
 *      sohne_kraftig.otf, sohne_halbfett.otf, druk_wide_bold.otf,
 *      druk_condensed_super.otf.
 *   2. Point the families below at them and delete the variation XML.
 *
 * THE CONSTRAINT THAT DECIDES EVERYTHING (§02): our players are called
 * Ọláwale, Chiọma, Ṣàńgó, Aliyu Ƙasim. Yoruba needs ẹ ọ ṣ with under-dots AND
 * stacked tone marks; Igbo needs ị ọ ụ ṅ; Hausa needs ɓ ɗ ƙ ƴ. Druk covers none
 * of them; Archivo covers only part. Until the 40-glyph commission lands, any
 * name or Nigerian-language string renders through [NigerianText], which
 * resolves to the platform's Noto and covers the full set. Player names must
 * never be set in the display family — they will lose their diacritics.
 */
object BrandType {

    /** Class 2 and 4 — Druk stand-in. Axes pinned in res/font/display_*.xml. */
    val Display: FontFamily = FontFamily(
        Font(R.font.display_medium, FontWeight.Medium, FontStyle.Normal),
        Font(R.font.display_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
        Font(R.font.display_black, FontWeight.Black, FontStyle.Normal)
    )

    /**
     * Condensed display, for long EDITORIAL strings only — never a name. It has
     * no glyphs for the Nigerian marks, which is the whole point of §02.
     */
    val DisplayCondensed: FontFamily = FontFamily(
        Font(R.font.display_condensed, FontWeight.Bold, FontStyle.Normal)
    )

    /** Classes 1 and 3 — Söhne stand-in. */
    val Interface: FontFamily = FontFamily(
        Font(R.font.ui_regular, FontWeight.Normal, FontStyle.Normal),
        Font(R.font.ui_medium, FontWeight.Medium, FontStyle.Normal),
        Font(R.font.ui_semibold, FontWeight.SemiBold, FontStyle.Normal),
        Font(R.font.ui_bold, FontWeight.Bold, FontStyle.Normal)
    )

    /**
     * Any string that can carry Yoruba, Igbo or Hausa marks — which in this app
     * means every player name and every club name.
     *
     * This is a BUNDLED subset of Noto Sans, not FontFamily.Default. The
     * difference matters: FontFamily.Default resolves to whatever the OEM ships
     * as its system sans — SamsungOne, MiSans, OnePlus Sans on exactly the
     * mid-range handsets this product targets — so the stacked mark-to-mark
     * positioning in "Ọ̀gbọ́nna" would be at the mercy of an unaudited font. The
     * bundled subset carries the full Yoruba/Igbo/Hausa glyph set plus the GPOS
     * and GDEF tables that position a tone mark over a dotted vowel.
     */
    val NigerianText: FontFamily = FontFamily(
        Font(R.font.noto_regular, FontWeight.Normal, FontStyle.Normal),
        Font(R.font.noto_semibold, FontWeight.SemiBold, FontStyle.Normal)
    )

    /**
     * The same family narrowed on Noto's own wdth axis, for long club names.
     * Condensing this way rather than switching to the display face is what
     * keeps "Ọ̀gbọ́nna" intact while still fitting "Bendel Insurance" on a card.
     */
    val NigerianTextCondensed: FontFamily = FontFamily(
        Font(R.font.noto_condensed, FontWeight.SemiBold, FontStyle.Normal)
    )

    // ---------- CLASS 1 — SCORE & DATA ----------
    /**
     * Anywhere a number can change. Tabular figures always, so rank 11 and
     * rank 88 occupy the same width and the column does not shiver when it
     * updates. Numbers never fade in — they count up (§13).
     */
    object ScoreAndData {
        /** Gameweek points. One per screen, never two. */
        val scoreline = TextStyle(
            fontFamily = Display, fontWeight = FontWeight.Black,
            fontSize = 64.sp, lineHeight = 58.sp, letterSpacing = (-0.01).em
        )
        /** The secondary figure — rank, transfers, a scoreline. Display 2's size. */
        val scorelineMid = TextStyle(
            fontFamily = Display, fontWeight = FontWeight.Black,
            fontSize = 28.sp, lineHeight = 30.sp, letterSpacing = (-0.01).em
        )
        val data = TextStyle(
            fontFamily = Interface, fontWeight = FontWeight.Medium,
            fontSize = 15.sp, lineHeight = 16.sp, letterSpacing = 0.01.em
        )
        /**
         * Deliberately the same size as [data]. There is no 13sp level on the
         * scale, and the sites that used one were carrying money, the deadline
         * and the two-per-club cap — all of which §02 keeps at 15sp or above.
         */
        val dataSmall = TextStyle(
            fontFamily = Interface, fontWeight = FontWeight.Medium,
            fontSize = 15.sp, lineHeight = 18.sp, letterSpacing = 0.01.em
        )
    }

    // ---------- CLASS 2 — IDENTITY & EDITORIAL ----------
    /** Club names, gameweek titles, chip names. This class carries the swagger. */
    object IdentityAndEditorial {
        val display1 = TextStyle(
            fontFamily = Display, fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp, lineHeight = 38.sp, letterSpacing = (-0.02).em
        )
        val display2 = TextStyle(
            fontFamily = Display, fontWeight = FontWeight.Medium,
            fontSize = 28.sp, lineHeight = 30.sp, letterSpacing = (-0.015).em
        )
        /** Club and player names: Noto family, so the diacritics survive. */
        val name = TextStyle(
            fontFamily = NigerianText, fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = (-0.005).em
        )
        /** Club names in small print — still Noto, so the marks survive. */
        val nameMicro = TextStyle(
            fontFamily = NigerianText, fontWeight = FontWeight.Normal,
            fontSize = 12.sp, lineHeight = 16.sp
        )

        /** Long club names on a tight row. Still Noto — just narrower. */
        val nameCondensed = TextStyle(
            fontFamily = NigerianTextCondensed, fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp, lineHeight = 20.sp
        )
    }

    // ---------- CLASS 3 — INTERFACE & GUIDANCE ----------
    /**
     * Buttons, forms, rules, errors, settings. Never uppercase beyond 14sp.
     * This class is never funny: a manager who just lost a squad to a bad
     * connection wants instructions, not banter.
     */
    object InterfaceAndGuidance {
        val title = TextStyle(
            fontFamily = Interface, fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.01).em
        )
        val body = TextStyle(
            fontFamily = Interface, fontWeight = FontWeight.Normal,
            fontSize = 16.sp, lineHeight = 24.sp
        )
        /** Secondary information only. Never rules, never money (§02 v1.1). */
        val bodySmall = TextStyle(
            fontFamily = Interface, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.005.em
        )
        val label = TextStyle(
            fontFamily = Interface, fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp, lineHeight = 12.sp, letterSpacing = 0.09.em
        )
        val micro = TextStyle(
            fontFamily = Interface, fontWeight = FontWeight.Normal,
            fontSize = 12.sp, lineHeight = 16.sp
        )
        val button = TextStyle(
            fontFamily = Interface, fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = 0.01.em
        )
    }

    // ---------- CLASS 4 — CELEBRATION & MOTION ----------
    /**
     * Exactly four moments: chip activation, a personal-best gameweek, winning
     * a mini-league, and the receipt card. Its scarcity is the point.
     */
    object CelebrationAndMotion {
        /**
         * The signwriter treatment: heavy caps with a Jara Lime offset shadow
         * standing in for the painted outline. Rotation is applied at the call
         * site with Modifier.rotate(-1f); it cannot live in a TextStyle.
         */
        val celebration = TextStyle(
            fontFamily = Display, fontWeight = FontWeight.Black,
            fontSize = 40.sp, lineHeight = 38.sp, letterSpacing = (-0.01).em,
            shadow = Shadow(
                color = BrandColor.JaraLime,
                offset = Offset(3f, 3f),
                blurRadius = 0f
            )
        )
        val celebrationSub = TextStyle(
            fontFamily = Display, fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = 0.04.em
        )
    }

    /** Material 3 mapping, so stock components inherit the right class. */
    val material = Typography(
        displayLarge = ScoreAndData.scoreline,
        displayMedium = IdentityAndEditorial.display1,
        displaySmall = IdentityAndEditorial.display2,
        headlineLarge = IdentityAndEditorial.display1,
        headlineMedium = IdentityAndEditorial.display2,
        headlineSmall = InterfaceAndGuidance.title,
        titleLarge = InterfaceAndGuidance.title,
        titleMedium = IdentityAndEditorial.name,
        titleSmall = InterfaceAndGuidance.label,
        bodyLarge = InterfaceAndGuidance.body,
        bodyMedium = InterfaceAndGuidance.bodySmall,
        bodySmall = InterfaceAndGuidance.micro,
        labelLarge = InterfaceAndGuidance.button,
        labelMedium = InterfaceAndGuidance.label,
        labelSmall = InterfaceAndGuidance.label
    )
}
