package org.vander.vinylotech

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

private val FRAMEWORK_IMPORTS = listOf("android.", "androidx.", "com.spotify.", "org.vander.core.dto.")

private fun KoFileDeclaration.importsAny(prefixes: List<String>) =
    hasImport { import ->
        prefixes.any {
            import.name.startsWith(it)
        }
    }

class DependenciesTest {
    // general
    @Test
    fun `no class should use Android util logging`() {
        Konsist
            .scopeFromProduction()
            .files
            .assertFalse(strict = true) { it.hasImport { import -> import.name == "android.util.Log" } }
    }

    // app
    @Test
    fun `app does not depend on spotify-lib internals`() {
        Konsist
            .scopeFromProduction()
            .assertArchitecture {
                val app = Layer("app", "org.vander.android.vinylotech..")
                val spotifyLibData = Layer("data", "org.vander.spotifyclient.data..")
                val spotifyLibDomain = Layer("domain", "org.vander.spotifyclient.domain..")
                val spotifyLibDi = Layer("di", "org.vander.spotifyclient.di..")

                app.doesNotDependOn(spotifyLibData)
                app.doesNotDependOn(spotifyLibDomain)
                app.doesNotDependOn(spotifyLibDi)
            }
    }

    // core domain
    @Test
    fun `core domain is pure`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPackage("org.vander.core.domain..")
            .assertFalse(strict = true) { it.importsAny(FRAMEWORK_IMPORTS) }
    }

    // core security
    @Test
    fun `core security api is pure`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPackage("org.vander.core.security.api..")
            .assertFalse(strict = true) {
                it.importsAny(
                    listOf("org.vander.core.security.impl.", "com.google.crypto.tink.", "androidx.datastore."),
                )
            }
    }

    // spotify-lib
    @Test
    fun `spotify-lib domain is pure`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPackage("org.vander.spotifyclient.domain..")
            .assertFalse(strict = true) { it.importsAny(FRAMEWORK_IMPORTS) }
    }

    @Test
    fun `spotify-lib domain dependencies direction is correct`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPackage("org.vander.spotifyclient.domain..")
            .assertFalse(strict = true) {
                it.importsAny(
                    listOf(
                        "org.vander.spotifyclient.data.",
                        "org.vander.spotifyclient.bridge.",
                        "org.vander.spotifyclient.di.",
                        "org.vander.spotifyclient.network.",
                    ),
                )
            }
    }
}
