package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.rules.Eligibility
import ng.naijaleague.fantasy.ui.components.BrandBackdrop
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.SectionLabel

/**
 * Save your squad — the account step (§03).
 *
 * WHERE THIS SITS, AND WHY IT IS NOT SCREEN TWO. The reference flow asks for an
 * account second, before the manager has seen anything. §03's first four
 * minutes says the opposite and the board approved it: choose a club, choose
 * fifteen players, see the pitch, and only then be asked for a name. By that
 * point the manager has built something and the account is how they keep it,
 * which is a completely different question from "sign up to look around".
 *
 * So the screen the reference calls Sign Up exists — it was missing and it is
 * needed — but it is the door out of onboarding rather than the door in. The
 * heading says what is actually being saved.
 *
 * WHAT IS ASKED FOR, AND WHAT IS NOT. Phone first, because that is how this
 * market signs in and because §05's SMS team news needs a number anyway. Google
 * second. No Apple button: this is an Android product, Apple requires that
 * button on iOS and nowhere else, and a dead option on a signup form is one
 * more thing to read.
 *
 * NO PASSWORD FIELD EITHER. A password is a thing to forget on the one day of
 * the week that matters, twenty minutes before a deadline. A one-time code to
 * the number already being collected is fewer fields, fewer resets, and one
 * less credential for this product to be responsible for storing.
 *
 * THE AGE FIELD IS NOT OPTIONAL AND NOT DECORATION. [Eligibility] gates play at
 * sixteen and cash prizes at eighteen, and that gate fails closed: no date, no
 * account. The copy says which is which, because a sixteen-year-old who wins
 * and is then refused would have been misled by this screen.
 */
@Composable
fun SignUpScreen(
    squadName: String,
    onSaved: () -> Unit,
    onSignIn: () -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalBrandPalette.current
    var phone by remember { mutableStateOf("") }
    var manager by remember { mutableStateOf("") }

    BrandBackdrop(surface = BrandSurface.NIGHT_PITCH) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = BrandDimens.Gutter)
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(BrandDimens.SpaceLg))
            Box(
                Modifier
                    .heightIn(min = BrandDimens.MinTapTarget)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("<", style = BrandType.InterfaceAndGuidance.title, color = palette.ink)
            }

            Text(
                stringResource(R.string.signup_head),
                style = BrandType.IdentityAndEditorial.display2,
                color = palette.ink
            )
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                // Names the squad they just built, so it is obvious what is at
                // stake if they close the app here.
                stringResource(R.string.signup_body, squadName),
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )

            Spacer(Modifier.height(BrandDimens.SpaceXl))
            ProviderButton(stringResource(R.string.signup_google), onSaved)

            Spacer(Modifier.height(BrandDimens.SpaceLg))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandRule(Modifier.weight(1f))
                Text(
                    stringResource(R.string.signup_or),
                    style = BrandType.InterfaceAndGuidance.label,
                    color = palette.inkDim,
                    modifier = Modifier.padding(horizontal = BrandDimens.SpaceMd)
                )
                BrandRule(Modifier.weight(1f))
            }
            Spacer(Modifier.height(BrandDimens.SpaceLg))

            Field(
                value = manager,
                onValueChange = { manager = it },
                placeholder = stringResource(R.string.signup_manager_name)
            )
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            Field(
                value = phone,
                onValueChange = { phone = it },
                placeholder = stringResource(R.string.signup_phone)
            )
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                stringResource(R.string.signup_phone_why),
                style = BrandType.InterfaceAndGuidance.micro,
                color = palette.inkDim
            )

            Spacer(Modifier.height(BrandDimens.SpaceLg))
            SectionLabel(stringResource(R.string.signup_age_label))
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                // Derived from the engine, so the promise on this screen and the
                // rule that enforces it cannot drift apart.
                stringResource(
                    R.string.signup_age_body,
                    Eligibility.MINIMUM_PLAYING_AGE,
                    Eligibility.MINIMUM_CASH_AGE
                ),
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )

            Spacer(Modifier.height(BrandDimens.SpaceXl))
            BrandButton(label = stringResource(R.string.signup_cta), onClick = onSaved)
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = BrandDimens.MinTapTarget)
                    .clickable(onClick = onSignIn),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.signup_have_account),
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.accent
                )
            }
            Spacer(Modifier.height(BrandDimens.SpaceXl))
        }
    }
}

/**
 * A single-tap provider row.
 *
 * Outlined rather than filled: the filled button on this screen is the one that
 * saves the squad, and two loud buttons means neither is the next step (§13).
 */
@Composable
private fun ProviderButton(label: String, onClick: () -> Unit) {
    val palette = LocalBrandPalette.current
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = BrandDimens.MinTapTarget)
            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
            .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.ChipRadius))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = BrandType.InterfaceAndGuidance.title, color = palette.ink)
    }
}

/** A text field matching the search field on Choose Players, so the app has one. */
@Composable
private fun Field(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    val palette = LocalBrandPalette.current
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = BrandDimens.MinTapTarget)
            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
            .background(palette.raised)
            .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.ChipRadius))
            .padding(horizontal = BrandDimens.SpaceMd),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                placeholder,
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = BrandType.InterfaceAndGuidance.body.copy(
                color = palette.ink,
                fontFamily = BrandType.NigerianText
            ),
            cursorBrush = SolidColor(palette.accent),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
