package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.ui.components.BrandButtonSecondary
import ng.naijaleague.fantasy.ui.components.BrandCard
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.HonourBadge
import ng.naijaleague.fantasy.ui.components.ScreenHeader
import ng.naijaleague.fantasy.ui.components.SectionLabel

private enum class LeagueTab(val label: String) {
    MINI("My league"), OVERALL("Overall"), CLUB("My club")
}

/**
 * Leagues.
 *
 * The table is type on the ground, not a stack of cards (§13) — a league table
 * is a table, and this audience reads them for pleasure. Rank 1 is the only row
 * allowed brass, because brass means something was won (§01).
 */
@Composable
fun LeaguesScreen(onOpenGafferPass: () -> Unit) {
    val palette = LocalBrandPalette.current
    var tab by remember { mutableStateOf(LeagueTab.MINI) }

    Column(Modifier.fillMaxWidth()) {
        ScreenHeader(
            title = "Leagues",
            subtitle = "Gameweek ${SampleData.gameweekNumber} · 8 managers"
        )

        Row(
            Modifier.padding(horizontal = BrandDimens.Gutter),
            horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceSm)
        ) {
            LeagueTab.entries.forEach { candidate ->
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

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceSm)
        ) {
            Text("#", style = BrandType.InterfaceAndGuidance.label, color = palette.inkDim,
                modifier = Modifier.width(28.dp))
            Text("SQUAD", style = BrandType.InterfaceAndGuidance.label, color = palette.inkDim,
                modifier = Modifier.weight(1f))
            Text("GW", style = BrandType.InterfaceAndGuidance.label, color = palette.inkDim,
                modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
            Text("TOTAL", style = BrandType.InterfaceAndGuidance.label, color = palette.inkDim,
                modifier = Modifier.width(56.dp), textAlign = TextAlign.End)
        }
        BrandRule()

        LazyColumn(Modifier.weight(1f)) {
            items(SampleData.miniLeague, key = { it.rank }) { entry ->
                LeagueRow(entry)
            }

            item {
                Column(
                    Modifier
                        .padding(BrandDimens.Gutter)
                        .padding(top = BrandDimens.SpaceLg)
                ) {
                    // The empty-state line from §12, used as the invitation it is.
                    // Gaffer Pass sits here rather than interrupting the game:
                    // it is offered next to the thing it helps with, and never
                    // as a modal over a squad someone is in the middle of.
                    BrandCard(onClick = onOpenGafferPass) {
                        Column {
                            SectionLabel("Gaffer Pass", color = BrandColor.IfeBrass)
                            Spacer(Modifier.height(BrandDimens.SpaceSm))
                            Text(
                                "Better information. Not better odds.",
                                style = BrandType.InterfaceAndGuidance.title,
                                color = palette.ink
                            )
                            Spacer(Modifier.height(BrandDimens.SpaceXs))
                            Text(
                                "The Differential Board, team news by SMS, and the Monday " +
                                    "voice note. From ₦400 a week.",
                                style = BrandType.InterfaceAndGuidance.body,
                                color = palette.inkDim
                            )
                        }
                    }
                    Spacer(Modifier.height(BrandDimens.SpaceLg))
                    BrandCard {
                        Column {
                            SectionLabel("Add your people")
                            Spacer(Modifier.height(BrandDimens.SpaceSm))
                            Text(
                                "A league with one person inside is just a spreadsheet.",
                                style = BrandType.InterfaceAndGuidance.body,
                                color = palette.ink
                            )
                            Spacer(Modifier.height(BrandDimens.SpaceMd))
                            BrandButtonSecondary(
                                label = "Share invite to WhatsApp",
                                onClick = {}
                            )
                        }
                    }
                    Spacer(Modifier.height(BrandDimens.SpaceXxl))
                }
            }
        }
    }
}

@Composable
private fun LeagueRow(entry: SampleData.LeagueEntry) {
    val palette = LocalBrandPalette.current
    val isLeader = entry.rank == 1
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = BrandDimens.SquadRowHeight)
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.width(28.dp)) {
                if (isLeader) {
                    HonourBadge("1")
                } else {
                    Text(
                        entry.rank.toString(),
                        style = BrandType.ScoreAndData.data,
                        color = palette.inkDim
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    entry.squadName,
                    style = BrandType.IdentityAndEditorial.name,
                    color = if (isLeader) BrandColor.IfeBrass else palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${entry.manager} · ${entry.town}",
                    style = BrandType.InterfaceAndGuidance.micro,
                    color = palette.inkDim
                )
            }
            Text(
                entry.gameweekPoints.toString(),
                style = BrandType.ScoreAndData.data,
                color = palette.accent,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
            Text(
                entry.totalPoints.toString(),
                style = BrandType.ScoreAndData.data,
                color = palette.ink,
                modifier = Modifier.width(56.dp),
                textAlign = TextAlign.End
            )
        }
        BrandRule(Modifier.padding(start = BrandDimens.Gutter))
    }
}
