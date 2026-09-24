package org.vander.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.declaration.type.KoTypeDeclaration
import com.lemonappdev.konsist.api.provider.KoModuleProvider

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

// Top-level only: a nested interface takes its effective visibility from its parent, so an
// internal parent already makes it internal without the modifier on the nested one.
internal val spotifyLibInterfaces
    get() = productionScope.interfaces(includeNested = false).filter { it.resideInModule("spotify-lib") }

// Imports are a complete signal here: no two modules share a package and nothing imports
// spotifyclient with a wildcard, so any cross-module use needs an import. Test sources are
// included: an app test that uses a contract keeps that contract public.
private val importedOutsideSpotifyLib by lazy {
    Konsist
        .scopeFromProject()
        .files
        .filterNot { it.resideInModule("spotify-lib") }
        .flatMap { file -> file.imports.map { it.name } }
        .toSet()
}

internal fun KoInterfaceDeclaration.isUsedOutsideSpotifyLib() = fullyQualifiedName in importedOutsideSpotifyLib

// An allowlist, not a denylist: a ViewModel depending on any other module, even a future one, fails.
// core/logger and core/ui are cross-cutting, not business contracts.
private val VIEW_MODEL_ALLOWED_MODULES = setOf("core/domain", "core/logger", "core/ui")

internal val appViewModels
    get() =
        productionScope
            .classes()
            .filter { it.resideInModule("app") && it.hasAnnotationWithName("HiltViewModel") }

internal fun KoClassDeclaration.receivesOnlyContractTypes() =
    primaryConstructor
        ?.parameters
        .orEmpty()
        .all { it.type.isFromAllowedModule() }

// A library type (SavedStateHandle...) belongs to no project module: only project declarations are constrained.
private fun KoTypeDeclaration.isFromAllowedModule(): Boolean {
    val module = (sourceDeclaration as? KoModuleProvider)?.moduleName ?: return true
    return module in VIEW_MODEL_ALLOWED_MODULES
}
