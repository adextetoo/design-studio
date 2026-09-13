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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.NpflSquads
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.rules.DifferentialTier
import ng.naijaleague.fantasy.rules.Money
import ng.naijaleague.fantasy.rules.Player
import ng.naijaleague.fantasy.rules.Position
import ng.naijaleague.fantasy.rules.SquadRules
import ng.naijaleague.fantasy.rules.Transfers
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.ClubBadge
import ng.naijaleague.fantasy.ui.components.ShirtNumber
import ng.naijaleague.fantasy.ui.components.SourceNote
import ng.naijaleague.fantasy.ui.components.StandInTag
import ng.naijaleague.fantasy.ui.components.squadDataSummary
import ng.naijaleague.fantasy.ui.components.squadSourceLine

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
    // Off by default: hiding eighteen clubs would be a stranger first impression
    // than showing stand-ins. On, it is the list a manager who only wants real
    // players actually wants.
    var sourcedOnly by remember { mutableStateOf(false) }

    val squad = SampleData.squad
    val ownedIds = remember { squad.allPlayers.map { it.id }.toSet() }
    val clubCounts = remember { squad.allPlayers.groupingBy { it.clubId }.eachCount() }

    // In the live build this comes from the squad view-model; the screen only
    // needs to know how many changes are pending in order to price them.
    val transfersMade = 0

    val shown = remember(filter, query, sourcedOnly) {
        SampleData.pool
            .filter { filter == null || it.position == filter }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .filter { !sourcedOnly || !it.isPlaceholder }
            // Real players first at equal price, so a manager scanning the list
            // meets the people before the stand-ins.
            .sortedWith(compareBy({ it.isPlaceholder }, { -it.priceNaira }))
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
                textStyle = BrandType.InterfaceAndGuidance.body.copy(
                    color = palette.ink,
                    fontFamily = BrandType.NigerianText
                ),
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
            FilterPill(stringResource(R.string.choose_real_only), sourcedOnly) { sourcedOnly = !sourcedOnly }
        }

        Spacer(Modifier.height(BrandDimens.SpaceMd))

        // Said once, at the top, in the app's own voice. Somebody who knows the
        // NPFL already knows nineteen club sites have no squad page; hiding it
        // would only tell them we do not.
        SourceNote(
            label = stringResource(R.string.squad_data_label),
            detail = squadDataSummary,
            modifier = Modifier.padding(horizontal = BrandDimens.Gutter)
        )

        // ---- The list. Dense on purpose: this audience reads tables for pleasure. ----
        LazyColumn(Modifier.weight(1f)) {
            items(shown, key = { it.id }) { player ->
                val clubFull = (clubCounts[player.clubId] ?: 0) >= SquadRules.MAX_PER_CLUB
                val owned = player.id in ownedIds
                val tooExpensive = player.priceNaira > squad.budgetRemaining
                PlayerRow(
                    player = player,
                    owned = owned,
                    onToggle = { /* wired to the squad view-model in the live build */ },
                    blockedReason = when {
                        owned -> null
                        clubFull -> "2 from ${SampleData.club(player.clubId).name} already"
                        tooExpensive -> "Over your budget"
                        else -> null
                    }
                )
            }

            // "Where did you get that" answered at the bottom of the list it
            // applies to, once per sourced club, rather than buried in an about
            // page nobody opens.
            items(NpflSquads.sources, key = { it.clubId }) { source ->
                squadSourceLine(source.clubId)?.let { (label, detail) ->
                    SourceNote(
                        label = label,
                        detail = detail,
                        modifier = Modifier
                            .padding(horizontal = BrandDimens.Gutter)
                            .padding(bottom = BrandDimens.SpaceMd)
                    )
                }
            }
        }

        Column(
            Modifier
                .background(palette.ground)
                .padding(BrandDimens.Gutter)
        ) {
            // The rules page promises "the app tells you the cost before you
            // confirm, every single time". This is where that promise is kept.
            Text(
                Transfers.confirmationCopy(
                    bankedComingIn = SampleData.bankedTransfers,
                    transfersMade = transfersMade
                ),
                style = BrandType.InterfaceAndGuidance.body,
                color = if (Transfers.pointsHit(SampleData.bankedTransfers, transfersMade) > 0) {
                    palette.negative
                } else {
                    palette.inkDim
                }
            )
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            BrandButton(label = "Save squad", onClick = onClose, enabled = squad.isValid)
        }
    }
}

@Composable
private fun FilterPill(label: String, active: Boolean, onClick: () -> Unit) {
    val palette = LocalBrandPalette.current
    Box(
        Modifier
            .heightIn(min = BrandDimens.MinTapTarget)
            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
            .background(if (active) palette.accent else palette.raised)
            .clickable(onClick = onClick)
            .padding(horizontal = BrandDimens.SpaceLg),
        contentAlignment = Alignment.Center
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
private fun PlayerRow(
    player: Player,
    owned: Boolean,
    onToggle: () -> Unit,
    blockedReason: String?
) {
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
                    if (player.isPlaceholder) {
                        StandInTag()
                        Spacer(Modifier.width(6.dp))
                    } else if (player.squadNumber != null) {
                        ShirtNumber(player.squadNumber)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        "${player.position.short} · ${SampleData.club(player.clubId).name}",
                        style = BrandType.IdentityAndEditorial.nameMicro,
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
            AddButton(enabled = enabled, owned = owned, onToggle = onToggle)
        }
        BrandRule(Modifier.padding(start = BrandDimens.Gutter))
    }
}

@Composable
private fun AddButton(enabled: Boolean, owned: Boolean, onToggle: () -> Unit) {
    val palette = LocalBrandPalette.current
    // Brass is honours only. Owning a player is a state, not a trophy.
    val fill = when {
        owned -> palette.raised
        enabled -> palette.accent
        else -> palette.raised
    }
    Box(
        Modifier
            .size(BrandDimens.MinTapTarget)
            .clip(CircleShape)
            .background(fill)
            .clickable(enabled = enabled || owned, onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (owned) "−" else "+",
            style = BrandType.InterfaceAndGuidance.title,
            color = when {
                owned -> palette.ink
                enabled -> palette.accentInk
                else -> palette.inkDim
            }
        )
    }
}
