package ng.naijaleague.fantasy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.NpflClubs
import ng.naijaleague.fantasy.data.NpflSquads

/**
 * Telling the manager what the app actually knows.
 *
 * WHY THESE EXIST. Only two of the twenty NPFL clubs publish a squad anybody can
 * source. The app could hide that and look finished, or say it and be trusted.
 * §12 settles it: the voice is someone who knows the league, and someone who
 * knows the league knows that nineteen club websites do not have a squad page.
 * Pretending otherwise is the one thing that would cost this product its only
 * real advantage over FPL.
 *
 * So: a stand-in is tagged on its row, every sourced squad can show where it
 * came from and what is wrong with it, and a club that is playing 600km from
 * home says so on the fixture. None of it is an apology. It is the same register
 * as a good match report — here is what we know, here is how we know it.
 */

/** Tone of a note, which decides its colour without inventing a new one. */
enum class NoteTone { NEUTRAL, CAUTION }

/**
 * A small caveat block: a rule above, label, then the detail.
 *
 * Deliberately quiet. A caveat that shouts competes with the scores, and the
 * manager came here for the scores.
 */
@Composable
fun SourceNote(
    label: String,
    detail: String,
    modifier: Modifier = Modifier,
    tone: NoteTone = NoteTone.NEUTRAL
) {
    val palette = LocalBrandPalette.current
    val tint = if (tone == NoteTone.CAUTION) palette.accent else palette.inkDim
    Column(modifier.fillMaxWidth()) {
        BrandRule()
        Spacer(Modifier.height(BrandDimens.SpaceSm))
        Row(Modifier.padding(horizontal = BrandDimens.Gutter)) {
            Box(
                Modifier
                    .width(2.dp)
                    .height(BrandDimens.SpaceLg)
                    .background(tint)
            )
            Spacer(Modifier.width(BrandDimens.SpaceSm))
            Column {
                Text(
                    label.uppercase(),
                    style = BrandType.InterfaceAndGuidance.label,
                    color = tint
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    detail,
                    style = BrandType.InterfaceAndGuidance.micro,
                    color = palette.inkDim
                )
            }
        }
        Spacer(Modifier.height(BrandDimens.SpaceSm))
    }
}

/**
 * The tag that goes on a stand-in's row.
 *
 * An outline and not a fill: a filled chip reads as a status somebody earned,
 * and this is the absence of information rather than a property of a player.
 */
@Composable
fun StandInTag(modifier: Modifier = Modifier) {
    val palette = LocalBrandPalette.current
    Box(
        modifier
            .clip(RoundedCornerShape(BrandDimens.ChipRadius))
            .border(1.dp, palette.rule, RoundedCornerShape(BrandDimens.ChipRadius))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text(
            stringResource(R.string.stand_in),
            style = BrandType.InterfaceAndGuidance.label,
            color = palette.inkDim
        )
    }
}

/** A real player's shirt number, as the club publishes it. */
@Composable
fun ShirtNumber(number: Int, modifier: Modifier = Modifier) {
    val palette = LocalBrandPalette.current
    Text(
        number.toString(),
        style = BrandType.ScoreAndData.dataSmall,
        color = palette.inkDim,
        modifier = modifier
    )
}

/**
 * One line of squad-data state for a club, ready to show under a player list.
 *
 * Returns null when there is nothing to say, so a caller can skip the block
 * rather than render an empty one.
 */
fun squadSourceLine(clubId: String): Pair<String, String>? {
    val source = NpflSquads.sourceFor(clubId) ?: return null
    return "Squad from ${source.description}" to
        "Read ${source.retrieved}. ${source.caveat}"
}

/**
 * The one-line explanation of why eighteen clubs are full of stand-ins.
 *
 * Counts are derived, not written down, so this line cannot go stale the day
 * somebody sources a third squad.
 */
val squadDataSummary: String
    get() {
        val sourced = NpflSquads.clubsWithVerifiedSquad.size
        val total = NpflClubs.all.size
        val real = NpflSquads.positioned().size
        val waiting = NpflSquads.awaitingPosition().size
        return "$sourced of $total clubs publish a squad we can stand behind — " +
            "$real real players so far. Everyone else is a labelled stand-in. " +
            "$waiting more players are confirmed by name but nobody prints what " +
            "position they play."
    }

/**
 * Kit colour for a club, or null when the app should fall back to its own mark.
 *
 * Null covers two different cases and treats them the same, which is correct:
 * no source found, and two sources naming different colours. Either way the
 * honest answer is the generated mark rather than a guess.
 */
fun kitColourOrNull(clubId: String): Color? {
    val kit = NpflClubs.record(clubId).kit
    return if (kit.renderable) Color(kit.primary!!) else null
}

/** Fixtures the table calls home games and the club does not. */
@Composable
fun NeutralGroundNote(clubId: String, modifier: Modifier = Modifier) {
    val record = NpflClubs.record(clubId)
    if (!record.playsHomeAtNeutralGround) return
    SourceNote(
        label = "Home, but not at home",
        detail = "${record.club.shortName} play this one at ${record.stadium}. " +
            "Their ground is ${record.homeGroundOfRecord}. Ground Man does not pay " +
            "out on it.",
        tone = NoteTone.CAUTION,
        modifier = modifier
    )
}
