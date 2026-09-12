package ng.naijaleague.fantasy

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import ng.naijaleague.fantasy.brand.NaijaLeagueTheme
import ng.naijaleague.fantasy.ui.NaijaLeagueRoot

/**
 * Reports whether the handset has animations switched off.
 *
 * The brand system requires the app to be fully usable with motion disabled
 * (§13, the 240ms rule). Android exposes this as the global animator duration
 * scale, which a lot of people on older handsets set to zero to claw back
 * performance — exactly our audience.
 */
val LocalAnimationsEnabled = staticCompositionLocalOf { true }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val animationScale = runCatching {
            Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f)

        setContent {
            CompositionLocalProvider(LocalAnimationsEnabled provides (animationScale > 0f)) {
                NaijaLeagueTheme {
                    NaijaLeagueRoot()
                }
            }
        }
    }
}
