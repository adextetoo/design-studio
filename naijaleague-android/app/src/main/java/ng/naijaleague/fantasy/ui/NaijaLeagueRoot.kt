package ng.naijaleague.fantasy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ng.naijaleague.fantasy.brand.BrandSurface
import ng.naijaleague.fantasy.brand.LocalBrandPalette
import ng.naijaleague.fantasy.brand.NaijaLeagueTheme
import ng.naijaleague.fantasy.data.SampleData
import ng.naijaleague.fantasy.ui.components.BrandBottomBar
import ng.naijaleague.fantasy.ui.components.DeadlineStrip
import ng.naijaleague.fantasy.ui.components.Tab
import ng.naijaleague.fantasy.ui.screens.ChoosePlayersScreen
import ng.naijaleague.fantasy.ui.screens.GafferPassScreen
import ng.naijaleague.fantasy.ui.screens.HomeScreen
import ng.naijaleague.fantasy.ui.screens.LeaguesScreen
import ng.naijaleague.fantasy.ui.screens.LiveMatchScreen
import ng.naijaleague.fantasy.ui.screens.OnboardingScreen
import ng.naijaleague.fantasy.ui.screens.ReceiptScreen
import ng.naijaleague.fantasy.ui.screens.RulesScreen
import ng.naijaleague.fantasy.ui.screens.ThreePickScreen
import ng.naijaleague.fantasy.ui.screens.EndCardScreen
import ng.naijaleague.fantasy.ui.screens.JoinLeaguesScreen
import ng.naijaleague.fantasy.ui.screens.NotificationsScreen
import ng.naijaleague.fantasy.ui.screens.PointsScreen
import ng.naijaleague.fantasy.ui.screens.ProfileScreen
import ng.naijaleague.fantasy.ui.screens.SignUpScreen
import ng.naijaleague.fantasy.ui.screens.SplashScreen
import ng.naijaleague.fantasy.ui.screens.TransferConfirmedScreen
import ng.naijaleague.fantasy.ui.screens.TeamScreen

/**
 * Four tabs and plain state — no navigation graph.
 *
 * "One decision per screen" (§13). A four-destination app with two overlays
 * does not need a nav host, and the fewer moving parts between a thumb and a
 * squad the better on a slow handset.
 */
/**
 * The launch sequence, which is the adopted flow's stages 1-3 and 14 in order.
 *
 * A returning manager skips all of it: signing in from the splash goes straight
 * to [Phase.APP]. The end card closes the FIRST session only — showing it after
 * every visit would make leaving the app an event, and it is not one.
 */
private enum class Phase { SPLASH, ONBOARDING, SIGN_UP, END_CARD, APP }

private enum class Overlay {
    NONE, CHOOSE_PLAYERS, LIVE_MATCH, RECEIPT, GAFFER_PASS, THREE_PICK,
    POINTS, PROFILE, NOTIFICATIONS, JOIN_LEAGUES, TRANSFER_CONFIRMED
}

@Composable
fun NaijaLeagueRoot() {
    var phase by rememberSaveable { mutableStateOf(Phase.SPLASH) }
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var overlay by rememberSaveable { mutableStateOf(Overlay.NONE) }

    // The squad the account step is asked to save. Named here rather than inside
    // the screen so the heading can say what is actually at stake.
    val squadName = SampleData.miniLeague[4].squadName

    when (phase) {
        Phase.SPLASH -> {
            SplashScreen(
                onStart = { phase = Phase.ONBOARDING },
                // A returning manager has a squad already and waits for nothing.
                onSignIn = { phase = Phase.APP }
            )
            return
        }
        Phase.ONBOARDING -> {
            OnboardingScreen(onFinished = { phase = Phase.SIGN_UP })
            return
        }
        Phase.SIGN_UP -> {
            SignUpScreen(
                squadName = squadName,
                onSaved = { phase = Phase.END_CARD },
                onSignIn = { phase = Phase.APP },
                onBack = { phase = Phase.ONBOARDING }
            )
            return
        }
        Phase.END_CARD -> {
            EndCardScreen(onHome = { phase = Phase.APP })
            return
        }
        Phase.APP -> Unit
    }

    when (overlay) {
        Overlay.CHOOSE_PLAYERS -> {
            ChoosePlayersScreen(onClose = { overlay = Overlay.TRANSFER_CONFIRMED })
            return
        }
        Overlay.LIVE_MATCH -> {
            LiveMatchScreen(onClose = { overlay = Overlay.NONE })
            return
        }
        Overlay.RECEIPT -> {
            ReceiptScreen(onClose = { overlay = Overlay.NONE })
            return
        }
        Overlay.GAFFER_PASS -> {
            GafferPassScreen(onClose = { overlay = Overlay.NONE })
            return
        }
        Overlay.THREE_PICK -> {
            ThreePickScreen(onClose = { overlay = Overlay.NONE })
            return
        }
        Overlay.POINTS -> {
            PointsScreen(onClose = { overlay = Overlay.NONE })
            return
        }
        Overlay.PROFILE -> {
            ProfileScreen(
                onClose = { overlay = Overlay.NONE },
                onOpenGafferPass = { overlay = Overlay.GAFFER_PASS }
            )
            return
        }
        Overlay.NOTIFICATIONS -> {
            NotificationsScreen(onClose = { overlay = Overlay.NONE })
            return
        }
        Overlay.JOIN_LEAGUES -> {
            JoinLeaguesScreen(onClose = { overlay = Overlay.NONE })
            return
        }
        Overlay.TRANSFER_CONFIRMED -> {
            TransferConfirmedScreen(
                onViewTeam = {
                    overlay = Overlay.NONE
                    tab = Tab.TEAM
                },
                onAnother = { overlay = Overlay.CHOOSE_PLAYERS }
            )
            return
        }
        Overlay.NONE -> Unit
    }

    // The rules page reads on Nzu Chalk — it is the reading surface, not a
    // light mode (§01). Everything else lives on Night Pitch.
    val surface = if (tab == Tab.RULES) BrandSurface.NZU_CHALK else BrandSurface.NIGHT_PITCH

    NaijaLeagueTheme(surface = surface) {
        val palette = LocalBrandPalette.current
        Box(Modifier.fillMaxSize().background(palette.ground)) {
            Column(Modifier.fillMaxSize()) {
                // §13: the deadline is permanent furniture, not a banner. It is the
                // clock the whole product is organised around, so it sits above
                // every tab rather than on Home alone.
                DeadlineStrip(
                    deadlineLabel = SampleData.deadlineLabel,
                    hoursRemaining = SampleData.hoursToDeadline(),
                    modifier = Modifier.statusBarsPadding()
                )
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        Tab.HOME -> HomeScreen(
                            onViewTeam = { overlay = Overlay.POINTS },
                            onOpenProfile = { overlay = Overlay.PROFILE },
                            onOpenNotifications = { overlay = Overlay.NOTIFICATIONS },
                            onOpenLive = { overlay = Overlay.LIVE_MATCH },
                            onOpenReceipt = { overlay = Overlay.RECEIPT },
                            onOpenThreePick = { overlay = Overlay.THREE_PICK }
                        )
                        Tab.TEAM -> TeamScreen(onChoosePlayers = { overlay = Overlay.CHOOSE_PLAYERS })
                        Tab.LEAGUES -> LeaguesScreen(
                            onOpenGafferPass = { overlay = Overlay.GAFFER_PASS },
                            onJoinLeagues = { overlay = Overlay.JOIN_LEAGUES }
                        )
                        Tab.RULES -> RulesScreen()
                    }
                }
                BrandBottomBar(selected = tab, onSelect = { tab = it })
            }
        }
    }
}
