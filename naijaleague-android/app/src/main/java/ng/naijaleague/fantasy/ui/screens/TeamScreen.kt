package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import ng.naijaleague.fantasy.R
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
import ng.naijaleague.fantasy.ui.components.ClubJersey
import ng.naijaleague.fantasy.ui.components.PlayerCardSheet
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

    // Tapping a token opens that player's card. Held here rather than in a
    // navigation graph for the same reason the root has no nav host: the card
    // is a detail of this screen, not a destination of its own.
    var opened by rememberSaveable { mutableStateOf<String?>(null) }
    var claiming by rememberSaveable { mutableStateOf(false) }
    val openedPlayer = opened?.let { id -> squad.allPlayers.firstOrNull { it.id == id } }
    if (openedPlayer != null) {
        if (claiming) {
            ClaimCardScreen(player = openedPlayer, onClose = { claiming = false })
        } else {
            PlayerCardSheet(
                player = openedPlayer,
                score = scoreById[openedPlayer.id],
                performance = SampleData.gameweek12[openedPlayer.id],
                isCaptain = openedPlayer.id == squad.captainId,
                onClose = { opened = null },
                onClaim = { claiming = true }
            )
        }
        return
    }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        ScreenHeader(
            title = "My team",
            subtitle = "Gameweek ${SampleData.gameweekNumber} · ${squad.formation} · " +
                "${Money.format(squad.budgetRemaining)} in the bank"
        )

        PitchView(squad.startingXi, scoreById, squad.captainId) { opened = it.id }

        // ---- Bench. Only scores under Owambe, and the screen says so. ----
        Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
            Spacer(Modifier.height(BrandDimens.SpaceLg))
            SectionLabel("Bench · scores only with Owambe")
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Row(horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceSm)) {
                squad.bench.forEach { player ->
                    Box(Modifier.weight(1f)) {
                        PlayerPill(
                            player = player,
                            score = scoreById[player.id],
                            isCaptain = false,
                            benched = true,
                            onClick = { opened = player.id }
                        )
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
 * The pitch.
 *
 * TWO THINGS MAKE IT READ AS A PITCH RATHER THAN AS FOUR ROWS OF BUTTONS, and
 * it previously had neither.
 *
 * ONE: A FIXED PROPORTION. The box used to take its height from whatever the
 * rows happened to need, so the markings — all drawn as fractions of the box —
 * stretched with the content. A centre circle on a box of the wrong proportion
 * is an ellipse, and the halfway line lands wherever. A real pitch is 68m by
 * 105m, which is 0.65; that is too tall to fill a phone with four rows of
 * tokens, so this uses [PITCH_RATIO] — the shape a broadcast graphic uses for
 * the same reason, and close enough that the markings below can be set from the
 * actual laws of the game.
 *
 * TWO: ONE TOKEN SIZE. Every row used to stretch its players to fill the width,
 * so a back four drew wider tokens than a front three and the squad looked
 * assembled from different kits. Here the widest line sets the token width and
 * every other line uses it, centred. Five defenders and one keeper are the same
 * object, in the same size, in different numbers — which is what a formation is.
 */
private const val PITCH_RATIO = 0.70f

/**
 * The token is portrait and small, not a square slab.
 *
 * It was as wide as the row could make it and near enough square, which read as
 * a grid of buttons rather than a team sheet. A player on a pitch is a shirt
 * with a name under it — taller than it is wide, and small enough that eleven
 * of them look like a formation instead of a menu. The gap between them went up
 * with the size coming down: crowding was half of why it looked wrong.
 */
private val MaxTokenWidth = 62.dp

/**
 * Every token is the same height as well as the same width.
 *
 * A surname that wraps to two lines would otherwise make one token taller than
 * its neighbours and the line would sit crooked. Reserving the space costs a
 * few dp of pitch and buys a row that lines up.
 */
private val TokenHeight = 74.dp

/** Air between tokens. A team sheet needs the space between the lines. */
private val TokenGap = BrandDimens.SpaceSm

@Composable
private fun PitchView(
    startingXi: List<Player>,
    scores: Map<String, PlayerScore>,
    captainId: String?,
    onOpenPlayer: (Player) -> Unit
) {
    val palette = LocalBrandPalette.current
    val lines = listOf(Position.GK, Position.DEF, Position.MID, Position.FWD)
        .map { position -> startingXi.filter { it.position == position } }
        .filter { it.isNotEmpty() }
    if (lines.isEmpty()) return

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = BrandDimens.SpaceSm)
            .aspectRatio(PITCH_RATIO)
            .clip(RoundedCornerShape(BrandDimens.CardRadius))
            .background(BrandColor.EagleDark)
    ) {
        // The widest line decides the token, so the token never overflows and
        // never changes between rows.
        val widest = lines.maxOf { it.size }
        val gap = TokenGap
        val available = maxWidth - BrandDimens.SpaceMd * 2
        val tokenWidth = ((available - gap * (widest - 1)) / widest).coerceAtMost(MaxTokenWidth)

        Canvas(Modifier.matchParentSize()) {
            val paint = palette.accent.copy(alpha = 0.20f)
            val w = size.width
            val h = size.height
            val stroke = 2f

            // Set from the laws of the game as fractions of a 68m x 105m pitch,
            // so the proportions are the ones an eye already knows.
            val touchInset = w * 0.035f
            val goalInset = h * 0.018f
            val pitchW = w - touchInset * 2
            val pitchH = h - goalInset * 2

            drawRect(
                color = paint, style = Stroke(stroke),
                topLeft = Offset(touchInset, goalInset), size = Size(pitchW, pitchH)
            )
            drawLine(
                paint,
                Offset(touchInset, h / 2f), Offset(w - touchInset, h / 2f), stroke
            )
            // Centre circle: 9.15m radius of a 68m width.
            drawCircle(
                paint, radius = pitchW * (9.15f / 68f),
                center = Offset(w / 2f, h / 2f), style = Stroke(stroke)
            )
            drawCircle(paint, radius = stroke * 1.6f, center = Offset(w / 2f, h / 2f))

            // Penalty area 40.32m x 16.5m; goal area 18.32m x 5.5m.
            val penW = pitchW * (40.32f / 68f)
            val penH = pitchH * (16.5f / 105f)
            val goalW = pitchW * (18.32f / 68f)
            val goalH = pitchH * (5.5f / 105f)
            listOf(true, false).forEach { atTop ->
                val penTop = if (atTop) goalInset else h - goalInset - penH
                drawRect(
                    color = paint, style = Stroke(stroke),
                    topLeft = Offset((w - penW) / 2f, penTop), size = Size(penW, penH)
                )
                val goalTop = if (atTop) goalInset else h - goalInset - goalH
                drawRect(
                    color = paint, style = Stroke(stroke),
                    topLeft = Offset((w - goalW) / 2f, goalTop), size = Size(goalW, goalH)
                )
            }
        }

        Column(
            Modifier
                .matchParentSize()
                .padding(horizontal = BrandDimens.SpaceMd, vertical = BrandDimens.SpaceMd),
            // Spread up the pitch rather than stacked at the top with a gap
            // underneath. A formation is about the distance between the lines.
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            lines.forEach { line ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
                ) {
                    line.forEach { player ->
                        Box(Modifier.width(tokenWidth)) {
                            PlayerPill(
                                player = player,
                                score = scores[player.id],
                                isCaptain = player.id == captainId,
                                benched = false,
                                onClick = { onOpenPlayer(player) }
                            )
                        }
                    }
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
    benched: Boolean,
    onClick: (() -> Unit)? = null
) {
    val palette = LocalBrandPalette.current
    val awayReturn = score?.lines?.any { it.label.startsWith("Away Day Bonus") } == true
    Column(
        Modifier
            .heightIn(min = if (benched) Dp.Unspecified else TokenHeight)
            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
            .background(if (benched) palette.raised else BrandColor.NightPitch.copy(alpha = 0.82f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // The shirt, with the score pinned to its top-right corner.
        //
        // The points used to sit on their own line under the name, which pushed
        // the name and the shirt number apart and made the token three stacked
        // things. Hanging the score off the jersey leaves the line below to read
        // as one object — "Asibe 1", the way a team sheet writes it — and puts
        // the number where the eye already goes on a football graphic.
        if (!benched) {
            Box(contentAlignment = Alignment.TopEnd) {
                ClubJersey(
                    clubId = player.clubId,
                    modifier = Modifier.padding(end = 7.dp, top = 5.dp),
                    sizeDp = 30.dp
                )
                ScoreTick(score = score, captain = isCaptain)
            }
            Spacer(Modifier.height(2.dp))
        }
        if (benched) {
            // The bench has room for a line of its own, and no jersey to hang
            // the score off.
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${score?.finalPoints ?: 0}",
                    style = BrandType.ScoreAndData.dataSmall,
                    color = if ((score?.finalPoints ?: 0) < 0) palette.negative else palette.accent
                )
                if (awayReturn) {
                    Spacer(Modifier.width(3.dp))
                    Text("A", style = BrandType.InterfaceAndGuidance.label, color = palette.accent)
                }
            }
        } else if (awayReturn || score?.differentialTier != DifferentialTier.NONE) {
            // The two things the score alone cannot say: it came from an away
            // ground, and it was multiplied for being a brave pick (§08).
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (awayReturn) {
                    Text("A", style = BrandType.InterfaceAndGuidance.label, color = palette.accent)
                }
                score?.differentialTier
                    ?.takeIf { it != DifferentialTier.NONE }
                    ?.let {
                        Text(
                            "x${it.multiplier}",
                            style = BrandType.InterfaceAndGuidance.label,
                            color = palette.accent
                        )
                    }
            }
        }
    }
}

/**
 * The score, pinned to the corner of the shirt.
 *
 * Small, tabular and hard-edged against the jersey behind it, so it reads as a
 * number attached to a player rather than a caption underneath one. The captain
 * takes brass, because the doubling is an honour the armband earned (§01).
 */
@Composable
private fun ScoreTick(score: PlayerScore?, captain: Boolean) {
    val palette = LocalBrandPalette.current
    val points = score?.finalPoints ?: 0
    val fill = when {
        captain -> BrandColor.IfeBrass
        points < 0 -> palette.negative
        else -> palette.accent
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(fill)
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            "$points",
            style = BrandType.ScoreAndData.dataSmall,
            color = BrandColor.legibleInkOn(fill)
        )
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
