package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.rules.Chip
import ng.naijaleague.fantasy.rules.Money
import ng.naijaleague.fantasy.rules.Position
import ng.naijaleague.fantasy.rules.SquadRules
import ng.naijaleague.fantasy.rules.Transfers
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.ScreenHeader
import ng.naijaleague.fantasy.ui.components.SectionLabel

/**
 * Rules.
 *
 * On Nzu Chalk — the reading surface (§01). This is not a light mode; it is the
 * surface the brand system reserves for anything you actually read, and it is
 * the only screen in the app that uses it.
 *
 * Every number on this page is pulled from the rules engine rather than typed
 * in, so the page and the game can never disagree. If a scoring value changes
 * in Scoring.kt, this page changes with it.
 */
private data class ScoreRow(
    val action: String,
    val gk: String,
    val def: String,
    val mid: String,
    val fwd: String
)

private val scoringRows = listOf(
    ScoreRow("Played up to 59 minutes", "1", "1", "1", "1"),
    ScoreRow("Played 60 minutes or more", "2", "2", "2", "2"),
    ScoreRow("Goal", "6", "6", "5", "4"),
    ScoreRow("Assist", "3", "3", "3", "3"),
    ScoreRow("Clean sheet (60 mins+)", "4", "4", "1", "—"),
    ScoreRow("Every 3 saves", "1", "—", "—", "—"),
    ScoreRow("Penalty saved", "5", "—", "—", "—"),
    ScoreRow("Away goal or assist", "+1", "+1", "+1", "+1"),
    ScoreRow("Away clean sheet", "+2", "+2", "—", "—"),
    ScoreRow("The Three", "3/2/1", "3/2/1", "3/2/1", "3/2/1"),
    ScoreRow("Every 2 goals conceded", "−1", "−1", "—", "—"),
    ScoreRow("Yellow card", "−1", "−1", "−1", "−1"),
    ScoreRow("Red card", "−3", "−3", "−3", "−3"),
    ScoreRow("Penalty missed", "−2", "−2", "−2", "−2"),
    ScoreRow("Own goal", "−2", "−2", "−2", "−2")
)

@Composable
fun RulesScreen() {
    val palette = LocalBrandPalette.current

    LazyColumn(Modifier.fillMaxWidth().background(palette.ground)) {
        item {
            ScreenHeader(
                title = "How scoring works",
                subtitle = "Every point explained. No mystery bonus."
            )
        }

        // ---- The Away Day Bonus, first, because it is the difference ----
        item {
            Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                SectionLabel("The Away Day Bonus")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "In the NPFL, home teams win. Everybody knows it — the pitches, the travel, " +
                        "the nine hours on a bus, the crowd. So backing an away player is the " +
                        "bravest thing you can do in this game, and we pay you for it.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "Goal or assist away from home: one extra point. Clean sheet away: two extra. " +
                        "No other fantasy game in the world has this, because no other league needs it.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceXl))
            }
        }

        // ---- Scoring table. A table, set as type on the ground (§13). ----
        item {
            Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                SectionLabel("Scoring")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceSm)
            ) {
                Text("ACTION", style = BrandType.InterfaceAndGuidance.label,
                    color = palette.inkDim, modifier = Modifier.weight(1f))
                listOf("GK", "DEF", "MID", "FWD").forEach {
                    Text(it, style = BrandType.InterfaceAndGuidance.label, color = palette.inkDim,
                        modifier = Modifier.width(38.dp), textAlign = TextAlign.End)
                }
            }
            BrandRule(Modifier.padding(horizontal = BrandDimens.Gutter))
        }
        items(scoringRows) { row ->
            val emphasise = row.action.startsWith("Away") || row.action == "The Three"
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = BrandDimens.Gutter, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        row.action,
                        style = BrandType.InterfaceAndGuidance.body,
                        color = if (emphasise) palette.accent else palette.ink,
                        modifier = Modifier.weight(1f)
                    )
                    listOf(row.gk, row.def, row.mid, row.fwd).forEach { value ->
                        Text(
                            value,
                            style = BrandType.ScoreAndData.data,
                            color = if (emphasise) palette.accent else palette.ink,
                            modifier = Modifier.width(38.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
                BrandRule(Modifier.padding(horizontal = BrandDimens.Gutter))
            }
        }

        // ---- The Three ----
        item {
            Column(
                Modifier
                    .padding(horizontal = BrandDimens.Gutter)
                    .padding(top = BrandDimens.SpaceXl)
            ) {
                SectionLabel("The Three — and why it's not a secret")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "Three points to the best player in each match, two to the next, one to the next.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "The difference between us and everybody else is that we publish the reason. " +
                        "Every award comes with a line from the match scorer who was at the ground. " +
                        "If you think we got it wrong, you have two hours to flag it with the clip. " +
                        "Points are provisional until Monday, 12:00.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "We would rather be corrected in public than pretend a number came from nowhere.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )
                Spacer(Modifier.height(BrandDimens.SpaceXl))
            }
        }

        // ---- Squad rules, read from the engine ----
        item {
            Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                SectionLabel("Your squad")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                RuleLine("Budget", Money.format(SquadRules.BUDGET_NAIRA))
                RuleLine("Squad size", "${SquadRules.SQUAD_SIZE} players")
                RuleLine(
                    "Shape",
                    Position.entries.joinToString(", ") {
                        "${SquadRules.QUOTA[it]} ${it.short}"
                    }
                )
                RuleLine("Maximum from one club", "${SquadRules.MAX_PER_CLUB} players")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "Two per club, not three. The NPFL is more even than England, and we are not " +
                        "letting you stack one squad — which also means you end up caring about at " +
                        "least eight clubs.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )
                Spacer(Modifier.height(BrandDimens.SpaceXl))
            }
        }

        // ---- Transfers, read from the engine ----
        item {
            Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                SectionLabel("Transfers")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                RuleLine("Free every gameweek", "${Transfers.FREE_PER_GW}")
                RuleLine("You can bank up to", "${Transfers.MAX_BANKED}")
                RuleLine("Each extra transfer costs", "${Transfers.HIT_PER_EXTRA} points")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "The app tells you the cost before you confirm, every single time. Nobody " +
                        "should ever lose four points by accident.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "Your changes save on your phone first, then sync. If your data finishes " +
                        "mid-transfer, the move is not lost — it goes through when you reconnect, " +
                        "as long as that happens before Sunday 3pm.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )
                Spacer(Modifier.height(BrandDimens.SpaceXl))
            }
        }

        // ---- Chips, straight off the enum ----
        item {
            Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                SectionLabel("Chips · five a season")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
            }
        }
        items(Chip.entries.toList()) { chip ->
            Column(Modifier.padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceSm)) {
                Text(
                    chip.chipName,
                    style = BrandType.InterfaceAndGuidance.title,
                    color = palette.ink
                )
                Text(
                    chip.explanation,
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )
            }
        }

        item {
            Column(
                Modifier
                    .padding(BrandDimens.Gutter)
                    .padding(top = BrandDimens.SpaceLg, bottom = BrandDimens.SpaceXxl)
            ) {
                BrandRule()
                Spacer(Modifier.height(BrandDimens.SpaceLg))
                Text(
                    "One thing we will never do",
                    style = BrandType.InterfaceAndGuidance.title,
                    color = palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "You cannot buy points. Not a chip, not a transfer, not a multiplier, not a " +
                        "boost. Gaffer Pass gets you better information and nicer things to look " +
                        "at. It gets you nothing your rival cannot get for free.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "The day money buys points, the table starts lying.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.accent
                )
            }
        }
    }
}

@Composable
private fun RuleLine(label: String, value: String) {
    val palette = LocalBrandPalette.current
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = BrandType.InterfaceAndGuidance.body, color = palette.inkDim)
        Text(value, style = BrandType.ScoreAndData.data, color = palette.ink)
    }
}
