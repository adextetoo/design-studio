package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandMark
import ng.naijaleague.fantasy.ui.components.EndlineLockup
import ng.naijaleague.fantasy.ui.components.HeroBackdrop

/**
 * The end card — the last thing the first session shows before handing the
 * manager back to their Saturday.
 *
 * THIS IS THE SCREEN THE ENDLINE WAS WRITTEN FOR. §12 bans "Fantasy football
 * for real NPFL fans" from onboarding, from empty states, and from anywhere it
 * is aimed at somebody as a test — a door with a bouncer on it. The rule is
 * about *position*, not about the words: said to a stranger at the entrance it
 * is a challenge, and said to somebody who has just built fifteen players and
 * named a squad it is the thing they have already proved. So [EndlineLockup],
 * which the README reserves for the store listing, the About screen and share
 * cards, belongs here too and is used rather than rebuilt.
 *
 * IT IS NOT A TROPHY AND IS NOT TREATED AS ONE. §02 lets the Class 4
 * celebration voice out four times only — a chip going off, a personal best, a
 * mini-league won, and the Monday receipt. Finishing setup is none of those.
 * Nothing has been won yet, so there is no brass on this screen either (§01);
 * the loud lime appears exactly once, on the door out.
 *
 * [HeroBackdrop] rather than [ng.naijaleague.fantasy.ui.components.BrandBackdrop]
 * because this screen carries the product and not a task — it is the third of
 * the three the illustration system names, alongside the splash and the first
 * onboarding card. Drawn, not photographed, for the reason the whole
 * illustration system exists: the footballers available to photograph for an
 * NPFL app are all playing somewhere else.
 */
@Composable
fun EndCardScreen(onHome: () -> Unit) {
    val palette = LocalBrandPalette.current

    HeroBackdrop(surface = BrandSurface.NIGHT_PITCH) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = BrandDimens.Gutter)
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Class 3, not Class 2. A closing line set in the display face
                // at 28sp would be a headline shouting at somebody on their way
                // out of the door; this is one quiet sentence and it is sized
                // like one.
                Text(
                    stringResource(R.string.end_card_line),
                    style = BrandType.InterfaceAndGuidance.title,
                    color = palette.ink,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(BrandDimens.SpaceXxl))
                BrandMark(sizeDp = 56)
                Spacer(Modifier.height(BrandDimens.SpaceLg))
                EndlineLockup()
            }

            // One decision, at the thumb (§13). There is nothing else to do
            // here, and a second option would invent one.
            BrandButton(label = "Back to home", onClick = onHome)
            Spacer(Modifier.height(BrandDimens.SpaceXl))
        }
    }
}
