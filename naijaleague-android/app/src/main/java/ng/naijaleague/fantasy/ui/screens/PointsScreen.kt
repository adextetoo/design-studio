package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.rules.AutoSubs
import ng.naijaleague.fantasy.rules.DifferentialTier
import ng.naijaleague.fantasy.rules.Money
import ng.naijaleague.fantasy.rules.Player
import ng.naijaleague.fantasy.rules.PlayerScore
import ng.naijaleague.fantasy.rules.PointLine
import ng.naijaleague.fantasy.rules.Scoring
import ng.naijaleague.fantasy.rules.Transfers
import ng.naijaleague.fantasy.ui.components.AwayBonusBadge
import ng.naijaleague.fantasy.ui.components.BrandBackdrop
import ng.naijaleague.fantasy.ui.components.BrandCard
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.ClubBadge
import ng.naijaleague.fantasy.ui.components.HonourBadge
import ng.naijaleague.fantasy.ui.components.PointsCounter
import ng.naijaleague.fantasy.ui.components.ScreenHeader
import ng.naijaleague.fantasy.ui.components.SectionLabel
import ng.naijaleague.fantasy.ui.components.StandInTag

private enum class PointsTab(val label: String) {
    POINTS("Points"), TRANSFERS("Transfers"), FIXTURES("Fixtures")
}

/**
 * The prefix [Scoring] gives every Away Day Bonus line.
 *
 * Matched rather than recomputed, so this screen can never disagree with the
 * engine about which points were away points — the one number on here the
 * product is actually arguing about (§05).
 */
private const val AWAY_LINE_PREFIX = "Away Day Bonus"

/**
 * The gameweek that has just been played, as opposed to the one being picked.
 *
 * The rest of the app reads [SampleData.gameweekNumber] as "the round you are
 * choosing for". This screen looks backwards at the same round after the final
 * whistle, which is why every figure on it comes out of the engine rather than
 * out of a stored total.
 */
private val squad = SampleData.squad

/**
 * The transfer that was made before this gameweek's deadline.
 *
 * Demo state standing in for the squad view-model, which is the only thing that
 * knows a manager's transfer history. Both ends are real rows out of
 * [SampleData.pool]: ThankGod Chilaka is a sourced Ikorodu City forward and the
 * stand-in he replaced is labelled as one on the row, because §07's rule is that
 * a stand-in says on its face what it is wherever it is shown.
 */
private val transferIn: Player = SampleData.player("IKO-chilaka")
private val transferOut: Player = SampleData.player("PH-KAT-fwd1")

/** One transfer made against the free one. Priced by the engine, never by hand. */
private const val TRANSFERS_MADE = 1

/**
 * Points & Results.
 *
 * The screen a manager opens on Sunday evening to find out what happened, so it
 * is built to answer three questions in the order they get asked: what did I
 * score, where did it come from, and did my bench come on.
 *
 * EVERY NUMBER IS COMPUTED. The total, each player's points, the away bonus and
 * the transfer cost all come from [Scoring], [AutoSubs] and [Transfers]. Nothing
 * on this screen is a literal, so the screen cannot drift from the rules page
 * the way a stored total would.
 *
 * THE TOTAL IS PROVISIONAL AND SAYS SO. §05's challenge window runs to Monday
 * 12:00, and that window is the trust mechanism that makes thin NPFL data
 * survivable — a manager who cannot see that a number is still open cannot
 * challenge it. So [GameweekScore.isProvisional] gates a plain-English line and
 * the screen names which player is still open, because "provisional" without a
 * subject is a disclaimer rather than information (§12).
 *
 * THE TABLE IS TYPE ON THE GROUND, NOT FIFTEEN CARDS (§13). Eleven rows have to
 * be scannable at 360dp; a card per player would turn one glance into a scroll.
 * Tapping a row opens its itemised [PointLine] list, but the Away Day Bonus
 * lines show whether it is open or not — they are the rule no other fantasy
 * game has, and burying them behind a tap would hide the argument the product
 * is making.
 */
@Composable
fun PointsScreen(onClose: () -> Unit) {
    val palette = LocalBrandPalette.current
    var tab by remember { mutableStateOf(PointsTab.POINTS) }
    var opened by remember { mutableStateOf(emptySet<String>()) }

    val gameweek = remember {
        Scoring.scoreSquad(squad, SampleData.gameweek12, SampleData.activeChip)
    }
    val scoreById = remember(gameweek) { gameweek.playerScores.associateBy { it.playerId } }
    val awayBonus = remember(gameweek) {
        gameweek.playerScores
            .flatMap { it.lines }
            .filter { it.label.startsWith(AWAY_LINE_PREFIX) }
            .sumOf { it.points }
    }
    val provisional = remember(gameweek) { gameweek.isProvisional(SampleData.gameweek12) }
    val stillOpen = remember {
        SampleData.gameweek12.values
            .filter { it.provisional }
            .map { SampleData.player(it.playerId).name }
    }

    // Absences are derived from what the scorers filed, not stored, so a fixture
    // nobody filed a report for reads as postponed rather than as a blank.
    val absences = remember { AutoSubs.absencesFrom(squad, SampleData.gameweek12) }
    val subs = remember(absences) {
        AutoSubs.resolve(squad, absences, activeChip = SampleData.activeChip)
    }

    // Positional order, read off the XI rather than the pick order, so the list
    // reads like a teamsheet: keeper, back line, midfield, front.
    val xi = remember { squad.startingXi.sortedBy { it.position.ordinal } }

    BrandBackdrop(surface = palette.surface) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {

            Row(
                Modifier.fillMaxWidth().padding(horizontal = BrandDimens.SpaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(BrandDimens.MinTapTarget)
                        .clip(CircleShape)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text("‹", style = BrandType.InterfaceAndGuidance.title, color = palette.ink)
                }
            }

            ScreenHeader(
                title = "Gameweek ${SampleData.gameweekNumber}",
                subtitle = "Results, and where every point came from"
            )

            // ---- The hero number. One dominant figure per screen (§13). ----
            Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                SectionLabel("Gameweek total")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Row(verticalAlignment = Alignment.Bottom) {
                    PointsCounter(points = gameweek.points)
                    Spacer(Modifier.width(BrandDimens.SpaceMd))
                    if (awayBonus > 0) {
                        // The second beat: the base total counts up, then the away
                        // bonus lands. §13 calls this the most screenshotted moment
                        // in the product, so it gets its own component and its own
                        // arrival rather than being blended into one figure.
                        Box(Modifier.padding(bottom = 10.dp)) {
                            AwayBonusBadge(extraPoints = awayBonus)
                        }
                    }
                }
                if (provisional) {
                    Spacer(Modifier.height(BrandDimens.SpaceSm))
                    Text(
                        stringResource(R.string.provisional),
                        style = BrandType.InterfaceAndGuidance.micro,
                        color = palette.inkDim
                    )
                    if (stillOpen.isNotEmpty()) {
                        // Named, because a manager cannot flag a number they
                        // cannot identify. Set in the Noto family: this line
                        // carries player names and the display face drops the
                        // marks they are spelled with (§02).
                        Text(
                            "Still open: ${stillOpen.joinToString(", ")}",
                            style = BrandType.IdentityAndEditorial.nameMicro,
                            color = palette.inkDim
                        )
                    }
                }
                Spacer(Modifier.height(BrandDimens.SpaceLg))
            }

            // ---- Tabs. The Leagues pattern, unchanged. ----
            Row(
                Modifier.padding(horizontal = BrandDimens.Gutter),
                horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceSm)
            ) {
                PointsTab.entries.forEach { candidate ->
                    val active = candidate == tab
                    Box(
                        Modifier
                            .heightIn(min = BrandDimens.MinTapTarget)
                            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
                            .background(if (active) palette.accent else palette.raised)
                            .clickable { tab = candidate }
                            .padding(horizontal = BrandDimens.SpaceLg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            candidate.label,
                            style = BrandType.InterfaceAndGuidance.label,
                            color = if (active) palette.accentInk else palette.inkDim
                        )
                    }
                }
            }

            Spacer(Modifier.height(BrandDimens.SpaceLg))

            LazyColumn(Modifier.weight(1f)) {
                when (tab) {
                    PointsTab.POINTS -> {
                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = BrandDimens.Gutter,
                                        vertical = BrandDimens.SpaceSm
                                    )
                            ) {
                                Text(
                                    "PLAYER",
                                    style = BrandType.InterfaceAndGuidance.label,
                                    color = palette.inkDim,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "PTS",
                                    style = BrandType.InterfaceAndGuidance.label,
                                    color = palette.inkDim,
                                    modifier = Modifier.width(44.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                            BrandRule()
                        }

                        items(xi, key = { it.id }) { player ->
                            PlayerPointsRow(
                                player = player,
                                score = scoreById[player.id],
                                isCaptain = player.id == squad.captainId,
                                open = player.id in opened,
                                onToggle = {
                                    opened = if (player.id in opened) opened - player.id
                                    else opened + player.id
                                }
                            )
                        }

                        item { SubstitutionsBlock(subs, absences) }
                        item { AwayBonusNote(awayBonus) }
                        item { TabFooter() }
                    }

                    PointsTab.TRANSFERS -> {
                        item { TransfersTab(returned = scoreById[transferIn.id]?.finalPoints) }
                        item { TabFooter() }
                    }

                    PointsTab.FIXTURES -> {
                        items(SampleData.fixtures, key = { it.homeClubId + it.awayClubId }) {
                            FixtureLine(it)
                        }
                        item { TabFooter() }
                    }
                }
            }
        }
    }
}

/**
 * One player's row, 56dp and dense (§13).
 *
 * The whole row is the tap target, and it is already above
 * [BrandDimens.MinTapTarget] at rest, so opening a breakdown never needs a
 * separate control competing with the name.
 */
@Composable
private fun PlayerPointsRow(
    player: Player,
    score: PlayerScore?,
    isCaptain: Boolean,
    open: Boolean,
    onToggle: () -> Unit
) {
    val palette = LocalBrandPalette.current
    val club = SampleData.club(player.clubId)
    val points = score?.finalPoints ?: 0
    val awayLines = score?.lines.orEmpty().filter { it.label.startsWith(AWAY_LINE_PREFIX) }

    Column(Modifier.clickable(onClick = onToggle)) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = BrandDimens.SquadRowHeight)
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClubBadge(club.shortName, 28, clubId = club.id)
            Spacer(Modifier.width(BrandDimens.SpaceMd))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Noto, always. A player name in the display family loses the
                    // dots and tone marks it is spelled with (§02).
                    Text(
                        player.name,
                        style = BrandType.IdentityAndEditorial.name,
                        color = palette.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isCaptain) {
                        Spacer(Modifier.width(6.dp))
                        // Brass, and the only brass on this screen. The captaincy
                        // is an honour the manager awarded; nothing else here was
                        // won (§01).
                        HonourBadge("C")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (player.isPlaceholder) {
                        StandInTag()
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        "${player.position.short} · ${club.name}",
                        style = BrandType.IdentityAndEditorial.nameMicro,
                        color = palette.inkDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
            Spacer(Modifier.width(BrandDimens.SpaceSm))
            Text(
                points.toString(),
                style = BrandType.ScoreAndData.data,
                color = if (points < 0) palette.negative else palette.accent,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.End
            )
        }

        if (open && score != null) {
            score.lines.forEach { line -> BreakdownLine(line.label, line.points) }
            // The multipliers, in the order the engine applies them:
            // base -> differential -> Ground Man -> round -> captain.
            if (score.differentialTier != DifferentialTier.NONE) {
                BreakdownLine(score.differentialTier.label, null)
            }
            if (score.groundManApplied) BreakdownLine("Ground Man x1.5", null)
            if (score.captainMultiplier > 1) {
                BreakdownLine("Captain x${score.captainMultiplier}", null)
            }
            BreakdownLine("Total", score.finalPoints, strong = true)
            Spacer(Modifier.height(BrandDimens.SpaceSm))
        } else {
            // Closed, but the away lines still show. Everything else about a
            // gameweek is arithmetic every fantasy game does; this is the part
            // only this one does (§05).
            awayLines.forEach { line -> BreakdownLine(line.label, line.points) }
            if (awayLines.isNotEmpty()) Spacer(Modifier.height(BrandDimens.SpaceSm))
        }

        BrandRule(Modifier.padding(start = BrandDimens.Gutter))
    }
}

/**
 * One itemised line under a player.
 *
 * Indented to sit under the name rather than under the crest, so the breakdown
 * reads as belonging to the row above it and not as a new row.
 */
@Composable
private fun BreakdownLine(label: String, points: Int?, strong: Boolean = false) {
    val palette = LocalBrandPalette.current
    val away = label.startsWith(AWAY_LINE_PREFIX)
    val tint = when {
        strong -> palette.ink
        away -> palette.accent
        points != null && points < 0 -> palette.negative
        else -> palette.inkDim
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = BrandDimens.Gutter + 40.dp, end = BrandDimens.Gutter)
            .padding(bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = BrandType.InterfaceAndGuidance.micro,
            color = tint,
            modifier = Modifier.weight(1f)
        )
        Text(
            when {
                points == null -> ""
                points > 0 && !strong -> "+$points"
                else -> points.toString()
            },
            style = BrandType.ScoreAndData.dataSmall,
            color = tint,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Automatic substitutions (§05).
 *
 * The first thing a manager checks after the total, and the one place the NPFL
 * differs hardest from FPL: a postponed fixture brings the bench on here, so
 * [AutoSubs.SubstitutionEvent.reason] has to distinguish "Did not play" from
 * "Match postponed" — the difference matters to the person reading it, and
 * three Matchday 2 fixtures were already postponed this season.
 *
 * The block renders when nothing happened as well, because "did my bench come
 * on" deserves an answer either way, and silence reads as a missing feature.
 */
@Composable
private fun SubstitutionsBlock(
    result: AutoSubs.Result,
    absences: Map<String, AutoSubs.Absence>
) {
    val palette = LocalBrandPalette.current
    val benchAbsentees = absences.keys
        .filter { it !in squad.startingIds }
        .map { SampleData.player(it).name }

    Column(
        Modifier
            .padding(horizontal = BrandDimens.Gutter)
            .padding(top = BrandDimens.SpaceXl)
    ) {
        SectionLabel("Substitutions")
        Spacer(Modifier.height(BrandDimens.SpaceSm))

        if (result.substitutions.isEmpty()) {
            Text(
                "Your eleven all played. Nothing came off the bench.",
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.ink
            )
            if (benchAbsentees.isNotEmpty()) {
                Spacer(Modifier.height(BrandDimens.SpaceXs))
                Text(
                    "On the bench and did not get on: ${benchAbsentees.joinToString(", ")}.",
                    style = BrandType.IdentityAndEditorial.nameMicro,
                    color = palette.inkDim
                )
            }
        } else {
            result.substitutions.forEach { event ->
                SubstitutionRow(event)
            }
        }

        Spacer(Modifier.height(BrandDimens.SpaceMd))
        Text(
            stringResource(R.string.subs_postponed_title),
            style = BrandType.InterfaceAndGuidance.bodySmall,
            color = palette.inkDim
        )
    }
}

@Composable
private fun SubstitutionRow(event: AutoSubs.SubstitutionEvent) {
    val palette = LocalBrandPalette.current
    Column(Modifier.padding(bottom = BrandDimens.SpaceMd)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "OUT",
                style = BrandType.InterfaceAndGuidance.label,
                color = palette.inkDim,
                modifier = Modifier.width(36.dp)
            )
            Text(
                SampleData.player(event.outId).name,
                style = BrandType.IdentityAndEditorial.name,
                color = palette.inkDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "IN",
                style = BrandType.InterfaceAndGuidance.label,
                color = palette.accent,
                modifier = Modifier.width(36.dp)
            )
            Text(
                SampleData.player(event.inId).name,
                style = BrandType.IdentityAndEditorial.name,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // The engine's own words. "Match postponed" is the line §05 promises a
        // manager will see instead of being left to work it out.
        Text(
            event.reason,
            style = BrandType.InterfaceAndGuidance.micro,
            color = palette.inkDim,
            modifier = Modifier.padding(start = 36.dp)
        )
    }
}

/**
 * The Away Day Bonus, totalled and then explained.
 *
 * The figure is the sum of the engine's own away lines, stated before the
 * differential and captain multipliers touch it — which is what it is. Rounding
 * it up into "away points" after multipliers would be a bigger number and a
 * less true one.
 */
@Composable
private fun AwayBonusNote(awayBonus: Int) {
    val palette = LocalBrandPalette.current
    Column(
        Modifier
            .padding(horizontal = BrandDimens.Gutter)
            .padding(top = BrandDimens.SpaceXl)
    ) {
        BrandCard {
            Column {
                SectionLabel(stringResource(R.string.away_bonus_title))
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                if (awayBonus > 0) {
                    Text(
                        "$awayBonus points of it came from away days, before your multipliers.",
                        style = BrandType.InterfaceAndGuidance.body,
                        color = palette.ink
                    )
                    Spacer(Modifier.height(BrandDimens.SpaceSm))
                }
                Text(
                    stringResource(R.string.away_bonus_detail),
                    style = BrandType.InterfaceAndGuidance.bodySmall,
                    color = palette.inkDim
                )
            }
        }
    }
}

/**
 * The transfer made before this gameweek's deadline, and what it cost.
 *
 * Priced by [Transfers] rather than written down. The tense is deliberate: the
 * engine's own confirmation sentence belongs on the confirmation screen, where
 * a manager can still change their mind. Here the gameweek is finished, so the
 * screen states the cost that was charged and what is banked into the next one.
 */
@Composable
private fun TransfersTab(returned: Int?) {
    val palette = LocalBrandPalette.current
    val hit = Transfers.pointsHit(SampleData.bankedTransfers, TRANSFERS_MADE)
    val banked = Transfers.bankAfter(SampleData.bankedTransfers, TRANSFERS_MADE)

    Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
        SectionLabel("Made before the deadline")
        Spacer(Modifier.height(BrandDimens.SpaceMd))

        TransferSide(label = "Out", player = transferOut)
        Spacer(Modifier.height(BrandDimens.SpaceSm))
        TransferSide(label = "In", player = transferIn, emphasis = true)

        Spacer(Modifier.height(BrandDimens.SpaceLg))
        BrandRule()
        Spacer(Modifier.height(BrandDimens.SpaceMd))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "What it cost",
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (hit == 0) "0 points" else "-$hit points",
                style = BrandType.ScoreAndData.data,
                color = if (hit > 0) palette.negative else palette.ink
            )
        }
        Spacer(Modifier.height(BrandDimens.SpaceSm))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Banked into next week",
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim,
                modifier = Modifier.weight(1f)
            )
            Text(
                banked.toString(),
                style = BrandType.ScoreAndData.data,
                color = palette.ink
            )
        }

        if (returned != null) {
            Spacer(Modifier.height(BrandDimens.SpaceLg))
            BrandCard {
                Column {
                    SectionLabel("What it returned")
                    Spacer(Modifier.height(BrandDimens.SpaceSm))
                    Text(
                        "$returned points from the player you brought in.",
                        style = BrandType.InterfaceAndGuidance.body,
                        color = palette.ink
                    )
                }
            }
        }
    }
}

/** One end of a transfer: who, where from, and what they cost in naira. */
@Composable
private fun TransferSide(label: String, player: Player, emphasis: Boolean = false) {
    val palette = LocalBrandPalette.current
    val club = SampleData.club(player.clubId)
    Row(
        Modifier.fillMaxWidth().heightIn(min = BrandDimens.SquadRowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label.uppercase(),
            style = BrandType.InterfaceAndGuidance.label,
            color = if (emphasis) palette.accent else palette.inkDim,
            modifier = Modifier.width(36.dp)
        )
        ClubBadge(club.shortName, 28, clubId = club.id)
        Spacer(Modifier.width(BrandDimens.SpaceMd))
        Column(Modifier.weight(1f)) {
            Text(
                player.name,
                style = BrandType.IdentityAndEditorial.name,
                color = if (emphasis) palette.ink else palette.inkDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (player.isPlaceholder) {
                    StandInTag()
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    "${player.position.short} · ${club.name}",
                    style = BrandType.IdentityAndEditorial.nameMicro,
                    color = palette.inkDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
        Spacer(Modifier.width(BrandDimens.SpaceSm))
        // Naira, formatted by the engine. Money never renders below 15sp (§02).
        Text(
            Money.format(player.priceNaira),
            style = BrandType.ScoreAndData.data,
            color = palette.ink
        )
    }
}

/**
 * A fixture in the round.
 *
 * The venue line is read off [SampleData.Fixture.homeAtNeutralGround] rather
 * than stored on the row, so it cannot drift from the club table. Two clubs
 * spend all of 2026/27 somewhere that is not home, and that changes how the
 * fixture should be read — Ground Man pays for playing at home and does not pay
 * out here — so it goes on the fixture and not in a footnote.
 */
@Composable
private fun FixtureLine(fixture: SampleData.Fixture) {
    val palette = LocalBrandPalette.current
    val home = SampleData.club(fixture.homeClubId)
    val away = SampleData.club(fixture.awayClubId)
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = BrandDimens.SquadRowHeight)
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClubBadge(home.shortName, 28, clubId = home.id)
            Spacer(Modifier.width(BrandDimens.SpaceSm))
            Text(
                home.name,
                style = BrandType.IdentityAndEditorial.nameCondensed,
                color = palette.ink,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            Text(
                "v",
                style = BrandType.InterfaceAndGuidance.micro,
                color = palette.inkDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(20.dp)
            )
            Text(
                away.name,
                style = BrandType.IdentityAndEditorial.nameCondensed,
                color = palette.ink,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(BrandDimens.SpaceSm))
            ClubBadge(away.shortName, 28, clubId = away.id)
        }
        Text(
            fixture.kickoff,
            style = BrandType.InterfaceAndGuidance.micro,
            color = palette.inkDim,
            modifier = Modifier.padding(start = BrandDimens.Gutter + 36.dp, bottom = 2.dp)
        )
        if (fixture.homeAtNeutralGround) {
            Text(
                "Home fixture, played at ${fixture.venueName}",
                style = BrandType.InterfaceAndGuidance.micro,
                color = palette.accent,
                modifier = Modifier.padding(
                    start = BrandDimens.Gutter + 36.dp,
                    bottom = 2.dp
                )
            )
        }
        Spacer(Modifier.height(BrandDimens.SpaceSm))
        BrandRule(Modifier.padding(start = BrandDimens.Gutter))
    }
}

/** Room under the last row, and clear of the system bar. */
@Composable
private fun TabFooter() {
    Spacer(
        Modifier
            .height(BrandDimens.SpaceXxl)
            .navigationBarsPadding()
    )
}
