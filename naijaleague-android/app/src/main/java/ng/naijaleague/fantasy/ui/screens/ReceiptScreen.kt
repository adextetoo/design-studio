package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandColor
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.rules.Scoring
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandButtonSecondary
import ng.naijaleague.fantasy.ui.components.SectionLabel

/**
 * The Receipt (§04).
 *
 * The Monday morning share card — the campaign's central asset and the only
 * place in the product where the campaign line "Table no dey lie" appears. §04
 * builds it automatically at 9am Monday; this screen is the preview a manager
 * sees before sending it.
 *
 * It is one of Class 4's four permitted moments (§02), which is why the
 * celebration treatment is allowed here and almost nowhere else.
 *
 * Two constraints shape the layout, both from §03's screenshot rules: assume
 * every one of these images is re-shared into a WhatsApp group at terrible
 * compression, and assume it is read in a thumbnail. So: enormous type, hard
 * contrast, no thin lines, no long sentences, and a fixed aspect ratio (4:5,
 * which is what X and Instagram will not crop).
 */
@Composable
fun ReceiptScreen(onClose: () -> Unit) {
    val palette = LocalBrandPalette.current

    // The manager's own total comes from the engine, never from a stored value.
    val myPoints = remember {
        Scoring.scoreSquad(SampleData.squad, SampleData.gameweek12, SampleData.activeChip).points
    }
    val rival = remember { SampleData.miniLeague.first { it.gameweekPoints < myPoints } }

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
            Text(
                "Your receipt",
                style = BrandType.InterfaceAndGuidance.title,
                color = palette.ink
            )
        }

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = BrandDimens.Gutter),
            verticalArrangement = Arrangement.Center
        ) {
            ReceiptCard(
                gameweek = SampleData.gameweekNumber,
                myName = "You",
                myPoints = myPoints,
                rivalName = rival.manager,
                rivalPoints = rival.gameweekPoints
            )
        }

        Column(
            Modifier
                .padding(BrandDimens.Gutter)
                .navigationBarsPadding()
        ) {
            BrandButton(label = "Share to WhatsApp", onClick = onClose)
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            BrandButtonSecondary(label = "Save image", onClick = onClose)
        }
    }
}

/**
 * The card itself, at 4:5. Kept as its own composable because the same layout
 * is rendered off-screen to a bitmap for sharing — what the manager previews
 * and what lands in the group chat must be the same object.
 */
@Composable
fun ReceiptCard(
    gameweek: Int,
    myName: String,
    myPoints: Int,
    rivalName: String,
    rivalPoints: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(BrandDimens.CardRadius))
            .background(BrandColor.NightPitch)
            // The exported image is standalone, but in preview the card sits on
            // the same Night Pitch ground — without an edge it reads as part of
            // the screen rather than as the object being shared.
            .border(
                1.dp,
                BrandColor.JaraLime.copy(alpha = 0.25f),
                RoundedCornerShape(BrandDimens.CardRadius)
            )
            .padding(BrandDimens.SpaceXl),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SectionLabel("Gameweek $gameweek")

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            ScoreColumn(myName, myPoints, winner = myPoints >= rivalPoints)
            ScoreColumn(rivalName, rivalPoints, winner = rivalPoints > myPoints)
        }

        Column {
            // The campaign line. §16 calls it "the memorable, ownable,
            // unmistakably Nigerian half of this brand" now that the name is
            // descriptive — and this card is where it earns that.
            Text(
                "TABLE NO\nDEY LIE.",
                style = BrandType.CelebrationAndMotion.celebration,
                color = BrandColor.JaraLime
            )
            Spacer(Modifier.height(BrandDimens.SpaceMd))
            Text(
                stringResource(R.string.app_name),
                style = BrandType.InterfaceAndGuidance.label,
                color = BrandColor.HarmattanHaze
            )
        }
    }
}

@Composable
private fun ScoreColumn(name: String, points: Int, winner: Boolean) {
    Column {
        Text(
            name.uppercase(),
            style = BrandType.InterfaceAndGuidance.label,
            color = BrandColor.HarmattanHaze
        )
        Spacer(Modifier.height(BrandDimens.SpaceXs))
        Text(
            points.toString(),
            style = BrandType.ScoreAndData.scoreline,
            color = if (winner) BrandColor.JaraLime else BrandColor.HarmattanHaze,
            textAlign = TextAlign.Start
        )
    }
}
