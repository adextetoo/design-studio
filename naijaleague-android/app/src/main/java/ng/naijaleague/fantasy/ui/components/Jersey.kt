package ng.naijaleague.fantasy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.NpflClubs

/**
 * A club shirt, drawn.
 *
 * WHY A JERSEY IS THE RIGHT PLACE FOR THE KIT COLOURS, when a badge was not.
 * The colours came off the badges because fourteen clubs in fourteen colours
 * put a second palette on every list row, competing with the score. On a shirt
 * the colour is not decoration — it IS the thing. A supporter identifies a
 * player by the shirt before they read the name, which is exactly the job this
 * has on a pitch of eleven tokens.
 *
 * WHAT IT DOES WHEN THERE IS NO COLOUR. Six of the twenty clubs have none this
 * app will stand behind — three because no source names one, three because two
 * sources name different ones (see [NpflClubs.KitColours]). Those get an
 * outlined shirt in the surface ink rather than a guess. A blank shirt reads as
 * "we do not know"; a wrong one reads as "we do not know the league".
 *
 * The shape is a shirt and not a crest on purpose. Real club crests are
 * licensed; a shirt silhouette in a club's own stated colours is not a mark.
 */
@Composable
fun ClubJersey(
    clubId: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 28.dp,
    /** Shown across the chest when there is room for it. */
    squadNumber: Int? = null
) {
    val palette = LocalBrandPalette.current
    val kit = NpflClubs.record(clubId).kit
    val sourced = kit.renderable

    val body = if (sourced) Color(kit.primary!!) else Color.Transparent
    val sleeve = if (sourced) {
        kit.secondary?.let { Color(it) } ?: Color(kit.primary!!)
    } else {
        Color.Transparent
    }
    val outline = if (sourced) BrandColor.NightPitch.copy(alpha = 0.45f) else palette.inkDim

    Box(modifier.size(sizeDp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(sizeDp)) {
            val w = size.width
            val h = size.height
            fun p(x: Float, y: Float) = androidx.compose.ui.geometry.Offset(x * w, y * h)

            // The shirt: collar notch, shoulders, sleeves, straight hem.
            val shirt = Path().apply {
                moveTo(p(0.30f, 0.06f).x, p(0.30f, 0.06f).y)
                lineTo(p(0.42f, 0.13f).x, p(0.42f, 0.13f).y)
                quadraticBezierTo(
                    p(0.50f, 0.22f).x, p(0.50f, 0.22f).y,
                    p(0.58f, 0.13f).x, p(0.58f, 0.13f).y
                )
                lineTo(p(0.70f, 0.06f).x, p(0.70f, 0.06f).y)
                lineTo(p(0.88f, 0.15f).x, p(0.88f, 0.15f).y)
                lineTo(p(1.00f, 0.42f).x, p(1.00f, 0.42f).y)
                lineTo(p(0.83f, 0.52f).x, p(0.83f, 0.52f).y)
                lineTo(p(0.79f, 0.44f).x, p(0.79f, 0.44f).y)
                lineTo(p(0.79f, 0.97f).x, p(0.79f, 0.97f).y)
                lineTo(p(0.21f, 0.97f).x, p(0.21f, 0.97f).y)
                lineTo(p(0.21f, 0.44f).x, p(0.21f, 0.44f).y)
                lineTo(p(0.17f, 0.52f).x, p(0.17f, 0.52f).y)
                lineTo(p(0.00f, 0.42f).x, p(0.00f, 0.42f).y)
                lineTo(p(0.12f, 0.15f).x, p(0.12f, 0.15f).y)
                close()
            }
            if (sourced) drawPath(shirt, body)

            // Sleeves in the second colour, so a two-colour kit reads as one.
            if (sourced && sleeve != body) {
                val left = Path().apply {
                    moveTo(p(0.12f, 0.15f).x, p(0.12f, 0.15f).y)
                    lineTo(p(0.21f, 0.44f).x, p(0.21f, 0.44f).y)
                    lineTo(p(0.17f, 0.52f).x, p(0.17f, 0.52f).y)
                    lineTo(p(0.00f, 0.42f).x, p(0.00f, 0.42f).y)
                    close()
                }
                val right = Path().apply {
                    moveTo(p(0.88f, 0.15f).x, p(0.88f, 0.15f).y)
                    lineTo(p(0.79f, 0.44f).x, p(0.79f, 0.44f).y)
                    lineTo(p(0.83f, 0.52f).x, p(0.83f, 0.52f).y)
                    lineTo(p(1.00f, 0.42f).x, p(1.00f, 0.42f).y)
                    close()
                }
                drawPath(left, sleeve)
                drawPath(right, sleeve)
            }

            // Always outlined: a white kit on a pale ground needs an edge, and an
            // unsourced club is nothing BUT its edge.
            drawPath(shirt, outline, style = Stroke(width = w * 0.045f))
        }
        if (squadNumber != null && sizeDp >= 40.dp) {
            Text(
                squadNumber.toString(),
                style = BrandType.ScoreAndData.dataSmall,
                color = if (sourced) BrandColor.legibleInkOn(body) else palette.inkDim
            )
        }
    }
}

/** Does this club have a kit this app will render? Six of twenty do not. */
fun hasRenderableKit(clubId: String): Boolean = NpflClubs.record(clubId).kit.renderable

/** What the sources actually said, for the card that explains a blank shirt. */
fun kitWords(clubId: String): String? = NpflClubs.record(clubId).kit.words

