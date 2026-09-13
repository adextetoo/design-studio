package ng.naijaleague.fantasy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.NpflClubs

/**
 * The deadline strip.
 *
 * Permanent furniture, not a banner (§13). It is the clock the whole product is
 * organised around, so it never scrolls away, and it turns Ivie Coral in the
 * final hour — the only place in the app allowed to go red without something
 * having gone wrong.
 */
@Composable
fun DeadlineStrip(
    deadlineLabel: String,
    hoursRemaining: Int,
    modifier: Modifier = Modifier
) {
    val palette = LocalBrandPalette.current
    val finalHour = hoursRemaining <= 1
    val ink = if (finalHour) palette.negative else palette.inkDim
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BrandDimens.DeadlineStripHeight)
            .background(if (finalHour) palette.negative.copy(alpha = 0.12f) else palette.raised)
            .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(7.dp)) { drawCircle(color = ink) }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.deadline_label).uppercase(),
                style = BrandType.InterfaceAndGuidance.label,
                color = ink
            )
        }
        Text(
            deadlineLabel,
            style = BrandType.ScoreAndData.dataSmall,
            color = if (finalHour) palette.negative else palette.ink
        )
    }
}

enum class Tab(val label: String) {
    HOME("Home"), TEAM("Team"), LEAGUES("Leagues"), RULES("Rules")
}

/**
 * The bottom bar.
 *
 * Thumb gravity (§13): everything important sits where the hand is. The icons
 * are drawn here rather than imported, following Nsibidi's grammar — one blunt
 * stroke, one meaning, no outlines, no gradients (§13).
 */
@Composable
fun BrandBottomBar(
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalBrandPalette.current
    Column(modifier.fillMaxWidth().background(palette.ground)) {
        BrandRule()
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = BrandDimens.SpaceSm),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Tab.entries.forEach { tab ->
                val active = tab == selected
                val tint = if (active) palette.accent else palette.inkDim
                Column(
                    Modifier
                        .weight(1f)
                        .heightIn(min = BrandDimens.MinTapTarget)
                        .clickable { onSelect(tab) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TabGlyph(tab, tint)
                    Spacer(Modifier.height(6.dp))
                    Text(tab.label, style = BrandType.InterfaceAndGuidance.label, color = tint)
                }
            }
        }
    }
}

@Composable
private fun TabGlyph(tab: Tab, tint: Color) {
    Canvas(Modifier.size(22.dp)) {
        val s = size.width
        val w = s * 0.13f
        when (tab) {
            // A gate: the way into the gameweek.
            Tab.HOME -> {
                drawLine(tint, Offset(s * 0.5f, s * 0.08f), Offset(s * 0.08f, s * 0.42f), w, StrokeCap.Square)
                drawLine(tint, Offset(s * 0.5f, s * 0.08f), Offset(s * 0.92f, s * 0.42f), w, StrokeCap.Square)
                drawLine(tint, Offset(s * 0.16f, s * 0.42f), Offset(s * 0.16f, s * 0.92f), w, StrokeCap.Square)
                drawLine(tint, Offset(s * 0.84f, s * 0.42f), Offset(s * 0.84f, s * 0.92f), w, StrokeCap.Square)
                drawLine(tint, Offset(s * 0.16f, s * 0.92f), Offset(s * 0.84f, s * 0.92f), w, StrokeCap.Square)
            }
            // A formation, which is what a team actually is here.
            Tab.TEAM -> {
                val r = s * 0.11f
                drawCircle(tint, r, Offset(s * 0.5f, s * 0.14f))
                drawCircle(tint, r, Offset(s * 0.16f, s * 0.5f))
                drawCircle(tint, r, Offset(s * 0.5f, s * 0.5f))
                drawCircle(tint, r, Offset(s * 0.84f, s * 0.5f))
                drawCircle(tint, r, Offset(s * 0.32f, s * 0.86f))
                drawCircle(tint, r, Offset(s * 0.68f, s * 0.86f))
            }
            // A table, ranked.
            Tab.LEAGUES -> {
                drawLine(tint, Offset(s * 0.1f, s * 0.2f), Offset(s * 0.9f, s * 0.2f), w, StrokeCap.Square)
                drawLine(tint, Offset(s * 0.1f, s * 0.5f), Offset(s * 0.66f, s * 0.5f), w, StrokeCap.Square)
                drawLine(tint, Offset(s * 0.1f, s * 0.8f), Offset(s * 0.42f, s * 0.8f), w, StrokeCap.Square)
            }
            // Straight from the Nsibidi set in the brand system's motif system.
            Tab.RULES -> {
                drawCircle(tint, s * 0.38f, Offset(s * 0.5f, s * 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = w))
                drawLine(tint, Offset(s * 0.5f, s * 0.08f), Offset(s * 0.5f, s * 0.92f), w, StrokeCap.Square)
            }
        }
    }
}

/**
 * A screen header. Identity & Editorial class, with the gameweek in the data
 * class beneath it — never the other way round.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val palette = LocalBrandPalette.current
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = BrandDimens.Gutter)
            .padding(top = BrandDimens.SpaceLg, bottom = BrandDimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = BrandType.IdentityAndEditorial.display2,
                color = palette.ink
            )
            if (subtitle != null) {
                Spacer(Modifier.height(BrandDimens.SpaceXs))
                Text(
                    subtitle,
                    style = BrandType.ScoreAndData.dataSmall,
                    color = palette.inkDim
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(BrandDimens.SpaceMd))
            trailing()
        }
    }
}

/**
 * A club crest stand-in. Real crests are licensed; this holds their place.
 *
 * CARRIES THE CLUB'S OWN COLOUR WHERE ONE IS SOURCED. The app used to draw every
 * club in the same Eagle Dark, which is consistent and legible and not the
 * clubs' colours — and a Nigerian supporter sees that immediately. So a badge
 * given a [clubId] fills with the kit colour when [NpflClubs] has one it will
 * stand behind, and falls back to Eagle Dark when it does not.
 *
 * THE FALLBACK COVERS TWO DIFFERENT CASES AND TREATS THEM THE SAME, correctly:
 * no source found (Barau, Doma United, Inter Lagos) and two sources naming
 * different colours (Rangers, Enyimba, Kano Pillars). Either way the honest
 * answer is a neutral mark rather than a confident guess.
 *
 * THE LABEL COLOUR IS ARITHMETIC, NOT A GUESS. A kit colour is not from the
 * brand palette, so nothing about it can be precomputed — white on Ikorodu
 * City's white would vanish. [BrandColor.legibleInkOn] picks whichever brand ink
 * has more contrast on the actual fill.
 */
@Composable
fun ClubBadge(
    shortName: String,
    sizeDp: Int = 32,
    modifier: Modifier = Modifier,
    /** When given, the badge uses this club's sourced kit colour if there is one. */
    clubId: String? = null
) {
    val palette = LocalBrandPalette.current
    val kit = clubId?.let { NpflClubs.record(it).kit }
    val fill = if (kit?.renderable == true) Color(kit.primary!!) else BrandColor.EagleDark
    val label = if (kit?.renderable == true) {
        BrandColor.legibleInkOn(fill)
    } else {
        palette.accent
    }
    Box(
        modifier
            .size(sizeDp.dp)
            .background(fill, androidx.compose.foundation.shape.CircleShape)
            // A white or pale kit needs an edge or it dissolves into a chalk ground.
            .border(1.dp, palette.rule, androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            shortName.take(3),
            style = BrandType.InterfaceAndGuidance.label.copy(fontFamily = BrandType.NigerianText),
            color = label
        )
    }
}
