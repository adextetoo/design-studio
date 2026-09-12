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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.rules.Club
import ng.naijaleague.fantasy.rules.Money
import ng.naijaleague.fantasy.rules.SquadRules
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandButtonSecondary
import ng.naijaleague.fantasy.ui.components.BrandMark
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.ClubBadge
import ng.naijaleague.fantasy.ui.components.SectionLabel

/**
 * Onboarding — the five screens from §03, in order.
 *
 * Two deliberate product calls carried over from the brand system:
 *
 *  - The squad is built BEFORE the account exists (step 3). The squad is the
 *    sunk cost that makes the sign-up worth finishing. Every fantasy product
 *    that asks for an email on screen one loses half the room.
 *  - Phone number, not email (step 5). Nigerians live on phone numbers; a good
 *    chunk of this audience has an email address they check twice a year.
 *
 * Nothing here asks for notification permission. That is asked on Saturday
 * evening before the first deadline, in context (§03).
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var step by rememberSaveable { mutableStateOf(0) }
    var club by rememberSaveable { mutableStateOf<String?>(null) }
    var squadName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    val palette = LocalBrandPalette.current

    Column(
        Modifier
            .fillMaxSize()
            .background(palette.ground)
            .statusBarsPadding()
    ) {
        StepIndicator(step = step, total = 5, modifier = Modifier.padding(BrandDimens.Gutter))

        Box(Modifier.weight(1f)) {
            when (step) {
                0 -> StepHook(onNext = { step = 1 })
                1 -> StepClub(selected = club, onSelect = { club = it; step = 2 })
                2 -> StepBuildSquad(onNext = { step = 3 })
                3 -> StepName(value = squadName, onValue = { squadName = it }, onNext = { step = 4 })
                else -> StepSave(
                    phone = phone,
                    onPhone = { phone = it },
                    onFinish = onFinished
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int, total: Int, modifier: Modifier = Modifier) {
    val palette = LocalBrandPalette.current
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(total) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(if (index <= step) palette.accent else palette.rule)
            )
        }
    }
}

/** Step 1 — the hook. Tagline first, endline locked underneath. */
@Composable
private fun StepHook(onNext: () -> Unit) {
    val palette = LocalBrandPalette.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = BrandDimens.Gutter)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Bottom
    ) {
        BrandMark(sizeDp = 52)
        Spacer(Modifier.height(BrandDimens.SpaceXl))
        Text(
            "BUILD YOUR NPFL DREAM TEAM.\nTHEN PROVE AM.",
            style = BrandType.IdentityAndEditorial.display1,
            color = palette.ink
        )
        Spacer(Modifier.height(BrandDimens.SpaceLg))
        Text(
            "Choose 15 NPFL players. Score every matchday. Settle it with your people.",
            style = BrandType.InterfaceAndGuidance.body,
            color = palette.inkDim
        )
        Spacer(Modifier.height(BrandDimens.SpaceXl))
        BrandButton(label = "Start building", onClick = onNext)
        Spacer(Modifier.height(BrandDimens.SpaceMd))
        BrandButtonSecondary(label = "I already have a squad", onClick = onNext)
        // No endline here. §12: "real fans" is an invitation, never a test, and
        // never in onboarding — screen one of a signup flow is the door with a
        // bouncer on it that the rule exists to prevent.
        Spacer(Modifier.height(BrandDimens.SpaceXl))
    }
}

/**
 * Step 2 — pick your club.
 *
 * Asked second, before any friction: it is the cheapest and most valuable data
 * we will ever collect, it makes every screen afterwards feel addressed to one
 * person, and it is the asset we eventually sell back to clubs (§06).
 */
@Composable
private fun StepClub(selected: String?, onSelect: (String) -> Unit) {
    val palette = LocalBrandPalette.current
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
            Text(
                "Who do you support?",
                style = BrandType.IdentityAndEditorial.display2,
                color = palette.ink
            )
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                "Pick one. We'll never ask you to switch.",
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )
            Spacer(Modifier.height(BrandDimens.SpaceLg))
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).padding(horizontal = BrandDimens.SpaceMd),
            horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceSm),
            verticalArrangement = Arrangement.spacedBy(BrandDimens.SpaceSm)
        ) {
            items(SampleData.clubs, key = { it.id }) { c ->
                ClubTile(club = c, selected = c.id == selected, onClick = { onSelect(c.id) })
            }
        }
        Column(
            Modifier
                .padding(BrandDimens.Gutter)
                .navigationBarsPadding()
        ) {
            BrandButtonSecondary(
                label = "I follow the whole league",
                onClick = { onSelect("ALL") }
            )
        }
    }
}

@Composable
private fun ClubTile(club: Club, selected: Boolean, onClick: () -> Unit) {
    val palette = LocalBrandPalette.current
    Column(
        Modifier
            .clip(RoundedCornerShape(BrandDimens.CardRadius))
            .background(if (selected) palette.accent.copy(alpha = 0.16f) else palette.raised)
            .border(
                1.dp,
                if (selected) palette.accent else palette.rule,
                RoundedCornerShape(BrandDimens.CardRadius)
            )
            .clickable(onClick = onClick)
            .padding(vertical = BrandDimens.SpaceMd, horizontal = BrandDimens.SpaceSm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ClubBadge(club.shortName, 36)
        Spacer(Modifier.height(BrandDimens.SpaceSm))
        Text(
            club.name,
            style = BrandType.IdentityAndEditorial.nameMicro,
            color = palette.ink,
            textAlign = TextAlign.Center
        )
        Text(
            club.city,
            style = BrandType.InterfaceAndGuidance.label,
            color = palette.inkDim,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/** Step 3 — build the squad, still with no account. */
@Composable
private fun StepBuildSquad(onNext: () -> Unit) {
    val palette = LocalBrandPalette.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BrandDimens.Gutter)
    ) {
        Text(
            "${Money.format(SquadRules.BUDGET_NAIRA)}. ${SquadRules.SQUAD_SIZE} players. " +
                "Max ${SquadRules.MAX_PER_CLUB} from any club.",
            style = BrandType.IdentityAndEditorial.display2,
            color = palette.ink
        )
        Spacer(Modifier.height(BrandDimens.SpaceSm))
        Text(
            "Tap a position to fill it. Nothing is final until Sunday 3pm, so guess freely.",
            style = BrandType.InterfaceAndGuidance.body,
            color = palette.inkDim
        )
        Spacer(Modifier.height(BrandDimens.SpaceXl))

        // A blank 15-slot pitch is paralysing. A squad that is 20% wrong is an
        // invitation to correct it — and correcting it is the game (§03).
        SectionLabel("Suggested — change anything")
        Spacer(Modifier.height(BrandDimens.SpaceSm))
        SampleData.squad.allPlayers.take(6).forEach { player ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = BrandDimens.SquadRowHeight)
                    .padding(vertical = BrandDimens.SpaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClubBadge(SampleData.club(player.clubId).shortName, 32)
                Spacer(Modifier.width(BrandDimens.SpaceMd))
                Column(Modifier.weight(1f)) {
                    Text(
                        player.name,
                        style = BrandType.IdentityAndEditorial.name,
                        color = palette.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        player.position.label,
                        style = BrandType.InterfaceAndGuidance.micro,
                        color = palette.inkDim
                    )
                }
                Text(
                    Money.compact(player.priceNaira),
                    style = BrandType.ScoreAndData.data,
                    color = palette.ink
                )
            }
            BrandRule()
        }

        Spacer(Modifier.height(BrandDimens.SpaceLg))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Budget left",
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )
            Text(
                Money.format(SampleData.squad.budgetRemaining),
                style = BrandType.ScoreAndData.data,
                color = palette.accent
            )
        }
        Spacer(Modifier.height(BrandDimens.SpaceXl))
        BrandButton(label = "Looks good — continue", onClick = onNext)
        Spacer(Modifier.height(BrandDimens.SpaceXxl))
    }
}

/** Step 4 — name it, for the group chat rather than for us. */
@Composable
private fun StepName(value: String, onValue: (String) -> Unit, onNext: () -> Unit) {
    val palette = LocalBrandPalette.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = BrandDimens.Gutter)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Your squad needs a name your group chat will remember.",
            style = BrandType.IdentityAndEditorial.display2,
            color = palette.ink
        )
        Spacer(Modifier.height(BrandDimens.SpaceLg))
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(BrandDimens.ChipRadius))
                .background(palette.raised)
                .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.ChipRadius))
                .padding(horizontal = BrandDimens.SpaceLg),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    "Squad name",
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValue,
                singleLine = true,
                textStyle = BrandType.InterfaceAndGuidance.body.copy(
                    color = palette.ink,
                    fontFamily = BrandType.NigerianText
                ),
                cursorBrush = SolidColor(palette.accent),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(BrandDimens.SpaceMd))
        Text(
            "e.g. Aba Boys Academy · Oga At The Top United · Jagaban FC · Harmattan Rangers",
            style = BrandType.InterfaceAndGuidance.bodySmall,
            color = palette.inkDim
        )
        Spacer(Modifier.height(BrandDimens.SpaceXl))
        BrandButton(label = "Continue", onClick = onNext, enabled = value.isNotBlank())
    }
}

/** Step 5 — save, then ask for the mini-league at the moment of maximum pride. */
@Composable
private fun StepSave(phone: String, onPhone: (String) -> Unit, onFinish: () -> Unit) {
    val palette = LocalBrandPalette.current
    // The celebration is gated behind the save. A Class 4 treatment sitting on
    // screen before anything has happened is the exact failure §02 warns about:
    // it stops meaning anything, and the one moment we need it for is dead.
    var saved by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BrandDimens.Gutter)
            .navigationBarsPadding()
    ) {
        Spacer(Modifier.height(BrandDimens.SpaceXl))
        Text(
            "Save your squad",
            style = BrandType.IdentityAndEditorial.display2,
            color = palette.ink
        )
        Spacer(Modifier.height(BrandDimens.SpaceSm))
        Text(
            "Continue with your phone number. One code, no password to forget.",
            style = BrandType.InterfaceAndGuidance.body,
            color = palette.inkDim
        )
        Spacer(Modifier.height(BrandDimens.SpaceLg))
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(BrandDimens.ChipRadius))
                .background(palette.raised)
                .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.ChipRadius))
                .padding(horizontal = BrandDimens.SpaceLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("+234", style = BrandType.ScoreAndData.data, color = palette.accent)
            Spacer(Modifier.width(BrandDimens.SpaceMd))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (phone.isEmpty()) {
                    Text(
                        "800 000 0000",
                        style = BrandType.InterfaceAndGuidance.body,
                        color = palette.inkDim
                    )
                }
                BasicTextField(
                    value = phone,
                    onValueChange = { onPhone(it.filter(Char::isDigit).take(10)) },
                    singleLine = true,
                    textStyle = BrandType.InterfaceAndGuidance.body.copy(color = palette.ink),
                    cursorBrush = SolidColor(palette.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(BrandDimens.SpaceXl))
        BrandButton(
            label = "Save squad",
            onClick = { saved = true },
            enabled = phone.length >= 10 && !saved
        )

        if (saved) {
            Spacer(Modifier.height(BrandDimens.SpaceXxl))
            BrandRule()
            Spacer(Modifier.height(BrandDimens.SpaceLg))

            // Class 4, earned. §03 names this exact moment in the first-session
            // table: "OTP, saved. Confirmation in Class 4 type."
            Text(
                "SQUAD LOCKED.\nNOW GO AND TALK.",
                style = BrandType.CelebrationAndMotion.celebration,
                color = palette.accent,
                modifier = Modifier.rotate(-1f)
            )
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            Text(
                "A league with one person inside is just a spreadsheet. Add your people.",
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            BrandButtonSecondary(label = "Share invite to WhatsApp", onClick = onFinish)
        }
        Spacer(Modifier.height(BrandDimens.SpaceXxl))
    }
}
