package ng.naijaleague.fantasy.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import ng.naijaleague.fantasy.R
import ng.naijaleague.fantasy.brand.BrandDimens
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.BrandType
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.brand.NaijaLeagueTheme
import ng.naijaleague.fantasy.data.NpflClubs
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.rules.Scoring
import ng.naijaleague.fantasy.rules.SquadRules
import ng.naijaleague.fantasy.ui.components.BrandBackdrop
import ng.naijaleague.fantasy.ui.components.BrandRule
import ng.naijaleague.fantasy.ui.components.SectionLabel

/**
 * Notifications.
 *
 * A LIST, NOT A FEED OF CARDS (§13). Six rows of type on the ground, newest at
 * the top, each one saying what happened, what it means and how long ago. This
 * audience reads tables for pleasure; they do not need one notification per
 * card with air around it.
 *
 * UNREAD IS NEVER SIGNALLED BY COLOUR ALONE. Roughly one man in twelve cannot
 * separate two of our greens, and "the green ones are new" would be a state
 * that simply does not exist for him. So an unread row carries three signals at
 * once: a filled dot in the left column, a heavier headline, and full-strength
 * ink against the dimmed ink of a row already read. Any one of them read alone
 * is enough.
 *
 * THE DEADLINE ROW IS THE ONE THAT MATTERS. §13 calls the deadline the clock
 * the whole product is organised around, and the strip that carries it is
 * permanent furniture. A notification about it is the same clock arriving when
 * the manager is not looking at the app, so it is sorted to the top, it states
 * the time and the consequence, and it does not make a joke. It also reads its
 * final-hour state from [SampleData.hoursToDeadline] — the same source the
 * strip reads — so the app can never be relaxed in one place and coral in
 * another about the same minute.
 *
 * The model is private to this screen on purpose: notifications are a delivery
 * concern, not league data, and nothing in `data/` should grow a dependency on
 * how this screen happens to display them today.
 */
private enum class NotificationKind(val label: String) {
    DEADLINE("Deadline"),
    LIVE("Live"),
    POINTS("Points"),
    TRANSFER("Transfer"),
    INVITE("League invite"),
    RECEIPT("Receipt")
}

private data class Notification(
    val id: String,
    val kind: NotificationKind,
    val headline: String,
    val detail: String,
    /** Age in minutes. Sort order is derived from this, never hand-written. */
    val minutesAgo: Int,
    val unread: Boolean
)

/**
 * "2m ago", "1h ago", "2d ago".
 *
 * Truncated rather than rounded, deliberately: a notification that says "1h
 * ago" when it is 1h 55m old is annoying, and one that says "2h ago" when it is
 * 1h 5m old is wrong about the gameweek it belongs to.
 */
private fun relativeTime(minutesAgo: Int): String = when {
    minutesAgo < 60 -> "${minutesAgo}m ago"
    minutesAgo < 60 * 24 -> "${minutesAgo / 60}h ago"
    else -> "${minutesAgo / (60 * 24)}d ago"
}

/**
 * The sample feed.
 *
 * Composable because two of these lines are owned by strings.xml and read
 * through [stringResource] rather than retyped — copy that states a rule lives
 * in one place or it drifts.
 *
 * Every club here comes from [NpflClubs] and every player from [SampleData],
 * which is the point: a notification naming a club that is not in the 2026/27
 * NPFL is exactly the mistake this product cannot make, and it is far easier to
 * make in a notification than on a fixture list.
 */
@Composable
private fun sampleNotifications(): List<Notification> {
    val live = SampleData.liveFixture
    // Counted off the squad rather than asserted, and counted off the XI rather
    // than the fifteen — a manager reading "3 of your players are on" and then
    // finding one of them on the bench stops believing the next one.
    val onThePitch = SampleData.squad.allPlayers.count {
        it.id in SampleData.squad.startingIds &&
            (it.clubId == live.homeClubId || it.clubId == live.awayClubId)
    }
    val signing = SampleData.player("IKO-nnoli")
    val signingClub = SampleData.club(signing.clubId)
    val fromClub = SampleData.squad.allPlayers.count { it.clubId == signing.clubId }
    val points = remember {
        Scoring.scoreSquad(SampleData.squad, SampleData.gameweek12, SampleData.activeChip).points
    }
    val inviter = SampleData.miniLeague.first()

    return listOf(
        Notification(
            id = "deadline",
            kind = NotificationKind.DEADLINE,
            headline = "Gameweek ${SampleData.gameweekNumber} deadline: " +
                SampleData.deadlineLabel,
            detail = "Your squad locks then. Transfers, captain and bench order after " +
                "that count for Gameweek ${SampleData.gameweekNumber + 1}, not this one.",
            minutesAgo = 2,
            unread = true
        ),
        Notification(
            id = "live",
            kind = NotificationKind.LIVE,
            headline = "${NpflClubs.name(live.homeClubId)} ${live.homeScore}-" +
                "${live.awayScore} ${NpflClubs.name(live.awayClubId)} · ${live.minute}'",
            detail = "$onThePitch of your XI are in this one, at ${live.venueName}.",
            minutesAgo = 14,
            unread = true
        ),
        Notification(
            id = "points",
            kind = NotificationKind.POINTS,
            // Scored by the engine, like every other points figure in the app,
            // so a notification cannot quote a total the squad screen disagrees
            // with.
            headline = "Gameweek ${SampleData.gameweekNumber}: you are on $points",
            detail = stringResource(R.string.provisional),
            minutesAgo = 62,
            unread = true
        ),
        Notification(
            id = "transfer",
            kind = NotificationKind.TRANSFER,
            // A sourced Ikorodu City player, from the club's own players page —
            // not a stand-in. A notification is the one place a stand-in cannot
            // carry its STAND-IN tag with it, so only real names go in one.
            headline = "Transfer confirmed: ${signing.name} is in",
            detail = "${signingClub.shortName} defender, in for Gameweek " +
                "${SampleData.gameweekNumber}. That is $fromClub from " +
                "${signingClub.shortName} — the cap is ${SquadRules.MAX_PER_CLUB}.",
            minutesAgo = 185,
            unread = false
        ),
        Notification(
            id = "invite",
            kind = NotificationKind.INVITE,
            headline = "${inviter.manager} invited you to a mini-league",
            detail = "${SampleData.miniLeague.size} managers already in. Your points " +
                "count from the gameweek you join.",
            minutesAgo = 1_490,
            unread = false
        ),
        Notification(
            id = "receipt",
            kind = NotificationKind.RECEIPT,
            headline = "Your Gameweek ${SampleData.gameweekNumber - 1} receipt is ready",
            detail = "Built Monday at 9am, like always. Send it before the group chat " +
                "moves on.",
            minutesAgo = 2_950,
            unread = false
        )
    )
}

@Composable
fun NotificationsScreen(onClose: () -> Unit) {
    // Pinned to Night Pitch rather than inherited, for the same reason as the
    // profile: this screen can be opened from the chalk-surfaced rules tab, and
    // chalk is the reading surface, not a light mode (§01).
    NaijaLeagueTheme(surface = BrandSurface.NIGHT_PITCH) {
        val palette = LocalBrandPalette.current
        val all = sampleNotifications()
        // Newest first, from the age each entry carries. Ordering the literal
        // list by hand would be one more thing that can silently go wrong.
        val feed = all.sortedBy { it.minutesAgo }
        val unread = all.count { it.unread }
        // The same clock the deadline strip reads (§13).
        val finalHour = SampleData.hoursToDeadline() <= 1

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
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Notifications",
                            style = BrandType.IdentityAndEditorial.display2,
                            color = palette.ink
                        )
                        Text(
                            "$unread unread · newest first",
                            style = BrandType.ScoreAndData.dataSmall,
                            color = palette.inkDim
                        )
                    }
                }

                Spacer(Modifier.height(BrandDimens.SpaceLg))
                BrandRule()

                LazyColumn(Modifier.weight(1f)) {
                    items(feed, key = { it.id }) { item ->
                        NotificationRow(item, finalHour = finalHour)
                    }
                    item { Spacer(Modifier.height(BrandDimens.SpaceXxl)) }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: Notification, finalHour: Boolean) {
    val palette = LocalBrandPalette.current

    val kindTint = when (item.kind) {
        // Coral is licensed here by §01's "the final hour" role and nothing
        // else — outside that hour a deadline reminder is information, not an
        // alarm, and it takes the strongest neutral instead.
        NotificationKind.DEADLINE -> if (finalHour) palette.negative else palette.ink
        // Lime is the app's "look here" voice, and a match in play is the one
        // thing on this list that is still changing while it is being read.
        NotificationKind.LIVE -> palette.accent
        else -> palette.inkDim
    }

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = BrandDimens.MinTapTarget)
                // Wired by the integrator: each kind opens the thing it is about.
                .clickable { }
                .padding(horizontal = BrandDimens.Gutter, vertical = BrandDimens.SpaceMd),
            verticalAlignment = Alignment.Top
        ) {
            // Signal one: the dot. A fixed-width column whether or not it is
            // drawn, so read and unread rows keep the same left edge and the
            // list still scans as a column of headlines.
            Box(
                Modifier
                    .width(BrandDimens.SpaceMd)
                    .padding(top = BrandDimens.SpaceSm)
            ) {
                if (item.unread) {
                    Canvas(Modifier.size(BrandDimens.SpaceSm)) {
                        drawCircle(color = palette.ink)
                    }
                }
            }
            Spacer(Modifier.width(BrandDimens.SpaceSm))

            Column(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel(item.kind.label, color = kindTint)
                    Text(
                        relativeTime(item.minutesAgo),
                        style = BrandType.InterfaceAndGuidance.micro,
                        color = palette.inkDim
                    )
                }
                Spacer(Modifier.height(BrandDimens.SpaceXs))
                Text(
                    item.headline,
                    // Signals two and three: weight and ink. The name class,
                    // because a headline here routinely carries a club or a
                    // player name and the display faces drop the marks (§02).
                    style = if (item.unread) {
                        BrandType.IdentityAndEditorial.name
                    } else {
                        BrandType.IdentityAndEditorial.name.copy(
                            fontWeight = FontWeight.Normal
                        )
                    },
                    color = if (item.unread) palette.ink else palette.inkDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(BrandDimens.SpaceXs))
                Text(
                    item.detail,
                    // Detail lines name clubs and players often enough that
                    // picking the family per line would eventually pick wrong.
                    // Noto for all of them; the marks survive either way.
                    style = BrandType.InterfaceAndGuidance.bodySmall.copy(
                        fontFamily = BrandType.NigerianText
                    ),
                    color = if (item.kind == NotificationKind.DEADLINE) {
                        palette.ink
                    } else {
                        palette.inkDim
                    }
                )
            }
        }
        BrandRule(Modifier.padding(start = BrandDimens.Gutter))
    }
}
