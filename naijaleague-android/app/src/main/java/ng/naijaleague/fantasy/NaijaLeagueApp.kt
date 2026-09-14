package ng.naijaleague.fantasy

import android.app.Application

class NaijaLeagueApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Once per process, off the main thread, and the app is already
        // complete without it. See LiveCatalogue.
        LiveCatalogue.start(BuildConfig.CATALOGUE_BASE_URL)
    }
}
