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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.ui.components.BrandBackdrop
import ng.naijaleague.fantasy.ui.components.BrandButton
import ng.naijaleague.fantasy.ui.components.BrandButtonSecondary
import ng.naijaleague.fantasy.ui.components.BrandCard
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.NoteTone
import ng.naijaleague.fantasy.ui.components.SectionLabel
import ng.naijaleague.fantasy.ui.components.SourceNote

/**
 * Where a manager joins a league. Not the table — the table is [LeaguesScreen].
 *
 * §08 DECIDES THE LAYOUT, AND IT DECIDES IT AGAINST THE OBVIOUS ONE. The obvious
 * build is a directory: a big searchable list of public leagues with the private
 * code tucked in a corner. But the mini-league is the product's retention
 * mechanic precisely because of *who* is in it, and a league of 31,562 strangers
 * in Lagos retains nobody. So the invitation comes first and full width, the
 * public directory comes second under a line that says plainly what it is worth,
 * and the filter opens on Friends rather than Global — the one tab where the
 * names in the table are names the manager can shout at on Monday.
 *
 * WHY THE CODE FIELD IS NOT IN A CARD. §13 gives a card the meaning "a separate
 * thing you can act on"; the invitation block is one, and gets a card. A form
 * is type on the ground. It is also how the field keeps its contrast — the
 * search field this is styled after sits on `raised` against `ground`, and
 * putting the same field inside a `raised` card would leave only its hairline
 * border to say it is a field at all.
 *
 * THE MANAGER COUNTS ARE DEMO FIGURES and the screen says so on itself, in the
 * same [SourceNote] the squad screens use. `docs/SOURCES.md` is the standard:
 * the app states what it knows and how it knows it. A count that looks like
 * league data and is not would be the same lie as an invented player name,
 * just in a column instead of a row.
 */
@Composable
fun JoinLeaguesScreen(onClose: () -> Unit) {
    val palette = LocalBrandPalette.current

    // Friends, not Global. See the note above — this default is the §08
    // argument expressed as a line of state rather than a paragraph of copy.
    var filter by rememberSaveable { mutableStateOf(JoinFilter.FRIENDS) }
    var code by rememberSaveable { mutableStateOf("") }

    val typed = code.trim()
    // §12: a blocking message says what is wrong AND what to do. It also stays
    // quiet until there is something to block — a wrong-length warning on an
    // empty field is a telling-off for having not started yet.
    val showCodeProblem = typed.isNotEmpty() && typed.length != LEAGUE_CODE_LENGTH

    val shown = joinableLeagues.filter { it.scope == filter }

    BrandBackdrop(surface = BrandSurface.NIGHT_PITCH) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            // Back stays out of the scrolling content: a manager who opened this
            // by accident should not have to scroll to get out of it.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BrandDimens.Gutter)
                    .padding(top = BrandDimens.SpaceLg, bottom = BrandDimens.SpaceMd),
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
                Column(Modifier.weight(1f)) {
                    Text(
                        "Join a league",
                        style = BrandType.IdentityAndEditorial.display2,
                        color = palette.ink
                    )
                    Text(
                        "Gameweek ${SampleData.gameweekNumber}",
                        style = BrandType.ScoreAndData.dataSmall,
                        color = palette.inkDim
                    )
                }
            }

            LazyColumn(Modifier.weight(1f)) {

                // ---- 1. The invitation. First, widest, and the only lime fill. ----
                item {
                    Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                        BrandCard {
                            Column {
                                SectionLabel("Your people")
                                Spacer(Modifier.height(BrandDimens.SpaceSm))
                                Text(
                                    stringResource(R.string.join_head),
                                    style = BrandType.InterfaceAndGuidance.title,
                                    color = palette.ink
                                )
                                Spacer(Modifier.height(BrandDimens.SpaceXs))
                                Text(
                                    stringResource(R.string.join_body),
                                    style = BrandType.InterfaceAndGuidance.body,
                                    color = palette.inkDim
                                )
                                Spacer(Modifier.height(BrandDimens.SpaceLg))
                                // The same action, and the same words, as the
                                // prompt on the league table and the last step
                                // of onboarding. One invitation, one label.
                                BrandButton(
                                    label = stringResource(R.string.onb5_share),
                                    onClick = onClose
                                )
                            }
                        }
                    }
                }

                // ---- 2. The private code. A form, so: type on the ground. ----
                item {
                    Column(
                        Modifier
                            .padding(horizontal = BrandDimens.Gutter)
                            .padding(top = BrandDimens.SpaceXl)
                    ) {
                        SectionLabel(stringResource(R.string.join_code_label))
                        Spacer(Modifier.height(BrandDimens.SpaceSm))
                        Text(
                            stringResource(R.string.join_code_help),
                            style = BrandType.InterfaceAndGuidance.body,
                            color = palette.inkDim
                        )
                        Spacer(Modifier.height(BrandDimens.SpaceMd))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = BrandDimens.MinTapTarget)
                                .clip(RoundedCornerShape(BrandDimens.ChipRadius))
                                .background(palette.raised)
                                .border(
                                    1.dp,
                                    if (showCodeProblem) palette.negative else palette.rule,
                                    RoundedCornerShape(BrandDimens.ChipRadius)
                                )
                                .padding(horizontal = BrandDimens.SpaceMd),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (code.isEmpty()) {
                                Text(
                                    stringResource(R.string.join_code_hint),
                                    style = BrandType.InterfaceAndGuidance.body,
                                    color = palette.inkDim
                                )
                            }
                            BasicTextField(
                                value = code,
                                // Codes are printed and read aloud in group
                                // chats; nobody should lose ten minutes to a
                                // lowercase o. Case is taken out of the problem
                                // rather than explained away in help text.
                                onValueChange = { code = it.uppercase() },
                                singleLine = true,
                                textStyle = BrandType.InterfaceAndGuidance.body.copy(
                                    color = palette.ink,
                                    fontFamily = BrandType.NigerianText
                                ),
                                cursorBrush = SolidColor(palette.accent),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (showCodeProblem) {
                            Spacer(Modifier.height(BrandDimens.SpaceSm))
                            Text(
                                stringResource(R.string.join_code_wrong),
                                style = BrandType.InterfaceAndGuidance.body,
                                color = palette.negative
                            )
                        }
                        Spacer(Modifier.height(BrandDimens.SpaceMd))
                        // Secondary, deliberately. Both actions on this block
                        // get somebody into a private league, and the loud one
                        // is already spent on the invitation above. Two lime
                        // fills on one screen would mean neither is the answer.
                        BrandButtonSecondary(
                            label = stringResource(R.string.join_code_cta),
                            onClick = onClose
                        )
                    }
                }

                // ---- 3. The directory, second and honest about itself. ----
                item {
                    Column(
                        Modifier
                            .padding(horizontal = BrandDimens.Gutter)
                            .padding(top = BrandDimens.SpaceXl, bottom = BrandDimens.SpaceMd)
                    ) {
                        SectionLabel("Open leagues")
                        Spacer(Modifier.height(BrandDimens.SpaceXs))
                        Text(
                            stringResource(R.string.join_browse_note),
                            style = BrandType.InterfaceAndGuidance.body,
                            color = palette.inkDim
                        )
                    }
                }

                item {
                    // The filter pattern from LeaguesScreen, unchanged: one row
                    // of chips, accent when live, MinTapTarget whatever the
                    // label does.
                    Row(
                        Modifier.padding(horizontal = BrandDimens.Gutter),
                        horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceSm)
                    ) {
                        JoinFilter.entries.forEach { candidate ->
                            val active = candidate == filter
                            Box(
                                Modifier
                                    .heightIn(min = BrandDimens.MinTapTarget)
                                    .clip(RoundedCornerShape(BrandDimens.ChipRadius))
                                    .background(if (active) palette.accent else palette.raised)
                                    .clickable { filter = candidate }
                                    .padding(horizontal = BrandDimens.SpaceLg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    candidate.label,
                                    style = BrandType.InterfaceAndGuidance.label,
                                    color = if (active) palette.accentInk else palette.inkDim
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(BrandDimens.SpaceLg))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = BrandDimens.Gutter,
                                vertical = BrandDimens.SpaceSm
                            )
                    ) {
                        Text(
                            "LEAGUE",
                            style = BrandType.InterfaceAndGuidance.label,
                            color = palette.inkDim,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "MANAGERS",
                            style = BrandType.InterfaceAndGuidance.label,
                            color = palette.inkDim,
                            modifier = Modifier.width(CountColumn),
                            textAlign = TextAlign.End
                        )
                        // Holds the header off the action column so the figures
                        // sit under the word that names them.
                        Spacer(Modifier.width(BrandDimens.SpaceMd + ActionColumn))
                    }
                    BrandRule()
                }

                items(shown, key = { it.name }) { league ->
                    JoinRow(league)
                }

                item {
                    SourceNote(
                        label = stringResource(R.string.join_counts_label),
                        detail = stringResource(R.string.join_counts_note),
                        tone = NoteTone.CAUTION
                    )
                    Spacer(Modifier.navigationBarsPadding().height(BrandDimens.SpaceXxl))
                }
            }
        }
    }
}

/**
 * The three filters. Order is Global first because that is the order the
 * product speaks in; the *default* is Friends, which is the order §08 cares
 * about. Those are two different decisions and only one of them is visible.
 */
private enum class JoinFilter(val label: String) {
    GLOBAL("Global"), FRIENDS("Friends"), WORK("Work")
}

/** Six, and stated once so the field, the help line and the error cannot drift. */
private const val LEAGUE_CODE_LENGTH = 6

// Column widths, as LeaguesScreen sets them: layout arithmetic rather than
// brand spacing, so they are local to the table they align. Sized for the
// widest figure this screen can render — the overall league's seven digits.
private val CountColumn = 80.dp
private val ActionColumn = 56.dp

/**
 * A league somebody could join.
 *
 * [managers] IS A DEMO FIGURE. There is no entry count to read until there are
 * entries, and the screen carries a note saying exactly that rather than
 * dressing a plausible number as a fact.
 */
private data class JoinableLeague(
    val name: String,
    val scope: JoinFilter,
    val managers: Int,
    /** One line on what this league actually is, in the manager's own register. */
    val blurb: String,
    /** True for the one league a squad lands in without asking. */
    val alreadyIn: Boolean = false
)

/**
 * The demo directory.
 *
 * THE NAMES ARE WRITTEN THE WAY PEOPLE WRITE THEM, which is the register
 * `SampleData.miniLeague` already sets with its squad names — a joke, a place,
 * a shared room, an office floor. A directory full of "Nigeria Premier League
 * Fantasy Group 4" would be a directory nobody in this audience recognises as
 * theirs.
 *
 * The overall league is the exception and is deliberately not joinable: every
 * squad is in it the moment it is saved, so offering a Join button would be the
 * app asking for something it already has.
 */
private val joinableLeagues = listOf(
    JoinableLeague(
        name = "NaijaLeague Overall",
        scope = JoinFilter.GLOBAL,
        managers = SampleData.totalManagers,
        blurb = "Everybody who builds a squad is in this one.",
        alreadyIn = true
    ),
    JoinableLeague(
        name = "Enyimba Faithful",
        scope = JoinFilter.GLOBAL,
        managers = 12_408,
        blurb = "Aba people, and everybody who claims Aba."
    ),
    JoinableLeague(
        name = "Lagos Managers",
        scope = JoinFilter.GLOBAL,
        managers = 31_562,
        blurb = "One state. Far too many opinions."
    ),
    JoinableLeague(
        name = "Naija Abroad",
        scope = JoinFilter.GLOBAL,
        managers = 8_914,
        blurb = "Watching the 4pm kickoff on somebody else's clock."
    ),

    JoinableLeague(
        name = "The Boys From Surulere",
        scope = JoinFilter.FRIENDS,
        managers = 14,
        blurb = "Started in the group chat. Open until Sunday."
    ),
    JoinableLeague(
        name = "Unilag 2016 Set",
        scope = JoinFilter.FRIENDS,
        managers = 41,
        blurb = "Same set, same arguments, now with a table."
    ),
    JoinableLeague(
        name = "Room 12 Legends",
        scope = JoinFilter.FRIENDS,
        managers = 17,
        blurb = "Hostel room first. Group chat ever since."
    ),
    JoinableLeague(
        name = "Cousins Only",
        scope = JoinFilter.FRIENDS,
        managers = 9,
        blurb = "No uncles. They were asked and they said no."
    ),

    JoinableLeague(
        name = "Head Office vs Branch",
        scope = JoinFilter.WORK,
        managers = 38,
        blurb = "Two buildings, one argument, settled weekly."
    ),
    JoinableLeague(
        name = "Oga Is In This One",
        scope = JoinFilter.WORK,
        managers = 16,
        blurb = "Which is why nobody is saying anything."
    ),
    JoinableLeague(
        name = "Night Shift Managers",
        scope = JoinFilter.WORK,
        managers = 21,
        blurb = "For people who watch the highlights at 2am."
    ),
    JoinableLeague(
        name = "Third Floor Only",
        scope = JoinFilter.WORK,
        managers = 12,
        blurb = "Twelve desks. Twelve managers."
    )
)

/**
 * One row of the directory.
 *
 * A row and not a card (§13): this is a list to scan, and fifteen cards is a
 * scroll where fifteen rows is a glance. The league name goes through
 * [BrandType.IdentityAndEditorial.name] — that class is Noto-backed, and a
 * league called "Ọmọ Ìbàdàn" has to survive being typed by the person who
 * named it.
 */
@Composable
private fun JoinRow(league: JoinableLeague) {
    val palette = LocalBrandPalette.current
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = BrandDimens.SquadRowHeight)
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    league.name,
                    style = BrandType.IdentityAndEditorial.name,
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    league.blurb,
                    style = BrandType.InterfaceAndGuidance.micro,
                    color = palette.inkDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(BrandDimens.SpaceSm))
            Text(
                formatThousands(league.managers),
                style = BrandType.ScoreAndData.data,
                color = palette.ink,
                modifier = Modifier.width(CountColumn),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.width(BrandDimens.SpaceMd))
            if (league.alreadyIn) {
                // Not a button and not brass. Being in the overall league is a
                // state every squad has by default, and §01 keeps brass for
                // things that were won.
                Text(
                    "JOINED",
                    style = BrandType.InterfaceAndGuidance.label,
                    color = palette.inkDim,
                    modifier = Modifier.width(ActionColumn),
                    textAlign = TextAlign.End
                )
            } else {
                Box(
                    Modifier
                        .width(ActionColumn)
                        .heightIn(min = BrandDimens.MinTapTarget)
                        .clip(RoundedCornerShape(BrandDimens.ChipRadius))
                        .background(palette.raised)
                        .border(
                            1.dp,
                            palette.accent.copy(alpha = 0.55f),
                            RoundedCornerShape(BrandDimens.ChipRadius)
                        )
                        .clickable { /* wired to the leagues view-model in the live build */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Join",
                        style = BrandType.InterfaceAndGuidance.label,
                        color = palette.accent
                    )
                }
            }
        }
        BrandRule(Modifier.padding(start = BrandDimens.Gutter))
    }
}
