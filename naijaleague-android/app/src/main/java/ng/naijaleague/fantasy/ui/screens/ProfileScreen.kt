package ng.naijaleague.fantasy.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.brand.NaijaLeagueTheme
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.ui.components.BrandBackdrop
import ng.naijaleague.fantasy.ui.components.BrandCard
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.OfflineBadge
import ng.naijaleague.fantasy.ui.components.SectionLabel
import ng.naijaleague.fantasy.ui.components.SourceNote
import ng.naijaleague.fantasy.ui.components.squadDataSummary

/**
 * Profile and settings.
 *
 * A SETTINGS LIST IS A TABLE, NOT A STACK OF CARDS (§13). Six rows of type on
 * the ground with a hairline between them, each at [BrandDimens.MinTapTarget].
 * Wrapping each one in a card would say "six separate objects you can act on"
 * about what is really one list, and it would cost roughly a third of the rows
 * on a 360dp screen for nothing.
 *
 * THE IDENTITY IS THE SQUAD, NOT A PERSON. This app has no reason to put a
 * manager's legal name on a screen, and inventing one for a demo is the same
 * mistake as inventing a player. So the profile shows the squad name, a handle
 * derived from it, and the two numbers that actually say how the season is
 * going. In the live build both come from the account; here the squad name is
 * borrowed from the sample mini-league so the rank and the total agree with the
 * table on the Leagues screen rather than contradicting it.
 *
 * TWO THINGS ARE HERE THAT A SETTINGS SCREEN USUALLY BURIES:
 *
 *  1. §05's offline promise, stated on the row it belongs to. "Saved on your
 *     phone · will sync" is a badge; a manager who wants to know what that
 *     actually means should not have to find the rules page to be told.
 *  2. What the app knows about squads. The Choose Players screen says it at the
 *     point of use, which is right, but a manager who has already built a squad
 *     and is now wondering why half of it is labelled STAND-IN goes looking in
 *     settings. Answering in one place and not the other is how a product
 *     ends up looking evasive about something it is actually being honest about.
 */

/**
 * The manager's own squad.
 *
 * Rank 5 of the sample mini-league, used for the squad name and the season
 * total so that this screen and the Leagues table cannot disagree. The manager
 * name and town on that row are deliberately NOT shown: this is the user's own
 * profile, and a stranger's name has no business standing in for theirs.
 */
private val mySquad = SampleData.miniLeague[4]

/** Derived, not written down, so it cannot drift from the squad name above it. */
private val myHandle: String =
    "@" + mySquad.squadName.lowercase().filter { it.isLetterOrDigit() }

@Composable
fun ProfileScreen(onClose: () -> Unit, onOpenGafferPass: () -> Unit) {
    // Pinned to Night Pitch rather than inheriting. This screen can be opened
    // from the Rules tab, which runs on Nzu Chalk, and settings are not reading
    // material — the chalk surface is reserved for things you actually read
    // (§01). Pinning also means the palette is the same whichever door was used.
    NaijaLeagueTheme(surface = BrandSurface.NIGHT_PITCH) {
        val palette = LocalBrandPalette.current

        BrandBackdrop(surface = BrandSurface.NIGHT_PITCH) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = BrandDimens.SpaceSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(BrandDimens.MinTapTarget)
                            .clip(CircleShape)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "‹",
                            style = BrandType.InterfaceAndGuidance.title,
                            color = palette.ink
                        )
                    }
                    Text(
                        "Profile",
                        style = BrandType.IdentityAndEditorial.display2,
                        color = palette.ink
                    )
                }

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ---- Who this is ----
                    Column(Modifier.padding(horizontal = BrandDimens.Gutter)) {
                        SectionLabel("Your squad")
                        Spacer(Modifier.height(BrandDimens.SpaceSm))
                        Text(
                            mySquad.squadName,
                            // Display 2's size in the NAME family. A squad name is
                            // typed by the manager and can carry Yoruba, Igbo or
                            // Hausa marks, and the display face has no glyphs for
                            // them (§02) — so the size comes off §02's scale and
                            // only the family changes. Taking the size from
                            // display2 rather than typing 28sp here means this
                            // cannot drift if the scale ever moves.
                            style = BrandType.IdentityAndEditorial.name.copy(
                                fontSize = BrandType.IdentityAndEditorial.display2.fontSize,
                                lineHeight = BrandType.IdentityAndEditorial.display2.lineHeight
                            ),
                            color = palette.ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(BrandDimens.SpaceXs))
                        Text(
                            myHandle,
                            style = BrandType.IdentityAndEditorial.nameMicro,
                            color = palette.inkDim
                        )
                        Spacer(Modifier.height(BrandDimens.SpaceLg))

                        // Rank and total, in the data class. Both read with
                        // thousands separators — 128432 is a number you have to
                        // stop and count, and nobody stops to count their rank.
                        Row(horizontalArrangement = Arrangement.spacedBy(BrandDimens.SpaceMd)) {
                            BrandCard(Modifier.weight(1f)) {
                                Column {
                                    SectionLabel("Overall rank", color = palette.inkDim)
                                    Spacer(Modifier.height(BrandDimens.SpaceXs))
                                    Text(
                                        formatThousands(SampleData.overallRank),
                                        style = BrandType.ScoreAndData.scorelineMid,
                                        color = palette.ink
                                    )
                                    Text(
                                        "of ${formatThousands(SampleData.totalManagers)}",
                                        style = BrandType.InterfaceAndGuidance.micro,
                                        color = palette.inkDim
                                    )
                                }
                            }
                            BrandCard(Modifier.weight(1f)) {
                                Column {
                                    SectionLabel("Total points", color = palette.inkDim)
                                    Spacer(Modifier.height(BrandDimens.SpaceXs))
                                    Text(
                                        formatThousands(mySquad.totalPoints),
                                        style = BrandType.ScoreAndData.scorelineMid,
                                        color = palette.ink
                                    )
                                    Text(
                                        "after ${SampleData.gameweekNumber} gameweeks",
                                        style = BrandType.InterfaceAndGuidance.micro,
                                        color = palette.inkDim
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(BrandDimens.SpaceLg))
                        // The sync state belongs next to the identity it protects,
                        // and it is a state rather than a failure (§13).
                        OfflineBadge()
                        Spacer(Modifier.height(BrandDimens.SpaceXl))
                        SectionLabel("Settings")
                        Spacer(Modifier.height(BrandDimens.SpaceSm))
                    }

                    BrandRule()

                    SettingsRow(
                        label = "Edit profile",
                        detail = "Squad name, club you support, handle.",
                        // The integrator wires these; the screen only declares
                        // that the row is a destination.
                        onClick = {}
                    )
                    SettingsRow(
                        label = "Notifications",
                        detail = "Deadline reminders, live goals, the Monday receipt.",
                        onClick = {}
                    )
                    SettingsRow(
                        label = "Data & offline",
                        // §05's promise, stated where somebody would look for it
                        // rather than only on the rules page.
                        detail = stringResource(R.string.settings_offline_note),
                        onClick = {}
                    )
                    SettingsRow(
                        label = stringResource(R.string.gaffer_pass),
                        detail = stringResource(R.string.gaffer_promo),
                        // Brass, because §01 gives brass to honours and names
                        // Gaffer Pass as one of them. It is the only brass on
                        // this screen; nothing else here was won.
                        labelColor = palette.honours,
                        onClick = onOpenGafferPass
                    )
                    SettingsRow(
                        label = "Help & support",
                        detail = "Flag a wrong score, ask about a rule, reach a human.",
                        onClick = {}
                    )
                    SettingsRow(
                        label = "Sign out",
                        // Class 3 is never funny, and least of all on the row
                        // that ends a session. Say what happens and what it
                        // costs to come back (§12).
                        detail = "Your squad stays saved. You will need your number and one " +
                            "code to get back in.",
                        onClick = {}
                    )

                    Spacer(Modifier.height(BrandDimens.SpaceXl))

                    // ---- What the app actually knows about squads ----
                    SourceNote(
                        label = stringResource(R.string.squad_data_label),
                        detail = squadDataSummary
                    )

                    Spacer(Modifier.height(BrandDimens.SpaceXxl))
                }
            }
        }
    }
}

/**
 * One settings row.
 *
 * The chevron is the whole affordance — no switches, no inline expansion. A row
 * that opens something and a row that toggles something should not look
 * identical, and everything on this list opens something.
 */
@Composable
private fun SettingsRow(
    label: String,
    detail: String,
    onClick: () -> Unit,
    labelColor: Color? = null
) {
    val palette = LocalBrandPalette.current
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = BrandDimens.MinTapTarget)
                .clickable(onClick = onClick)
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    style = BrandType.InterfaceAndGuidance.body,
                    color = labelColor ?: palette.ink
                )
                Spacer(Modifier.height(BrandDimens.SpaceXs))
                Text(
                    detail,
                    style = BrandType.InterfaceAndGuidance.bodySmall,
                    color = palette.inkDim
                )
            }
            Spacer(Modifier.width(BrandDimens.SpaceMd))
            Text(
                "›",
                style = BrandType.InterfaceAndGuidance.title,
                color = palette.inkDim
            )
        }
        BrandRule(Modifier.padding(start = BrandDimens.Gutter))
    }
}
