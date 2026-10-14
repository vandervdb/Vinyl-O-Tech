package org.vander.konsist

import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test
import org.vander.konsist.debt.ArchitectureDebt
import org.vander.konsist.debt.isListedIn

class SpotifyLibArchitectureTest {
    private val domainFiles get() = productionScope.files.withPackage("org.vander.spotifyclient.domain..")

    @Test
    fun `spotify-lib domain imports no framework or DTO type`() {
        domainFiles
            .filterNot { it.isListedIn(ArchitectureDebt.spotifyLibDomainImpure) }
            .assertFalse(strict = true) { it.importsAny(FRAMEWORK_IMPORTS) }
    }

    @Test
    fun `spotify-lib domain does not import outer layers`() {
        domainFiles
            .filterNot { it.isListedIn(ArchitectureDebt.spotifyLibDomainWrongDirection) }
            .assertFalse(strict = true) { it.importsAny(SPOTIFY_LIB_OUTER_LAYERS) }
    }
}
