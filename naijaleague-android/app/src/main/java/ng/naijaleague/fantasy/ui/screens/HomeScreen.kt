package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.rules.Scoring
import ng.naijaleague.fantasy.rules.Transfers
import ng.naijaleague.fantasy.ui.components.AwayBonusBadge
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandCard
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.ClubBadge
import ng.naijaleague.fantasy.ui.components.OfflineBadge
import ng.naijaleague.fantasy.ui.components.PointsCounter
import ng.naijaleague.fantasy.ui.components.ScreenHeader
import ng.naijaleague.fantasy.ui.components.SectionLabel

/**
 * Home.
 *
 * One dominant figure per screen (§13). The gameweek points total is the hero;
 * the rank sits beside it in the data class, deliberately smaller. If a second
 * number ever competes with the points total, it belongs on another screen.
 */
@Composable
fun HomeScreen(
    onViewTeam: () -> Unit,
    onOpenLive: () -> Unit
) {
    val palette = LocalBrandPalette.current

    // Scored by the same engine the rules page describes — the number on this
    // screen is computed, never stored, so the screen cannot drift from the rules.
    val gameweek = remember {
        Scoring.scoreSquad(SampleData.squad, SampleData.gameweek12, SampleData.activeChip)
    }
    val awayBonus = remember(gameweek) {
        gameweek.playerScores.flatMap { it.lines }
            .filter { it.label.startsWith("Away Day Bonus") }
            .sumOf { it.points }
    }
    val provisional = remember(gameweek) { gameweek.isProvisional(SampleData.gameweek12) }

    Column(Modifier.fillMaxWidth()) {
        LazyColumn(Modifier.fillMaxWidth()) {
            item {
                ScreenHeader(
                    title = "Gameweek ${SampleData.gameweekNumber}",
                    subtitle = "Your squad is locked in. 6 fixtures to come."
                )
            }

            // ---- The hero number ----
            item {
                Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                    SectionLabel("Your points")
                    Spacer(Modifier.height(BrandDimens.SpaceSm))
                    Row(verticalAlignment = Alignment.Bottom) {
                        PointsCounter(points = gameweek.points)
                        Spacer(Modifier.width(BrandDimens.SpaceMd))
                        Column(Modifier.padding(bottom = 10.dp)) {
                            if (awayBonus > 0) AwayBonusBadge(extraPoints = awayBonus)
                            Spacer(Modifier.height(BrandDimens.SpaceXs))
                            Text(
                                "Gameweek average 51",
                                style = BrandType.InterfaceAndGuidance.micro,
                                color = palette.inkDim
                            )
                        }
                    }
                    if (provisional) {
                        Spacer(Modifier.height(BrandDimens.SpaceSm))
                        Text(
                            "Provisional until Monday 12:00. Flag anything wrong.",
                            style = BrandType.InterfaceAndGuidance.micro,
                            color = palette.inkDim
                        )
                    }
                    Spacer(Modifier.height(BrandDimens.SpaceLg))
                }
            }

            // ---- Rank, in the data class, quieter than the points ----
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = BrandDimens.Gutter),
                    horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceMd)
                ) {
                    BrandCard(Modifier.weight(1f)) {
                        Column {
                            SectionLabel("Overall rank", color = palette.inkDim)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                formatThousands(SampleData.overallRank),
                                style = BrandType.ScoreAndData.scorelineMid,
                                color = palette.ink
                            )
                            Text(
                                "of ${formatThousands(SampleData.totalManagers)}",
                                style = BrandType.InterfaceAndGuidance.micro,
                                color = palette.inkDim
                            )
                        }
                    }
                    BrandCard(Modifier.weight(1f)) {
                        Column {
                            SectionLabel("Free transfers", color = palette.inkDim)
                            Spacer(Modifier.height(6.dp))
                            // Ask the engine. Open-coding "banked + 1" would
                            // advertise 6 free transfers at banked = 5, while
                            // Transfers caps at 5 and charges -4 for the sixth.
                            Text(
                                "${Transfers.availableThisGameweek(SampleData.bankedTransfers)}",
                                style = BrandType.ScoreAndData.scorelineMid,
                                color = palette.ink
                            )
                            Text(
                                "free this week · bank up to ${Transfers.MAX_BANKED}",
                                style = BrandType.InterfaceAndGuidance.micro,
                                color = palette.inkDim
                            )
                        }
                    }
                }
                Spacer(Modifier.height(BrandDimens.SpaceLg))
            }

            item {
                Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                    BrandButton(label = "View team", onClick = onViewTeam)
                    Spacer(Modifier.height(BrandDimens.SpaceMd))
                    OfflineBadge()
                    Spacer(Modifier.height(BrandDimens.SpaceXl))
                }
            }

            // ---- Live now ----
            item {
                Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                    SectionLabel("Live now")
                    Spacer(Modifier.height(BrandDimens.SpaceSm))
                    BrandCard(onClick = onOpenLive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .background(BrandColor.LiveGreen, androidx.compose.foundation.shape.CircleShape)
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                // White on Live Green is 3.57:1 — large text only. At
                                // Label size it would fail, so this uses Night Pitch.
                                Text(
                                    "LIVE",
                                    style = BrandType.InterfaceAndGuidance.label,
                                    color = BrandColor.NightPitch
                                )
                            }
                            Spacer(Modifier.width(BrandDimens.SpaceMd))
                            Text(
                                "${SampleData.club(SampleData.liveFixture.homeClubId).shortName} " +
                                    "${SampleData.liveFixture.homeScore}-${SampleData.liveFixture.awayScore} " +
                                    SampleData.club(SampleData.liveFixture.awayClubId).shortName,
                                style = BrandType.ScoreAndData.data,
                                color = palette.ink
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "${SampleData.liveFixture.minute}'",
                                style = BrandType.ScoreAndData.dataSmall,
                                color = palette.accent
                            )
                        }
                    }
                    Spacer(Modifier.height(BrandDimens.SpaceXl))
                }
            }

            // ---- Fixtures. A list, not a stack of cards (§13). ----
            item {
                Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                    SectionLabel("Upcoming fixtures")
                    Spacer(Modifier.height(BrandDimens.SpaceSm))
                }
            }
            items(SampleData.fixtures) { fixture ->
                FixtureRow(
                    home = SampleData.club(fixture.homeClubId).name,
                    homeShort = SampleData.club(fixture.homeClubId).shortName,
                    away = SampleData.club(fixture.awayClubId).name,
                    awayShort = SampleData.club(fixture.awayClubId).shortName,
                    kickoff = fixture.kickoff
                )
            }

            // ---- The one rule nobody else's game has (§03, first session) ----
            item {
                Column(
                    Modifier
                        .padding(horizontal = BrandDimens.Gutter)
                        .padding(top = BrandDimens.SpaceXl, bottom = BrandDimens.SpaceXxl)
                ) {
                    BrandCard {
                        Column {
                            SectionLabel("Away Day Bonus")
                            Spacer(Modifier.height(BrandDimens.SpaceSm))
                            Text(
                                "3 of your players are away on Sunday. Goals away from home pay more.",
                                style = BrandType.InterfaceAndGuidance.body,
                                color = palette.ink
                            )
                            Spacer(Modifier.height(BrandDimens.SpaceSm))
                            Text(
                                "No other fantasy game has this, because no other league needs it.",
                                style = BrandType.InterfaceAndGuidance.bodySmall,
                                color = palette.inkDim
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FixtureRow(
    home: String,
    homeShort: String,
    away: String,
    awayShort: String,
    kickoff: String
) {
    val palette = LocalBrandPalette.current
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClubBadge(homeShort, 28)
            Spacer(Modifier.width(BrandDimens.SpaceSm))
            Text(
                home,
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
                away,
                style = BrandType.IdentityAndEditorial.nameCondensed,
                color = palette.ink,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(BrandDimens.SpaceSm))
            ClubBadge(awayShort, 28)
        }
        Text(
            kickoff,
            style = BrandType.InterfaceAndGuidance.micro,
            color = palette.inkDim,
            modifier = Modifier.padding(start = BrandDimens.Gutter + 36.dp, bottom = BrandDimens.SpaceMd)
        )
        BrandRule(Modifier.padding(horizontal = BrandDimens.Gutter))
    }
}

/** Thousands separators, because a rank of 128432 is unreadable at a glance. */
internal fun formatThousands(value: Int): String =
    value.toString().reversed().chunked(3).joinToString(",").reversed()
