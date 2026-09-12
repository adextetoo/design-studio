package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.rules.DifferentialTier
import ng.naijaleague.fantasy.rules.Money
import ng.naijaleague.fantasy.rules.Player
import ng.naijaleague.fantasy.rules.Position
import ng.naijaleague.fantasy.rules.SquadRules
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.ClubBadge

/**
 * Choose players.
 *
 * Two brand rules are enforced in the UI and not just in the engine, because a
 * rule a manager only discovers at save time is a rule they experience as a
 * bug: the two-per-club cap (§08) and the budget. A player you cannot legally
 * add shows why, on the row, before you tap.
 */
@Composable
fun ChoosePlayersScreen(onClose: () -> Unit) {
    val palette = LocalBrandPalette.current
    var filter by remember { mutableStateOf<Position?>(null) }
    var query by remember { mutableStateOf("") }

    val squad = SampleData.squad
    val ownedIds = remember { squad.allPlayers.map { it.id }.toSet() }
    val clubCounts = remember { squad.allPlayers.groupingBy { it.clubId }.eachCount() }

    val shown = remember(filter, query) {
        SampleData.pool
            .filter { filter == null || it.position == filter }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .sortedByDescending { it.priceNaira }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(palette.ground)
            .statusBarsPadding()
    ) {
        // ---- Header: budget is the number that matters here, so it is the hero ----
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = BrandDimens.Gutter)
                .padding(top = BrandDimens.SpaceLg, bottom = BrandDimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(BrandDimens.MinTapTarget)
                    .clip(CircleShape)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text("<", style = BrandType.InterfaceAndGuidance.title, color = palette.ink)
            }
            Spacer(Modifier.width(BrandDimens.SpaceSm))
            Column(Modifier.weight(1f)) {
                Text(
                    "Choose your players",
                    style = BrandType.IdentityAndEditorial.display2,
                    color = palette.ink
                )
                Text(
                    "${squad.allPlayers.size}/${SquadRules.SQUAD_SIZE} picked · " +
                        "${Money.format(squad.budgetRemaining)} left · max 2 from any club",
                    style = BrandType.ScoreAndData.dataSmall,
                    color = palette.inkDim
                )
            }
        }

        // ---- Search ----
        Box(
            Modifier
                .padding(horizontal = BrandDimens.Gutter)
                .fillMaxWidth()
                .heightIn(min = BrandDimens.MinTapTarget)
                .clip(RoundedCornerShape(BrandDimens.ChipRadius))
                .background(palette.raised)
                .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.ChipRadius))
                .padding(horizontal = BrandDimens.SpaceMd),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    "Search players",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )
            }
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = BrandType.InterfaceAndGuidance.body.copy(color = palette.ink),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(palette.accent),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(BrandDimens.SpaceMd))

        // ---- Position filter ----
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = BrandDimens.Gutter),
            horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceSm)
        ) {
            FilterPill("All", filter == null) { filter = null }
            Position.entries.forEach { position ->
                FilterPill(position.short, filter == position) { filter = position }
            }
        }

        Spacer(Modifier.height(BrandDimens.SpaceMd))
        BrandRule()

        // ---- The list. Dense on purpose: this audience reads tables for pleasure. ----
        LazyColumn(Modifier.weight(1f)) {
            items(shown, key = { it.id }) { player ->
                val clubFull = (clubCounts[player.clubId] ?: 0) >= SquadRules.MAX_PER_CLUB
                val owned = player.id in ownedIds
                val tooExpensive = player.priceNaira > squad.budgetRemaining
                PlayerRow(
                    player = player,
                    owned = owned,
                    blockedReason = when {
                        owned -> null
                        clubFull -> "2 from ${player.clubId} already"
                        tooExpensive -> "Over your budget"
                        else -> null
                    }
                )
            }
        }

        Column(
            Modifier
                .background(palette.ground)
                .padding(BrandDimens.Gutter)
        ) {
            BrandButton(label = "Save squad", onClick = onClose, enabled = squad.isValid)
        }
    }
}

@Composable
private fun FilterPill(label: String, active: Boolean, onClick: () -> Unit) {
    val palette = LocalBrandPalette.current
    Box(
        Modifier
            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
            .background(if (active) palette.accent else palette.raised)
            .clickable(onClick = onClick)
            .padding(horizontal = BrandDimens.SpaceLg, vertical = BrandDimens.SpaceSm)
    ) {
        Text(
            label,
            style = BrandType.InterfaceAndGuidance.label,
            color = if (active) palette.accentInk else palette.inkDim
        )
    }
}

/**
 * A squad row: 56dp, dense, scannable fifteen at a time (§13).
 *
 * Ownership is shown because the differential multiplier makes it a real
 * decision, not trivia — under 5% pays 1.25x, under 2% pays 1.5x (§08).
 */
@Composable
private fun PlayerRow(player: Player, owned: Boolean, blockedReason: String?) {
    val palette = LocalBrandPalette.current
    val tier = DifferentialTier.forOwnership(player.ownershipPct)
    val enabled = blockedReason == null && !owned

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = BrandDimens.SquadRowHeight)
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClubBadge(SampleData.club(player.clubId).shortName, 34)
            Spacer(Modifier.width(BrandDimens.SpaceMd))
            Column(Modifier.weight(1f)) {
                Text(
                    player.name,
                    style = BrandType.IdentityAndEditorial.name,
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${player.position.short} · ${SampleData.club(player.clubId).name}",
                        style = BrandType.InterfaceAndGuidance.micro,
                        color = palette.inkDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (tier != DifferentialTier.NONE) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${player.ownershipPct}% · x${tier.multiplier}",
                            style = BrandType.InterfaceAndGuidance.label,
                            color = palette.accent
                        )
                    }
                }
            }
            Spacer(Modifier.width(BrandDimens.SpaceSm))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    Money.compact(player.priceNaira),
                    style = BrandType.ScoreAndData.data,
                    color = palette.ink
                )
                if (blockedReason != null) {
                    Text(
                        blockedReason,
                        style = BrandType.InterfaceAndGuidance.micro,
                        color = palette.negative
                    )
                }
            }
            Spacer(Modifier.width(BrandDimens.SpaceMd))
            AddButton(enabled = enabled, owned = owned)
        }
        BrandRule(Modifier.padding(start = BrandDimens.Gutter))
    }
}

@Composable
private fun AddButton(enabled: Boolean, owned: Boolean) {
    val palette = LocalBrandPalette.current
    val fill = when {
        owned -> BrandColor.IfeBrass
        enabled -> palette.accent
        else -> palette.raised
    }
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(fill),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (owned) "-" else "+",
            style = BrandType.InterfaceAndGuidance.title,
            color = if (enabled || owned) BrandColor.NightPitch else palette.inkDim
        )
    }
}
