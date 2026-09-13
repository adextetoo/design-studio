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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.SectionLabel
import ng.naijaleague.fantasy.ui.components.motifField
import ng.naijaleague.fantasy.ui.components.motifFor

/**
 * Three Pick — the weekly predictor (§15).
 *
 * Taken from what Formula 1 got right: the prediction game sits BESIDE the
 * fantasy game rather than inside it. Thirty seconds, no squad, no budget, no
 * season-long commitment, and it pays out every week. It is the on-ramp that
 * carries a casual fan into the deeper product, and the cleanest thing a
 * sponsor can attach to.
 *
 * The second question — name an away team that wins — is deliberately the hard
 * one. It is hard *because* the NPFL is a home-win league, which is the same
 * fact the Away Day Bonus is built on, so the predictor teaches the scoring
 * system's central idea without explaining it.
 *
 * Two pieces of copy here are compliance, not marketing, and must not be
 * trimmed: entry is free and prizes are sponsor-funded (what keeps this outside
 * gaming regulation, §06), and cash prizes are 18+ with a non-cash alternative
 * for younger winners (the core user now runs 16–60).
 */
@Composable
fun ThreePickScreen(onClose: () -> Unit) {
    val palette = LocalBrandPalette.current
    val matchOfTheWeek = SampleData.fixtures.first()

    Column(
        Modifier
            .fillMaxSize()
            .background(palette.ground)
            .motifField(motifFor(BrandSurface.NIGHT_PITCH))
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
            SectionLabel("${stringResource(R.string.three_pick)} · Gameweek ${SampleData.gameweekNumber}")
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                stringResource(R.string.three_pick_strap),
                style = BrandType.IdentityAndEditorial.display2,
                color = palette.ink
            )
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                stringResource(R.string.three_pick_window),
                style = BrandType.ScoreAndData.dataSmall,
                color = palette.inkDim
            )

            Spacer(Modifier.height(BrandDimens.SpaceXl))
            PickRow(1, stringResource(R.string.three_pick_q1), "Tap to choose a player")
            PickRow(2, stringResource(R.string.three_pick_q2), "Tap to choose a team")
            PickRow(
                3,
                stringResource(R.string.three_pick_q3),
                "${SampleData.club(matchOfTheWeek.homeClubId).name} v " +
                    SampleData.club(matchOfTheWeek.awayClubId).name
            )

            Spacer(Modifier.height(BrandDimens.SpaceXl))
            SectionLabel("Prizes")
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                stringResource(R.string.three_pick_prize_all),
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.ink
            )
            Spacer(Modifier.height(BrandDimens.SpaceXs))
            Text(
                stringResource(R.string.three_pick_prize_two),
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.ink
            )

            Spacer(Modifier.height(BrandDimens.SpaceLg))
            BrandRule()
            Spacer(Modifier.height(BrandDimens.SpaceLg))
            Text(
                stringResource(R.string.three_pick_free),
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.accent
            )
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                stringResource(R.string.three_pick_eligibility),
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )
            Spacer(Modifier.height(BrandDimens.SpaceXl))
        }

        Column(
            Modifier
                .background(palette.ground)
                .padding(BrandDimens.Gutter)
                .navigationBarsPadding()
        ) {
            BrandButton(label = stringResource(R.string.three_pick_cta), onClick = onClose)
        }
    }
}

@Composable
private fun PickRow(number: Int, question: String, hint: String) {
    val palette = LocalBrandPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = BrandDimens.SpaceMd)
            .clip(RoundedCornerShape(BrandDimens.CardRadius))
            .background(palette.raised)
            .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.CardRadius))
            .clickable { }
            .padding(BrandDimens.SpaceLg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(palette.accent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number.toString(),
                style = BrandType.ScoreAndData.data,
                color = palette.accentInk
            )
        }
        Spacer(Modifier.width(BrandDimens.SpaceMd))
        Column(Modifier.weight(1f)) {
            Text(question, style = BrandType.InterfaceAndGuidance.body, color = palette.ink)
            Spacer(Modifier.height(2.dp))
            Text(hint, style = BrandType.InterfaceAndGuidance.micro, color = palette.inkDim)
        }
    }
}
