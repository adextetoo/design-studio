package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.rules.Money
import ng.naijaleague.fantasy.rules.Player
import ng.naijaleague.fantasy.rules.Transfers
import ng.naijaleague.fantasy.ui.components.BrandBackdrop
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandButtonSecondary
import ng.naijaleague.fantasy.ui.components.BrandCard
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.ClubBadge
import ng.naijaleague.fantasy.ui.components.ScreenHeader
import ng.naijaleague.fantasy.ui.components.SectionLabel
import ng.naijaleague.fantasy.ui.components.ShirtNumber
import ng.naijaleague.fantasy.ui.components.StandInTag

/**
 * The transfer that was just confirmed.
 *
 * Demo state standing in for the squad view-model. Both ends are sourced rows
 * out of [SampleData.pool] — Ekene Awazie and Alex Oyowah are Rivers United
 * forwards named in dated 2026 reporting, and nothing here is invented (§07).
 *
 * THEY ARE BOTH RIVERS PLAYERS FOR A REASON THE DATA FORCES. The squad already
 * holds two Rivers players and the cap is two (§08), so a real player can only
 * arrive in place of a player from his own club. Fifty-six sourced players
 * across five clubs is what the league publishes; swapping a stand-in for a
 * real name is arithmetically impossible in this squad, and pretending
 * otherwise would mean inventing somebody.
 */
private val outgoing: Player = SampleData.player("RIV-awazie")
private val incoming: Player = SampleData.player("RIV-oyowah")

/**
 * The third transfer of the gameweek, which is the state worth showing.
 *
 * One free transfer plus one banked makes two (§05). This is the third, so
 * [Transfers] charges four points for it — and a confirmation screen that only
 * ever showed the free case would never demonstrate the promise it exists to
 * keep.
 */
private const val TRANSFERS_MADE = 3

/**
 * Transfer confirmation.
 *
 * NOT A CELEBRATION. §02 permits the Class 4 treatment in exactly four moments
 * — chip activation, a personal best, winning a mini-league, and the receipt —
 * and a transfer is none of them. Its scarcity is the whole point of the class,
 * so this screen is set in Class 2 and Class 3: no brass, no signwriter type,
 * no trophy. Confirming a transfer is an administrative certainty, and the
 * design should feel like a receipt from a bank, not a goal.
 *
 * THE COST IS THE ONLY NUMBER ON THE SCREEN. §05 and the rules page both
 * promise the app states the cost before a manager confirms, every single time,
 * so that nobody loses four points by accident. This screen keeps that promise
 * on the other side of the tap: it repeats the exact sentence the manager was
 * shown, straight out of [Transfers.confirmationCopy], and puts the charge in
 * the deduction colour so it cannot be skimmed past.
 *
 * THE CAPTAINCY WARNING IS NOT AN ERROR. Selling your captain leaves an XI the
 * squad validator would reject, and §12 says a message that blocks somebody has
 * to say what is wrong and what to do. So it names the player, names the
 * deadline, and stays out of the deduction colour, because nothing has gone
 * wrong yet.
 */
@Composable
fun TransferConfirmedScreen(onViewTeam: () -> Unit, onAnother: () -> Unit) {
    val palette = LocalBrandPalette.current

    val hit = remember { Transfers.pointsHit(SampleData.bankedTransfers, TRANSFERS_MADE) }
    val costLine = remember {
        Transfers.confirmationCopy(SampleData.bankedTransfers, TRANSFERS_MADE)
    }
    // What is left after the swap, worked out from the squad rather than stored.
    // Both men are priced the same band at the same club, so this week the bank
    // does not move — and saying so is better than leaving a manager to wonder.
    val bankAfter = remember {
        SampleData.squad.budgetRemaining + outgoing.priceNaira - incoming.priceNaira
    }
    val soldTheCaptain = remember { outgoing.id == SampleData.squad.captainId }

    BrandBackdrop(surface = palette.surface) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {

            ScreenHeader(
                title = "Transfer confirmed",
                subtitle = "In before ${SampleData.deadlineLabel}"
            )

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = BrandDimens.Gutter)
            ) {
                // Two objects, so two cards (§13). The order is the order it
                // happened in: somebody left, somebody arrived.
                TransferEnd(label = "Out", player = outgoing, arriving = false)
                Spacer(Modifier.height(BrandDimens.SpaceMd))
                TransferEnd(label = "In", player = incoming, arriving = true)

                Spacer(Modifier.height(BrandDimens.SpaceXl))
                BrandRule()
                Spacer(Modifier.height(BrandDimens.SpaceLg))

                // ---- The cost, stated ----
                SectionLabel("What it cost", color = if (hit > 0) palette.negative else null)
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    if (hit == 0) "No points" else "-$hit points",
                    style = BrandType.ScoreAndData.scorelineMid,
                    color = if (hit > 0) palette.negative else palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceMd))
                SectionLabel("What you were told first", color = palette.inkDim)
                Spacer(Modifier.height(BrandDimens.SpaceXs))
                Text(
                    costLine,
                    style = BrandType.InterfaceAndGuidance.body,
                    color = if (hit > 0) palette.negative else palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                // The promise itself, at 16sp. §02 v1.1 forbids 14sp for a rule
                // or for anything involving money, and this is both.
                Text(
                    stringResource(R.string.transfer_cost_note),
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )

                Spacer(Modifier.height(BrandDimens.SpaceLg))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Left in the bank",
                        style = BrandType.InterfaceAndGuidance.body,
                        color = palette.inkDim,
                        modifier = Modifier.weight(1f)
                    )
                    // Naira, formatted by the engine, never "12.5M".
                    Text(
                        Money.format(bankAfter),
                        style = BrandType.ScoreAndData.data,
                        color = palette.ink
                    )
                }

                if (soldTheCaptain) {
                    Spacer(Modifier.height(BrandDimens.SpaceLg))
                    BrandCard {
                        Column {
                            SectionLabel("Your armband is empty", color = palette.accent)
                            Spacer(Modifier.height(BrandDimens.SpaceSm))
                            // A sentence that opens with a player's name is a
                            // sentence that has to render in the Noto family, or
                            // the marks in it are dropped by a face that has no
                            // glyphs for them (§02). Class 3 size, Class 2 family.
                            Text(
                                "${outgoing.name} was your captain. Pick a new one on the " +
                                    "team screen before ${SampleData.deadlineLabel}, or you " +
                                    "will start the gameweek without one.",
                                style = BrandType.InterfaceAndGuidance.body.copy(
                                    fontFamily = BrandType.NigerianText
                                ),
                                color = palette.ink
                            )
                        }
                    }
                }

                Spacer(Modifier.height(BrandDimens.SpaceXl))
            }

            // ---- Thumb gravity: the actions sit where the hand is (§13). ----
            Column(
                Modifier
                    .padding(BrandDimens.Gutter)
                    .navigationBarsPadding()
            ) {
                BrandButton(label = "View team", onClick = onViewTeam)
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                BrandButtonSecondary(label = "Make another transfer", onClick = onAnother)
            }
        }
    }
}

/**
 * One end of the transfer.
 *
 * The arriving player takes the ink and the accent label; the departing one is
 * set in [BrandPalette.inkDim]. That is the whole hierarchy — the manager needs
 * to see at a glance which way round it went, and a red "out" would say
 * something went wrong (§01: coral is for deductions and cards).
 */
@Composable
private fun TransferEnd(label: String, player: Player, arriving: Boolean) {
    val palette = LocalBrandPalette.current
    val club = SampleData.club(player.clubId)
    BrandCard {
        Column(Modifier.fillMaxWidth()) {
            SectionLabel(label, color = if (arriving) palette.accent else palette.inkDim)
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Row(
                Modifier.fillMaxWidth().heightIn(min = BrandDimens.MinTapTarget),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClubBadge(club.shortName, 36)
                Spacer(Modifier.width(BrandDimens.SpaceMd))
                Column(Modifier.weight(1f)) {
                    // Noto. A player name set in the display family loses the
                    // dots and tone marks it is spelled with (§02).
                    Text(
                        player.name,
                        style = BrandType.IdentityAndEditorial.name,
                        color = if (arriving) palette.ink else palette.inkDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // A stand-in says on its face that it is one, wherever it
                        // is shown (§07). Neither of these two is one, but the
                        // eighteen clubs with no published squad are full of them.
                        if (player.isPlaceholder) {
                            StandInTag()
                            Spacer(Modifier.width(6.dp))
                        } else {
                            // See ChoosePlayersScreen: Player is declared in the
                            // core build, so this needs a local rather than a
                            // smart cast across the module boundary.
                            val shirt = player.squadNumber
                            if (shirt != null) {
                                ShirtNumber(shirt)
                                Spacer(Modifier.width(6.dp))
                            }
                        }
                        Text(
                            "${player.position.label} · ${club.name}",
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
                    Money.format(player.priceNaira),
                    style = BrandType.ScoreAndData.data,
                    color = if (arriving) palette.ink else palette.inkDim
                )
            }
        }
    }
}
