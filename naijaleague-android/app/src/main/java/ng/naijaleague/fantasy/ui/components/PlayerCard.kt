package ng.naijaleague.fantasy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.NpflClubs
import ng.naijaleague.fantasy.data.NpflSquads
import ng.naijaleague.fantasy.rules.DifferentialTier
import ng.naijaleague.fantasy.rules.Money
import ng.naijaleague.fantasy.rules.Performance
import ng.naijaleague.fantasy.rules.Player
import ng.naijaleague.fantasy.rules.PlayerScore
import ng.naijaleague.fantasy.rules.Venue

/**
 * The player card.
 *
 * THE SHAPE IS THE ONE EVERYBODY KNOWS — a portrait card, the shirt, the big
 * number, a column of rated attributes underneath. That form is good and worth
 * borrowing. What goes INTO it is where this has to part company with a game
 * built on a scouting database.
 *
 * THERE IS NO PHOTOGRAPH, AND SAYING SO IS THE POINT. Nobody publishes squad
 * photographs for eighteen of these twenty clubs. The options were a stock
 * portrait of somebody else, a generated face, or an honest blank. The first
 * two are the same mistake as an invented name, on a bigger canvas — so the
 * card carries the club shirt where the face would be, which is what a
 * supporter actually recognises first anyway.
 *
 * THERE IS NO AGE EITHER, and none is shown. An age is a fact about a person.
 * Guessing one for Nathaniel Asibe because the layout has a slot for it would
 * be inventing a fact about a real man to fill a gap in a design.
 *
 * AND THE RATINGS ARE MATCH RETURNS, NOT ATTRIBUTES. Pace, shooting and passing
 * are scouting numbers somebody pays a network of analysts to produce, and no
 * such network covers the NPFL. Every number on this card is one the scoring
 * engine produced from what the scorer at the ground filed: minutes, goals,
 * assists, saves, cards, The Three, the ownership that sets the differential
 * multiplier. That is a real rate card. It is not a made-up one.
 */
@Composable
fun PlayerCardSheet(
    player: Player,
    score: PlayerScore?,
    performance: Performance?,
    isCaptain: Boolean,
    onClose: () -> Unit,
    onClaim: () -> Unit
) {
    val palette = LocalBrandPalette.current
    val record = NpflClubs.record(player.clubId)
    val registered = NpflSquads.forClub(player.clubId)
        .firstOrNull { it.name == player.name }

    Column(
        Modifier
            .fillMaxSize()
            .background(palette.ground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            Modifier
                .heightIn(min = BrandDimens.MinTapTarget)
                .padding(horizontal = BrandDimens.SpaceSm)
                .clickable(onClick = onClose)
                .padding(horizontal = BrandDimens.SpaceSm),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("‹", style = BrandType.InterfaceAndGuidance.title, color = palette.ink)
        }

        // ---- The card itself ----
        Column(
            Modifier
                .padding(horizontal = BrandDimens.Gutter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(BrandDimens.CardRadius))
                .background(palette.raised)
                .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.CardRadius))
                .padding(BrandDimens.SpaceXl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The gameweek score is the headline number, because it is the only
            // rating this product believes in.
            PointsCounter(points = score?.finalPoints ?: 0)
            Text(
                "POINTS · GAMEWEEK 12",
                style = BrandType.InterfaceAndGuidance.label,
                color = palette.inkDim
            )
            Spacer(Modifier.height(BrandDimens.SpaceLg))

            ClubJersey(
                clubId = player.clubId,
                sizeDp = 96.dp,
                squadNumber = player.squadNumber
            )
            Spacer(Modifier.height(BrandDimens.SpaceMd))

            Text(
                player.name,
                style = BrandType.IdentityAndEditorial.display2.copy(
                    fontFamily = BrandType.NigerianText
                ),
                color = if (player.isPlaceholder) palette.inkDim else palette.ink,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(BrandDimens.SpaceXs))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceSm)
            ) {
                Text(
                    player.position.label,
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )
                Text("·", color = palette.inkDim, style = BrandType.InterfaceAndGuidance.body)
                Text(
                    record.club.name,
                    style = BrandType.IdentityAndEditorial.nameMicro,
                    color = palette.inkDim
                )
            }
            if (isCaptain) {
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                HonourBadge("CAPTAIN · SCORES DOUBLE")
            }
            if (player.isPlaceholder) {
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                StandInTag()
            }
        }

        Spacer(Modifier.height(BrandDimens.SpaceXl))

        // ---- The rate card: every row is engine output ----
        Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
            SectionLabel("This gameweek")
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            if (score == null || performance == null) {
                Text(
                    "No match filed. Under Owambe a bench player scores; otherwise " +
                        "this one sat it out.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )
            } else {
                score.lines.forEach { line ->
                    RateRow(line.label, "${if (line.points >= 0) "+" else ""}${line.points}")
                }
                if (score.differentialTier != DifferentialTier.NONE) {
                    RateRow(
                        score.differentialTier.label,
                        "×${score.differentialTier.multiplier}",
                        emphasis = true
                    )
                }
                if (score.captainMultiplier > 1) {
                    RateRow("Captain", "×${score.captainMultiplier}", emphasis = true)
                }
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                BrandRule()
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                RateRow("Total", "${score.finalPoints}", emphasis = true)
            }

            Spacer(Modifier.height(BrandDimens.SpaceXl))
            SectionLabel("The numbers behind it")
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            RateRow("Price", Money.compact(player.priceNaira))
            RateRow("Owned by", "${player.ownershipPct}%")
            RateRow(
                "Differential multiplier",
                DifferentialTier.forOwnership(player.ownershipPct).let {
                    if (it == DifferentialTier.NONE) "none — over 5% owned" else "×${it.multiplier}"
                }
            )
            performance?.let {
                RateRow("Minutes", "${it.minutes}")
                RateRow("Venue", if (it.venue == Venue.AWAY) "Away" else "Home")
                if (it.theThree > 0) {
                    RateRow("The Three", "${it.theThree}")
                }
            }
            player.squadNumber?.let { RateRow("Shirt", "$it") }

            performance?.theThreeReason?.let { reason ->
                Spacer(Modifier.height(BrandDimens.SpaceLg))
                SourceNote(label = "Why they got The Three", detail = reason)
            }

            // ---- What the card does not have, said plainly ----
            Spacer(Modifier.height(BrandDimens.SpaceXl))
            SourceNote(
                label = "No photo, no age, no attribute ratings",
                detail = if (player.isPlaceholder) {
                    "This is a stand-in, not a person — there is nothing to photograph. " +
                        "No squad list is published for ${record.club.shortName} that any " +
                        "source will stand behind."
                } else {
                    "Nobody publishes squad photographs or dates of birth for this league, " +
                        "and no scouting network rates NPFL players for pace or passing. " +
                        "Every number on this card is one the scoring engine worked out " +
                        "from what the scorer at the ground filed. We would rather show you " +
                        "a shirt than a stranger's face."
                }
            )

            // The route for the one person who knows this card is wrong.
            Spacer(Modifier.height(BrandDimens.SpaceLg))
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = BrandDimens.MinTapTarget)
                    .clip(RoundedCornerShape(BrandDimens.ChipRadius))
                    .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.ChipRadius))
                    .clickable(onClick = onClaim),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.claim_link),
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.accent
                )
            }

            registered?.note?.let { note ->
                Spacer(Modifier.height(BrandDimens.SpaceMd))
                SourceNote(label = "On this name", detail = note)
            }
            if (!hasRenderableKit(player.clubId)) {
                Spacer(Modifier.height(BrandDimens.SpaceMd))
                SourceNote(
                    label = "The shirt is blank on purpose",
                    detail = kitWords(player.clubId)
                        ?.let { "Sources disagree: $it. Rather than pick a side, the shirt is drawn empty." }
                        ?: "No source names this club's colours. A blank shirt says we do not know; a wrong one says we do not know the league."
                )
            }
            Spacer(Modifier.navigationBarsPadding().height(BrandDimens.SpaceXxl))
        }
    }
}

/** One line of the rate card. Label left, value right, digits aligned. */
@Composable
private fun RateRow(label: String, value: String, emphasis: Boolean = false) {
    val palette = LocalBrandPalette.current
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = BrandType.InterfaceAndGuidance.body,
            color = if (emphasis) palette.ink else palette.inkDim,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(BrandDimens.SpaceMd))
        Text(
            value,
            style = BrandType.ScoreAndData.data,
            color = if (emphasis) palette.accent else palette.ink
        )
    }
}
