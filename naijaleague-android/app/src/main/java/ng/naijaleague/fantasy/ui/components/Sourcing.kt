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

/**
 * A contained note: what the app knows, or does not, about what is on screen.
 *
 * A CARD, NOT A RAIL. This used to be a coloured bar down the left of some
 * loose text. Two things were wrong with it. A card already means "a separate
 * thing" in this system (§13) and a note IS one, so the rail was inventing a
 * second, weaker way to say the same thing — and the accent stripe made every
 * caveat look like an alert, when most of them are simply context. Cards are
 * how the rest of the app contains a block of prose, and now this matches.
 *
 * No tone parameter either. A note that needed to shout would be a violation
 * message, and those live on the control they block, not in a card.
 *
 * Takes a modifier rather than applying its own gutter, so it sits correctly
 * whether the caller is already padded or not — same contract as [BrandCard].
 */
@Composable
fun SourceNote(
    label: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalBrandPalette.current
    BrandCard(modifier.fillMaxWidth()) {
        Column {
            SectionLabel(label)
            Spacer(Modifier.height(BrandDimens.SpaceSm))
            Text(
                detail,
                style = BrandType.InterfaceAndGuidance.body,
                color = palette.inkDim
            )
        }
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
        modifier = modifier
    )
}
