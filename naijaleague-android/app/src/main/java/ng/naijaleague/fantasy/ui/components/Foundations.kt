package ng.naijaleague.fantasy.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.LocalAnimationsEnabled
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandMotion
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette

/**
 * Shared components. Every one of these takes its colours from
 * LocalBrandPalette rather than a raw hex, which is what stops the banned
 * pairings in BrandColor.Contrast happening by accident (§01).
 */

/** Uppercase eyebrow. Label class, 11sp, +0.09em — never larger (§02). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color? = null) {
    val palette = LocalBrandPalette.current
    Text(
        text = text.uppercase(),
        style = BrandType.InterfaceAndGuidance.label,
        color = color ?: palette.accent,
        modifier = modifier
    )
}

/**
 * The primary action.
 *
 * Fill and label resolve per surface: Jara Lime on Night Pitch text where the
 * ground is dark (11.23:1), Eagle Dark on white where it is chalk (8.53:1).
 * Never white-on-Live-Green at this size — that pairing is 3.57:1 and is large
 * text only (§01).
 */
@Composable
fun BrandButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val palette = LocalBrandPalette.current
    val fill = if (enabled) palette.actionFill else BrandColor.HarmattanHaze.copy(alpha = 0.25f)
    val ink = if (enabled) palette.actionLabel else palette.inkDim
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BrandDimens.ButtonHeight)
            .clip(RoundedCornerShape(BrandDimens.ButtonRadius))
            .background(fill)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = BrandType.InterfaceAndGuidance.button, color = ink)
    }
}

/** The quieter second action. Outline, never a second loud fill. */
@Composable
fun BrandButtonSecondary(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalBrandPalette.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BrandDimens.ButtonHeight)
            .clip(RoundedCornerShape(BrandDimens.ButtonRadius))
            .border(BorderStroke(1.dp, palette.accent.copy(alpha = 0.55f)), RoundedCornerShape(BrandDimens.ButtonRadius))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = BrandType.InterfaceAndGuidance.button, color = palette.ink)
    }
}

/**
 * A points total.
 *
 * Points count, they never fade — a number arriving is the emotional payload of
 * this entire product (§13). When the handset has animation switched off the
 * number simply appears, which is the same rule stated the other way round.
 */
@Composable
fun PointsCounter(
    points: Int,
    modifier: Modifier = Modifier,
    large: Boolean = true
) {
    val palette = LocalBrandPalette.current
    val animate = LocalAnimationsEnabled.current
    val shown by animateIntAsState(
        targetValue = points,
        animationSpec = if (animate) {
            androidx.compose.animation.core.tween(
                durationMillis = BrandMotion.COUNT_UP_MS,
                easing = BrandMotion.UliCurve
            )
        } else {
            androidx.compose.animation.core.snap()
        },
        label = "points"
    )
    Text(
        text = shown.toString(),
        style = if (large) BrandType.ScoreAndData.scoreline else BrandType.ScoreAndData.scorelineMid,
        // Lime is "the number going up". A negative gameweek is not that.
        color = if (points < 0) palette.negative else palette.accent,
        modifier = modifier
    )
}

/**
 * The Away Day Bonus badge.
 *
 * Lands as a separate second beat after the base points so the manager feels
 * the extra (§13). This is the most screenshotted moment we have, so it is a
 * component and not an afterthought.
 */
@Composable
fun AwayBonusBadge(extraPoints: Int, modifier: Modifier = Modifier) {
    val palette = LocalBrandPalette.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
            .background(palette.accent)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "AWAY +$extraPoints",
            style = BrandType.InterfaceAndGuidance.label,
            color = palette.accentInk
        )
    }
}

/** Honours only — captain, rank one, Gaffer Pass. Brass never appears elsewhere (§01). */
@Composable
fun HonourBadge(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
            .background(BrandColor.IfeBrass)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(text, style = BrandType.InterfaceAndGuidance.label, color = BrandColor.NightPitch)
    }
}

/**
 * Offline is a visible state, not a failure (§13). Calm, small, and it never
 * suggests the manager has lost their work.
 */
@Composable
fun OfflineBadge(modifier: Modifier = Modifier) {
    val palette = LocalBrandPalette.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
            .background(palette.raised)
            .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.ChipRadius))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(8.dp)) {
            // Haze, not brass. Brass means something was won; this is a sync state.
            drawCircle(color = BrandColor.HarmattanHaze)
        }
        Spacer(Modifier.width(6.dp))
        Text(
            "Saved on your phone · will sync",
            style = BrandType.InterfaceAndGuidance.micro,
            color = palette.inkDim
        )
    }
}

/**
 * A card. Border, fill and radius each say "a separate object you can act on" —
 * a player, a fixture, a league. Tables and rules pages are type on the ground,
 * not cards (§13). Do not wrap a list row in this.
 */
@Composable
fun BrandCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val palette = LocalBrandPalette.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(BrandDimens.CardRadius))
            .background(palette.raised)
            .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.CardRadius))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(BrandDimens.SpaceLg)
    ) { content() }
}

/** A horizontal rule. One hairline, not a card edge. */
@Composable
fun BrandRule(modifier: Modifier = Modifier) {
    val palette = LocalBrandPalette.current
    Box(modifier.fillMaxWidth().height(1.dp).background(palette.rule))
}

/**
 * The endline lockup (§summary, §04).
 *
 * Locked under the mark on advertising, the store listing and share cards —
 * but NOT in onboarding. §12 is explicit that "real fans" is an invitation and
 * never a test, and must never appear in onboarding, in an empty state, or
 * aimed at a user. Screen one of a signup flow is exactly the door-with-a-
 * bouncer this rule exists to prevent, so this composable is reserved for the
 * About screen and the receipt/share card.
 */
@Composable
fun EndlineLockup(modifier: Modifier = Modifier) {
    val palette = LocalBrandPalette.current
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "NaijaLeague Fantasy",
            style = BrandType.IdentityAndEditorial.display2.copy(fontWeight = FontWeight.ExtraBold),
            color = palette.ink,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(BrandDimens.SpaceXs))
        Text(
            "Fantasy football for real NPFL fans",
            style = BrandType.InterfaceAndGuidance.label,
            color = palette.accent,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * The brand mark: the lime tick on Eagle Dark, drawn rather than imported so it
 * stays sharp at any size and matches the launcher icon exactly (§03).
 */
@Composable
fun BrandMark(sizeDp: Int = 44, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape((sizeDp * 0.23f).dp))
            .background(BrandColor.EagleDark),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size((sizeDp * 0.62f).dp)) {
            val w = size.width
            val stroke = w * 0.235f
            drawLine(
                color = BrandColor.JaraLime,
                start = Offset(w * 0.04f, w * 0.55f),
                end = Offset(w * 0.38f, w * 0.87f),
                strokeWidth = stroke,
                cap = StrokeCap.Square
            )
            drawLine(
                color = BrandColor.JaraLime,
                start = Offset(w * 0.38f, w * 0.87f),
                end = Offset(w * 0.98f, w * 0.10f),
                strokeWidth = stroke,
                cap = StrokeCap.Square
            )
        }
    }
}

/**
 * A stat comparison bar, for the live match screen.
 * Home takes the accent, away takes Uli Clay — warm, human, non-alarming, so it
 * carries data where red would read as failure (§01).
 */
@Composable
fun StatBar(label: String, home: Int, away: Int, modifier: Modifier = Modifier) {
    val palette = LocalBrandPalette.current
    val total = (home + away).coerceAtLeast(1)
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(home.toString(), style = BrandType.ScoreAndData.data, color = palette.ink)
            Text(label, style = BrandType.InterfaceAndGuidance.micro, color = palette.inkDim)
            Text(away.toString(), style = BrandType.ScoreAndData.data, color = palette.ink)
        }
        Spacer(Modifier.height(6.dp))
        // RowScope.weight requires a value greater than zero, so a genuine
        // 0-3 stat line would throw during composition. Clamping also keeps a
        // zero side visible as a sliver rather than vanishing, which reads
        // better than an empty track.
        val homeFraction = (home.toFloat() / total).coerceIn(0.02f, 0.98f)
        Row(Modifier.fillMaxWidth().height(5.dp)) {
            Box(
                Modifier
                    .weight(homeFraction)
                    .height(5.dp)
                    .background(palette.accent)
            )
            Spacer(Modifier.width(3.dp))
            Box(
                Modifier
                    .weight(1f - homeFraction)
                    .height(5.dp)
                    .background(BrandColor.UliClay)
            )
        }
    }
}
