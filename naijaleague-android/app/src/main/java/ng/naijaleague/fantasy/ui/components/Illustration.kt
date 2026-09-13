package ng.naijaleague.fantasy.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.LocalBrandPalette

/**
 * The illustration system (§01, §14).
 *
 * WHY IT EXISTS. The reference designs put a photograph behind the hero screens
 * — a player mid-celebration, shirt pulled over the head. Those photographs are
 * of Super Eagles internationals, none of whom play in the NPFL, and this
 * product's entire argument is that it knows the difference. A stock photo of
 * Osimhen on an NPFL app says the same thing an invented player name says.
 *
 * So the backgrounds are drawn rather than shot, from the four motif traditions
 * §01 names, plus the pitch markings themselves. That is not a compromise: a
 * commissioned photo shoot at Lekan Salami would be better than either, and
 * these tiles are what ships until there is one.
 *
 * WHY VECTORS AND NOT BITMAPS. A tile that has to sit on three surfaces at any
 * screen density either ships nine PNGs or ships once as a path. The vectors
 * also reference their colours by name (`@color/uli_clay`, not `#A8552F`), so
 * the palette lives in one place and a motif cannot go stale when a token moves.
 *
 * THE ONE RULE. Adire is a grid and Uli is freehand. They are different
 * disciplines and they fight on the same surface, so [forSurface] never returns
 * both, and no screen should ask for both by hand.
 */
enum class Motif(val drawableRes: Int, val tile: Dp) {
    /** Yoruba resist-dye grid. Structure. */
    ADIRE(R.drawable.il_adire_eleko, 120.dp),

    /** Igbo curvilinear linework. Movement. */
    ULI(R.drawable.il_uli_linework, 160.dp),

    /** Blunt ideographic strokes, the same grammar as the app's icons. */
    NSIBIDI(R.drawable.il_nsibidi_marks, 140.dp),

    /** Northern geometric interlace. Quiet structure. */
    AREWA(R.drawable.il_arewa_lattice, 100.dp),

    /**
     * Pitch markings. Not tiled — placed once, oversized and cropped, because a
     * complete centred circle reads as a logo and a cropped one reads as a pitch.
     */
    PITCH(R.drawable.il_pitch_arcs, 360.dp);

    val tiled: Boolean get() = this != PITCH
}

/**
 * The motif that belongs on a surface.
 *
 * Each ground gets one and keeps it, so a manager moving between tabs is not
 * moving between visual languages. Adire Indigo takes Adire, which is the only
 * pairing in the system that is a pun and is allowed to be.
 */
fun motifFor(surface: BrandSurface): Motif = when (surface) {
    BrandSurface.NIGHT_PITCH -> Motif.NSIBIDI
    BrandSurface.ADIRE_INDIGO -> Motif.ADIRE
    BrandSurface.NZU_CHALK -> Motif.AREWA
}

/**
 * Lay a motif behind whatever is drawn on top.
 *
 * Tiles by drawing the same vector repeatedly rather than by scaling one copy
 * up: a stretched motif loses the line weight the tradition is built on, and
 * line weight is most of what makes Uli read as Uli.
 *
 * The opacity is baked into each vector, not applied here, so a tile is never
 * accidentally drawn at full strength by a caller who forgot.
 */
fun Modifier.motifField(motif: Motif): Modifier = composed {
    val painter = painterResource(motif.drawableRes)
    drawBehind {
        val tile = motif.tile.toPx()
        if (!motif.tiled) {
            // Placed once, anchored bottom-left and allowed to run off the edge.
            translate(left = -tile * 0.15f, top = size.height - tile * 0.9f) {
                with(painter) { draw(Size(tile, tile)) }
            }
            return@drawBehind
        }
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                translate(left = x, top = y) {
                    with(painter) { draw(Size(tile, tile)) }
                }
                x += tile
            }
            y += tile
        }
    }
}

/**
 * A full-bleed brand ground with its motif already on it.
 *
 * The screens use this instead of `background(palette.ground)` so that adding a
 * screen cannot accidentally produce a flat surface that looks like a different
 * app.
 */
@Composable
fun BrandBackdrop(
    surface: BrandSurface,
    modifier: Modifier = Modifier,
    motif: Motif = motifFor(surface),
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalBrandPalette.current
    Box(
        modifier
            .fillMaxSize()
            .drawBehind { drawRect(palette.ground) }
            .motifField(motif),
        content = content
    )
}

/**
 * The hero treatment: the ground, its motif, and the pitch markings over both.
 *
 * For the three screens that carry the product rather than a task — the splash,
 * the first onboarding card, and the end card. Everywhere else this would be
 * noise competing with a score.
 */
@Composable
fun HeroBackdrop(
    surface: BrandSurface,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalBrandPalette.current
    Box(
        modifier
            .fillMaxSize()
            .drawBehind { drawRect(palette.ground) }
            .motifField(motifFor(surface))
            .motifField(Motif.PITCH),
        content = content
    )
}
