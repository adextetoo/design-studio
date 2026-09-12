package ng.naijaleague.fantasy.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.brand.NaijaLeagueTheme
import ng.naijaleague.fantasy.rules.Money
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.SectionLabel

/**
 * Gaffer Pass — the premium tier (§06).
 *
 * This screen is the one place in the product where money is asked for, so two
 * brand rules govern it absolutely:
 *
 *  1. **It never sells points.** §05: "You cannot buy points. Not a chip, not a
 *     transfer, not a multiplier, not a boost." Everything here is information,
 *     convenience or vanity. That promise is stated on the screen, not buried —
 *     it is the reason a manager can trust the table, and the table is the
 *     entire brand.
 *  2. **No dark patterns.** No fake countdown, no pre-ticked plan, no obscured
 *     price, no "cancel anytime" in 9sp grey. The audience is being asked for
 *     real money in a market where trust is scarce.
 *
 * It runs on Adire Indigo — §01 gives Indigo "editorial and story surfaces",
 * and this is the one screen that has to argue rather than display.
 *
 * Plan order is deliberate and follows §06's reasoning: weekly first, because
 * this market buys airtime in ₦200 and ₦500 units and weekly billing matches
 * how people already think about topping up. The season plan is shown with its
 * real saving rather than an invented "was" price.
 */
private data class Plan(
    val id: String,
    val name: String,
    val priceNaira: Long,
    val period: String,
    val note: String,
    val saving: String? = null
)

private val plans = listOf(
    Plan("week", "Weekly", 400, "a week", "Buy it for one big gameweek and stop."),
    Plan("month", "Monthly", 1_200, "a month", "About what a mobile streaming plan costs."),
    Plan("season", "Full season", 7_500, "for 9 months", "Sept to May, one payment.", saving = "Save 31%"),
    Plan("league", "Mini-league bundle", 28_000, "for 5 people", "One person pays for the group.")
)

private val included = listOf(
    "The Differential Board" to
        "Every player under 5% ownership, ranked by upcoming fixture difficulty.",
    "Team news by SMS" to
        "The alert fires for everyone at the same moment — you also get it as a text, " +
            "so it reaches you when your data has finished. A delivery channel, not a head start.",
    "Unlimited saved squads" to
        "Model four squads side by side, see the projected difference, then commit.",
    "The Gaffer's Room" to
        "A Monday voice note in Pidgin from a working NPFL coach, walking through what happened.",
    "Head-to-head and pick-order leagues" to
        "For groups that want a knockout instead of a table.",
    "Crest builder" to
        "Build a squad badge from the Adire, Uli, Nsibidi and Arewa components.",
    "Season archive" to
        "Every receipt, every captain call, every rank — kept forever.",
    "No ads" to "Nothing sold against your attention."
)

@Composable
fun GafferPassScreen(onClose: () -> Unit) {
    NaijaLeagueTheme(surface = BrandSurface.ADIRE_INDIGO) {
        val palette = LocalBrandPalette.current
        var selected by rememberSaveable { mutableStateOf("season") }

        Column(
            Modifier
                .fillMaxSize()
                .background(palette.ground)
                .statusBarsPadding()
        ) {
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

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = BrandDimens.Gutter)
            ) {
                SectionLabel("Gaffer Pass", color = BrandColor.IfeBrass)
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "Better information.\nNot better odds.",
                    style = BrandType.IdentityAndEditorial.display1,
                    color = palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceMd))
                Text(
                    "Everything in Gaffer Pass is information, convenience or vanity. " +
                        "None of it is points.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )

                Spacer(Modifier.height(BrandDimens.SpaceXl))
                SectionLabel("What you get")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                included.forEach { (title, detail) ->
                    Column(Modifier.padding(vertical = BrandDimens.SpaceSm)) {
                        Text(
                            title,
                            style = BrandType.InterfaceAndGuidance.title,
                            color = palette.ink
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            detail,
                            style = BrandType.InterfaceAndGuidance.body,
                            color = palette.inkDim
                        )
                    }
                    BrandRule()
                }

                Spacer(Modifier.height(BrandDimens.SpaceXl))
                SectionLabel("Choose a plan")
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                plans.forEach { plan ->
                    PlanRow(
                        plan = plan,
                        selected = plan.id == selected,
                        onSelect = { selected = plan.id }
                    )
                    Spacer(Modifier.height(BrandDimens.SpaceSm))
                }

                Spacer(Modifier.height(BrandDimens.SpaceLg))
                Text(
                    "You cannot buy points. Not a chip, not a transfer, not a multiplier, " +
                        "not a boost. Gaffer Pass gets you nothing your rival cannot get for free.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "The day money buys points, the table starts lying.",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.accent
                )
                Spacer(Modifier.height(BrandDimens.SpaceXl))
            }

            Column(
                Modifier
                    .background(palette.ground)
                    .padding(BrandDimens.Gutter)
                    .navigationBarsPadding()
            ) {
                val plan = plans.first { it.id == selected }
                BrandButton(
                    label = "Start ${plan.name.lowercase()} — ${Money.format(plan.priceNaira)}",
                    onClick = onClose
                )
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    "Cancel any time in Settings. No auto-upgrade, no price change without a " +
                        "message first.",
                    style = BrandType.InterfaceAndGuidance.bodySmall,
                    color = palette.inkDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PlanRow(plan: Plan, selected: Boolean, onSelect: () -> Unit) {
    val palette = LocalBrandPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BrandDimens.CardRadius))
            .background(if (selected) BrandColor.IfeBrass.copy(alpha = 0.14f) else palette.raised)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) BrandColor.IfeBrass else palette.rule,
                RoundedCornerShape(BrandDimens.CardRadius)
            )
            .clickable(onClick = onSelect)
            .padding(BrandDimens.SpaceLg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    plan.name,
                    style = BrandType.InterfaceAndGuidance.title,
                    color = palette.ink
                )
                if (plan.saving != null) {
                    Spacer(Modifier.width(BrandDimens.SpaceSm))
                    // Brass is earned here: this is the honours colour and the
                    // Gaffer Pass is one of its four permitted homes (§01).
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
                            .background(BrandColor.IfeBrass)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            plan.saving,
                            style = BrandType.InterfaceAndGuidance.label,
                            color = BrandColor.NightPitch
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                plan.note,
                style = BrandType.InterfaceAndGuidance.bodySmall,
                color = palette.inkDim
            )
        }
        Spacer(Modifier.width(BrandDimens.SpaceMd))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                Money.format(plan.priceNaira),
                style = BrandType.ScoreAndData.data,
                color = palette.ink
            )
            Text(
                plan.period,
                style = BrandType.InterfaceAndGuidance.micro,
                color = palette.inkDim
            )
        }
    }
}
