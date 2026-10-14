package org.vander.konsist.debt

import com.lemonappdev.konsist.api.declaration.KoFileDeclaration

private const val SPOTIFY_LIB = "spotify-lib/src/main/kotlin/org/vander/spotifyclient"
private const val APP = "app/src/main/kotlin/org/vander/android/vinylotech"
private const val SPOTIFY_LIB_PACKAGE = "org.vander.spotifyclient"

/**
 * Production files that break an architecture rule today.
 *
 * Each list may only shrink: a new violation fails its rule, and a fixed file still
 * listed here fails [ArchitectureDebtTest]. Remove an entry in the same commit as its fix.
 */
internal object ArchitectureDebt {
    val spotifyLibDomainImpure =
        setOf(
            "$SPOTIFY_LIB/domain/appremote/AppRemoteProvider.kt",
            "$SPOTIFY_LIB/domain/appremote/RemoteConnector.kt",
        )

    val spotifyLibDomainWrongDirection = emptySet<String>()

    val appDependsOnSpotifyLibInternals = emptySet<String>()

    // Declaration-level rules: entries are fully qualified names, not paths.
    val contractsToMoveToCoreDomain = emptySet<String>()

    // A core/domain contract that only spotify-lib uses: it is not a port and belongs in spotify-lib, internal.
    val coreDomainContractsUsedOnlyBySpotifyLib =
        setOf(
            "org.vander.core.domain.library.LibraryRepository",
            "org.vander.core.domain.player.PlayerStateRepository",
            "org.vander.core.domain.queue.QueueRepository",
        )

    val contractsToMakeInternal =
        setOf(
            "$SPOTIFY_LIB_PACKAGE.domain.appremote.AppRemoteProvider",
            "$SPOTIFY_LIB_PACKAGE.domain.appremote.RemoteConnector",
            "$SPOTIFY_LIB_PACKAGE.domain.player.PlayerClient",
        )

    // Naming rules span every module, so entries spell out the full package.
    val interfacesWithIPrefix = emptySet<String>()

    val classesWithImplSuffix =
        setOf(
            "org.vander.core.logger.KermitLoggerImpl",
        )

    val adaptersNotNamedAfterSpotify = emptySet<String>()

    // Declaration-level rule: entries are class names, not paths.
    val viewModelsDependingOnSpotifyLib = emptySet<String>()

    val androidUtilLog =
        setOf(
            "$APP/feature/library/PlaylistGrid.kt",
        )
}

// `path` is absolute and machine-specific, so entries are matched as project-relative suffixes.
internal fun KoFileDeclaration.isListedIn(debt: Set<String>) = debt.any { path.endsWith(it) }
