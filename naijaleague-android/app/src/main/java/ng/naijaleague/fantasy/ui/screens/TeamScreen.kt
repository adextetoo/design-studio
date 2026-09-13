package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import ng.naijaleague.fantasy.R
import androidx.compose.ui.unit.sp
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.rules.Chip
import ng.naijaleague.fantasy.rules.DifferentialTier
import ng.naijaleague.fantasy.rules.Money
import ng.naijaleague.fantasy.rules.Player
import ng.naijaleague.fantasy.rules.PlayerScore
import ng.naijaleague.fantasy.rules.Position
import ng.naijaleague.fantasy.rules.Scoring
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.HonourBadge
import ng.naijaleague.fantasy.ui.components.ScreenHeader
import ng.naijaleague.fantasy.ui.components.SectionLabel

/**
 * Team — the pitch.
 *
 * The formation string is read off the XI rather than stored, so the label can
 * never drift from what is actually on the pitch.
 */
@Composable
fun TeamScreen(onChoosePlayers: () -> Unit) {
    val palette = LocalBrandPalette.current
    val squad = SampleData.squad
    val gameweek = remember {
        Scoring.scoreSquad(squad, SampleData.gameweek12, SampleData.activeChip)
    }
    val scoreById = remember(gameweek) { gameweek.playerScores.associateBy { it.playerId } }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        ScreenHeader(
            title = "My team",
            subtitle = "Gameweek ${SampleData.gameweekNumber} · ${squad.formation} · " +
                "${Money.format(squad.budgetRemaining)} in the bank"
        )

        PitchView(squad.startingXi, scoreById, squad.captainId)

        // ---- Bench. Only scores under Owambe, and the screen says so. ----
        Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
            Spacer(Modifier.height(BrandDimens.SpaceLg))
            SectionLabel("Bench · scores only with Owambe")
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Row(horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceSm)) {
                squad.bench.forEach { player ->
                    Box(Modifier.weight(1f)) {
                        PlayerPill(player, scoreById[player.id], isCaptain = false, benched = true)
                    }
                }
            }
            Spacer(Modifier.height(BrandDimens.SpaceXl))

            // ---- Chips ----
            SectionLabel("Chips · five a season, one per gameweek")
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceSm)
            ) {
                Chip.entries.forEach { chip -> ChipCard(chip) }
            }
            Spacer(Modifier.height(BrandDimens.SpaceXl))

            BrandButton(label = "Choose players", onClick = onChoosePlayers)
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            Text(
                stringResource(R.string.no_buying_points_short),
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )
            Spacer(Modifier.height(BrandDimens.SpaceXxl))
        }
    }
}

/**
 * The pitch. Drawn, not an image: an image would be another megabyte to
 * download and would not recolour with the palette.
 */
@Composable
private fun PitchView(
    startingXi: List<Player>,
    scores: Map<String, PlayerScore>,
    captainId: String?
) {
    val palette = LocalBrandPalette.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = BrandDimens.SpaceSm)
            .clip(RoundedCornerShape(BrandDimens.CardRadius))
            .background(BrandColor.EagleDark)
    ) {
        Canvas(Modifier.matchParentSize()) {
            val line = palette.accent.copy(alpha = 0.18f)
            val w = size.width
            val h = size.height
            val stroke = 2f
            drawRect(
                color = line, style = Stroke(width = stroke),
                topLeft = Offset(w * 0.04f, h * 0.02f),
                size = Size(w * 0.92f, h * 0.96f)
            )
            drawLine(line, Offset(w * 0.04f, h * 0.5f), Offset(w * 0.96f, h * 0.5f), stroke)
            drawCircle(line, radius = w * 0.15f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            // Penalty boxes
            drawRect(
                color = line, style = Stroke(stroke),
                topLeft = Offset(w * 0.28f, h * 0.02f), size = Size(w * 0.44f, h * 0.12f)
            )
            drawRect(
                color = line, style = Stroke(stroke),
                topLeft = Offset(w * 0.28f, h * 0.86f), size = Size(w * 0.44f, h * 0.12f)
            )
        }
        Column(
            Modifier.fillMaxWidth().padding(vertical = BrandDimens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(BrandDimens.SpaceLg)
        ) {
            listOf(Position.GK, Position.DEF, Position.MID, Position.FWD).forEach { position ->
                val line = startingXi.filter { it.position == position }
                if (line.isEmpty()) return@forEach
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = BrandDimens.SpaceSm),
                    horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceXs)
                ) {
                    // A line of one or two players centres instead of stretching:
                    // a lone goalkeeper spread across the full width reads as a
                    // layout bug, not as a formation.
                    val sideWeight = (3 - line.size).coerceAtLeast(0) / 2f
                    if (sideWeight > 0f) Spacer(Modifier.weight(sideWeight))
                    line.forEach { player ->
                        Box(Modifier.weight(1f)) {
                            PlayerPill(
                                player = player,
                                score = scores[player.id],
                                isCaptain = player.id == captainId,
                                benched = false
                            )
                        }
                    }
                    if (sideWeight > 0f) Spacer(Modifier.weight(sideWeight))
                }
            }
        }
    }
}

/**
 * One player on the pitch.
 *
 * Names render through BrandType.IdentityAndEditorial.name, which uses the Noto
 * family — the display face would drop the stacked tone marks in "Ọ̀gbọ́nna"
 * (§02).
 */
@Composable
private fun PlayerPill(
    player: Player,
    score: PlayerScore?,
    isCaptain: Boolean,
    benched: Boolean
) {
    val palette = LocalBrandPalette.current
    val awayReturn = score?.lines?.any { it.label.startsWith("Away Day Bonus") } == true
    Column(
        Modifier
            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
            .background(if (benched) palette.raised else BrandColor.NightPitch.copy(alpha = 0.82f))
            .padding(horizontal = 5.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isCaptain) {
            HonourBadge("C")
            Spacer(Modifier.height(3.dp))
        }
        Text(
            player.name.split(" ").last(),
            style = BrandType.IdentityAndEditorial.name.copy(fontSize = 15.sp),
            color = palette.ink,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${score?.finalPoints ?: 0}",
                style = BrandType.ScoreAndData.dataSmall,
                color = if ((score?.finalPoints ?: 0) < 0) palette.negative else palette.accent
            )
            if (awayReturn) {
                Spacer(Modifier.width(3.dp))
                Text(
                    "A",
                    style = BrandType.InterfaceAndGuidance.label,
                    color = palette.accent
                )
            }
        }
        if (score?.differentialTier != null &&
            score.differentialTier != DifferentialTier.NONE
        ) {
            Text(
                score.differentialTier.multiplier.let { "x$it" },
                style = BrandType.InterfaceAndGuidance.label,
                color = palette.accent
            )
        }
    }
}

@Composable
private fun ChipCard(chip: Chip) {
    val palette = LocalBrandPalette.current
    Column(
        Modifier
            .width(168.dp)
            .clip(RoundedCornerShape(BrandDimens.CardRadius))
            .background(palette.raised)
            .padding(BrandDimens.SpaceMd)
    ) {
        // Class 2, not Class 4. Five celebration treatments sitting permanently
        // on an ordinary screen is exactly how Class 4 stops meaning anything.
        Text(
            chip.chipName.uppercase(),
            style = BrandType.InterfaceAndGuidance.label,
            color = palette.accent
        )
        Spacer(Modifier.height(BrandDimens.SpaceXs))
        Text(
            chip.explanation,
            style = BrandType.InterfaceAndGuidance.body,
            color = palette.inkDim
        )
    }
}
