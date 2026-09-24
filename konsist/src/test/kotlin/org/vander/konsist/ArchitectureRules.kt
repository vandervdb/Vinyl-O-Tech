package org.vander.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration

// Parsing every production file is the slow part of each test; do it once per test JVM.
internal val productionScope by lazy { Konsist.scopeFromProduction() }

internal val FRAMEWORK_IMPORTS = listOf("android.", "androidx.", "com.spotify.", "org.vander.core.dto.")

internal val SECURITY_BACKEND_IMPORTS =
    listOf("org.vander.core.security.impl.", "com.google.crypto.tink.", "androidx.datastore.")

internal val SPOTIFY_LIB_OUTER_LAYERS =
    listOf(
        "org.vander.spotifyclient.data.",
        "org.vander.spotifyclient.bridge.",
        "org.vander.spotifyclient.di.",
        "org.vander.spotifyclient.network.",
    )

// Mirrors the layers checked by AppArchitectureTest, for the debt ratchet.
internal val SPOTIFY_LIB_INTERNALS =
    listOf(
        "org.vander.spotifyclient.data.",
        "org.vander.spotifyclient.domain.",
        "org.vander.spotifyclient.di.",
    )

internal fun KoFileDeclaration.importsAny(prefixes: List<String>) =
    hasImport { import ->
        prefixes.any {
            import.name.startsWith(it)
        }
    }

internal fun KoFileDeclaration.importsAndroidLog() = hasImport { import -> import.name == "android.util.Log" }
