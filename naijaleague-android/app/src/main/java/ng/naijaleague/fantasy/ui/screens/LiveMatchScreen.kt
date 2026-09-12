package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.ui.components.BrandCard
import ng.naijaleague.fantasy.ui.components.ClubBadge
import ng.naijaleague.fantasy.ui.components.SectionLabel
import ng.naijaleague.fantasy.ui.components.StatBar

/**
 * Live match.
 *
 * The one place Live Green belongs: "something is happening right now" (§01).
 * The scoreline is the hero number; stats sit beneath in the data class.
 */
@Composable
fun LiveMatchScreen(onClose: () -> Unit) {
    val palette = LocalBrandPalette.current
    val fixture = SampleData.liveFixture
    val home = SampleData.club(fixture.homeClubId)
    val away = SampleData.club(fixture.awayClubId)

    Column(
        Modifier
            .fillMaxSize()
            .background(palette.ground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceMd),
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
            Box(
                Modifier
                    .background(BrandColor.LiveGreen, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    "LIVE",
                    style = BrandType.InterfaceAndGuidance.label,
                    color = BrandColor.NightPitch
                )
            }
            Spacer(Modifier.width(BrandDimens.SpaceSm))
            Text(
                "${fixture.minute}'",
                style = BrandType.ScoreAndData.dataSmall,
                color = palette.accent
            )
        }

        // ---- Scoreline ----
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                ClubBadge(home.shortName, 44)
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    home.name,
                    style = BrandType.IdentityAndEditorial.name,
                    color = palette.ink,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Home",
                    style = BrandType.InterfaceAndGuidance.label,
                    color = palette.inkDim
                )
            }
            Text(
                "${fixture.homeScore}-${fixture.awayScore}",
                style = BrandType.ScoreAndData.scorelineMid,
                color = palette.ink,
                modifier = Modifier.padding(horizontal = BrandDimens.SpaceMd)
            )
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                ClubBadge(away.shortName, 44)
                Spacer(Modifier.height(BrandDimens.SpaceSm))
                Text(
                    away.name,
                    style = BrandType.IdentityAndEditorial.name,
                    color = palette.ink,
                    textAlign = TextAlign.Center
                )
                // Away is flagged everywhere in this app, because away is worth more.
                // Lime: this is a scoring gain, not an honour.
                Text(
                    "Away · bonus live",
                    style = BrandType.InterfaceAndGuidance.label,
                    color = palette.accent
                )
            }
        }

        Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
            SectionLabel("Match stats")
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            SampleData.liveStats.forEach { stat ->
                StatBar(label = stat.label, home = stat.home, away = stat.away)
                Spacer(Modifier.height(BrandDimens.SpaceLg))
            }

            Spacer(Modifier.height(BrandDimens.SpaceSm))
            BrandCard {
                Column {
                    SectionLabel("The Three · provisional")
                    Spacer(Modifier.height(BrandDimens.SpaceSm))
                    Text(
                        "Filed by the scorer at the ground. Every award comes with the reason, " +
                            "and you have two hours after full time to flag it with the clip.",
                        style = BrandType.InterfaceAndGuidance.body,
                        color = palette.inkDim
                    )
                }
            }
            Spacer(Modifier.height(BrandDimens.SpaceXxl))
        }
    }
}
