package org.vander.konsist

import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import org.junit.Test
import org.vander.konsist.debt.ArchitectureDebt
import org.vander.konsist.debt.isListedIn

class AppArchitectureTest {
    @Test
    fun `app does not depend on spotify-lib internals`() {
        productionScope
            .slice { !it.isListedIn(ArchitectureDebt.appDependsOnSpotifyLibInternals) }
            .assertArchitecture {
                val app = Layer("app", "org.vander.android.vinylotech..")
                val spotifyLibData = Layer("spotify-lib data", "org.vander.spotifyclient.data..")
                val spotifyLibDomain = Layer("spotify-lib domain", "org.vander.spotifyclient.domain..")
                val spotifyLibDi = Layer("spotify-lib di", "org.vander.spotifyclient.di..")

                app.doesNotDependOn(spotifyLibData)
                app.doesNotDependOn(spotifyLibDomain)
                app.doesNotDependOn(spotifyLibDi)
            }
    }
}
