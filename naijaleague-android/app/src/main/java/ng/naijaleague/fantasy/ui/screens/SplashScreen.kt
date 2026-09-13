package ng.naijaleague.fantasy.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandButtonSecondary
import ng.naijaleague.fantasy.ui.components.BrandMark
import ng.naijaleague.fantasy.ui.components.HeroBackdrop

/**
 * The splash (§03).
 *
 * WHAT A SPLASH IS FOR, and it is not branding. The first screen's job is to
 * cover the gap between the icon being tapped and the app being usable. On the
 * handsets this product is actually for — a mid-range Android on a bad
 * connection — that gap is real, and an empty white frame during it is what
 * makes an app feel broken. So this screen exists to be honest about a wait,
 * not to show a logo to somebody who just tapped the logo.
 *
 * WHICH IS WHY IT HAS BUTTONS. The reference design has a splash that waits,
 * then a separate screen to choose between getting started and logging in.
 * That is two screens to answer one question. This is one: the mark and the
 * tagline settle while the app loads, and the two doors are there the moment
 * it is ready. A returning manager taps "I already have a squad" without
 * waiting for an animation to finish.
 *
 * NO PHOTOGRAPH. The reference puts a player mid-celebration behind this. The
 * players available to photograph for an NPFL app are not the ones in those
 * stock images — every one of them is a Super Eagles international playing in
 * Europe — and putting Osimhen behind an NPFL product says exactly what an
 * invented player name says. [HeroBackdrop] draws the ground instead: the
 * surface motif, and the pitch markings over it.
 */
@Composable
fun SplashScreen(
    onStart: () -> Unit,
    onSignIn: () -> Unit,
    /** Set false in tests and previews so nothing waits on a timer. */
    animated: Boolean = true
) {
    val palette = LocalBrandPalette.current

    // The mark settles, then the words. Two beats, not a performance: §14 gives
    // motion the job of explaining where something came from, and a splash that
    // entertains for three seconds is three seconds nobody asked for.
    var settled by remember { mutableStateOf(!animated) }
    LaunchedEffect(animated) {
        if (animated) {
            delay(220)
            settled = true
        }
    }
    val reveal by animateFloatAsState(
        targetValue = if (settled) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = LinearEasing),
        label = "splash-reveal"
    )

    HeroBackdrop(surface = BrandSurface.NIGHT_PITCH) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = BrandDimens.Gutter)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            BrandMark(sizeDp = 64)
            Spacer(Modifier.height(BrandDimens.SpaceLg))
            Text(
                stringResource(R.string.app_name),
                style = BrandType.IdentityAndEditorial.display2,
                color = palette.ink
            )
            Text(
                // The tagline, not the endline. "Real fans" is an invitation and
                // never a bouncer on the front door (§12).
                stringResource(R.string.tagline),
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim,
                modifier = Modifier.alpha(reveal)
            )
            Spacer(Modifier.height(BrandDimens.SpaceXxl))
            BrandButton(label = stringResource(R.string.splash_start), onClick = onStart)
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            BrandButtonSecondary(label = stringResource(R.string.splash_signin), onClick = onSignIn)
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                stringResource(R.string.splash_no_account),
                style = BrandType.InterfaceAndGuidance.micro,
                color = palette.inkDim,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(BrandDimens.SpaceXl))
        }
    }
}
