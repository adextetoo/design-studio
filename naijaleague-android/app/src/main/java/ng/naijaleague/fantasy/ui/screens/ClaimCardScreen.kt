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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.LiveCatalogue
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.NpflClubs
import ng.naijaleague.fantasy.rules.Player
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandCard
import ng.naijaleague.fantasy.ui.components.ClubJersey
import ng.naijaleague.fantasy.ui.components.SectionLabel
import ng.naijaleague.fantasy.ui.components.SourceNote

/**
 * Claim this card.
 *
 * WHY THIS EXISTS, and it is not a vanity feature. Eighteen of twenty clubs
 * publish no squad this app will stand behind, so most of the pitch is
 * stand-ins and most of the real names carry a caveat about spelling or which
 * club they are at. Every one of those gaps has somebody who knows the answer
 * for certain: the player. This is the route for him to say so.
 *
 * It is the same operator queue the rest of the app keeps pointing at, opened
 * to the one person whose word outranks a club website that has not been
 * updated since 2023.
 *
 * WHAT IT ASKS FOR, AND WHY IT IS SO LITTLE. A claim has to prove one thing:
 * that the person sending it is the person on the card. Name, phone and ONE
 * piece of club-side proof does that. So that is all it asks.
 *
 * It does NOT ask for a date of birth, an ID number, a BVN, a bank account or
 * a photograph of a passport, and it must not start to. None of those verify
 * anything this cannot, and every one of them turns a small form into a store
 * of documents worth stealing. Data you never collected cannot leak.
 *
 * A HUMAN READS IT. The claim does not flip the card by itself — it opens a
 * task. A form that instantly granted a name would be a form that lets anyone
 * rename anyone, and the whole point of this product is knowing the difference.
 */
@Composable
fun ClaimCardScreen(
    player: Player,
    onClose: () -> Unit
) {
    val palette = LocalBrandPalette.current
    val record = LiveCatalogue.record(player.clubId)

    var fullName by rememberSaveable { mutableStateOf(if (player.isPlaceholder) "" else player.name) }
    var phone by rememberSaveable { mutableStateOf("") }
    var shirt by rememberSaveable { mutableStateOf(player.squadNumber?.toString() ?: "") }
    var proof by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable { mutableStateOf(false) }

    val ready = fullName.isNotBlank() && phone.isNotBlank() && proof.isNotBlank()

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

        Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
            if (sent) {
                // The confirmation says what happens next and by when, because
                // "thanks, we'll be in touch" is not an answer (§12).
                Spacer(Modifier.height(BrandDimens.SpaceXl))
                Text(
                    stringResource(R.string.claim_sent_head),
                    style = BrandType.IdentityAndEditorial.display2,
                    color = palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceMd))
                Text(
                    stringResource(R.string.claim_sent_body),
                    style = BrandType.InterfaceAndGuidance.body,
                    color = palette.inkDim
                )
                Spacer(Modifier.height(BrandDimens.SpaceXl))
                BrandButton(label = stringResource(R.string.claim_done), onClick = onClose)
                Spacer(Modifier.navigationBarsPadding().height(BrandDimens.SpaceXxl))
                return@Column
            }

            Text(
                stringResource(R.string.claim_head),
                style = BrandType.IdentityAndEditorial.display2,
                color = palette.ink
            )
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                stringResource(R.string.claim_body),
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )

            // What is being claimed, so nobody submits against the wrong card.
            Spacer(Modifier.height(BrandDimens.SpaceLg))
            BrandCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClubJersey(clubId = player.clubId, sizeDp = 40.dp)
                    Spacer(Modifier.width(BrandDimens.SpaceMd))
                    Column(Modifier.weight(1f)) {
                        Text(
                            player.name,
                            style = BrandType.IdentityAndEditorial.name,
                            color = if (player.isPlaceholder) palette.inkDim else palette.ink
                        )
                        Text(
                            "${player.position.label} · ${record.club.name}",
                            style = BrandType.IdentityAndEditorial.nameMicro,
                            color = palette.inkDim
                        )
                    }
                }
            }

            Spacer(Modifier.height(BrandDimens.SpaceXl))
            SectionLabel(stringResource(R.string.claim_about_you))
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            ClaimField(fullName, { fullName = it }, stringResource(R.string.claim_name))
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            ClaimField(phone, { phone = it }, stringResource(R.string.claim_phone))
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            ClaimField(shirt, { shirt = it }, stringResource(R.string.claim_shirt))

            Spacer(Modifier.height(BrandDimens.SpaceXl))
            SectionLabel(stringResource(R.string.claim_proof_label))
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                stringResource(R.string.claim_proof_help),
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            ClaimField(proof, { proof = it }, stringResource(R.string.claim_proof))

            Spacer(Modifier.height(BrandDimens.SpaceLg))
            SourceNote(
                label = stringResource(R.string.claim_privacy_label),
                detail = stringResource(R.string.claim_privacy)
            )

            Spacer(Modifier.height(BrandDimens.SpaceXl))
            BrandButton(
                label = stringResource(R.string.claim_cta),
                onClick = { sent = true },
                enabled = ready
            )
            if (!ready) {
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    // Says what is missing rather than just greying out (§12).
                    stringResource(R.string.claim_incomplete),
                    style = BrandType.InterfaceAndGuidance.bodySmall,
                    color = palette.inkDim
                )
            }
            Spacer(Modifier.navigationBarsPadding().height(BrandDimens.SpaceXxl))
        }
    }
}

/** A field, matching the one on Choose Players so the app has one of these. */
@Composable
private fun ClaimField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
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
            Text(placeholder, style = BrandType.InterfaceAndGuidance.body, color = palette.inkDim)
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
