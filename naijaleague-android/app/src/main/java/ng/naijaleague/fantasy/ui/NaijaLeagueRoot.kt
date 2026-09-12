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
import ng.naijaleague.fantasy.ui.screens.TeamScreen

/**
 * Four tabs and plain state — no navigation graph.
 *
 * "One decision per screen" (§13). A four-destination app with two overlays
 * does not need a nav host, and the fewer moving parts between a thumb and a
 * squad the better on a slow handset.
 */
private enum class Overlay { NONE, CHOOSE_PLAYERS, LIVE_MATCH, RECEIPT, GAFFER_PASS }

@Composable
fun NaijaLeagueRoot() {
    var onboarded by rememberSaveable { mutableStateOf(false) }
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var overlay by rememberSaveable { mutableStateOf(Overlay.NONE) }

    if (!onboarded) {
        OnboardingScreen(onFinished = { onboarded = true })
        return
    }

    when (overlay) {
        Overlay.CHOOSE_PLAYERS -> {
            ChoosePlayersScreen(onClose = { overlay = Overlay.NONE })
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
                            onViewTeam = { tab = Tab.TEAM },
                            onOpenLive = { overlay = Overlay.LIVE_MATCH },
                            onOpenReceipt = { overlay = Overlay.RECEIPT }
                        )
                        Tab.TEAM -> TeamScreen(onChoosePlayers = { overlay = Overlay.CHOOSE_PLAYERS })
                        Tab.LEAGUES -> LeaguesScreen(
                            onOpenGafferPass = { overlay = Overlay.GAFFER_PASS }
                        )
                        Tab.RULES -> RulesScreen()
                    }
                }
                BrandBottomBar(selected = tab, onSelect = { tab = it })
            }
        }
    }
}
