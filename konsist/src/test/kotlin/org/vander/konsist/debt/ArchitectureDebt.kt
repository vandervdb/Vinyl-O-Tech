package org.vander.konsist.debt

import com.lemonappdev.konsist.api.declaration.KoFileDeclaration

private const val SPOTIFY_LIB = "spotify-lib/src/main/kotlin/org/vander/spotifyclient"
private const val APP = "app/src/main/kotlin/org/vander/android/vinylotech"

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
            "$SPOTIFY_LIB/domain/auth/IAuthRemoteDatasource.kt",
            "$SPOTIFY_LIB/domain/auth/ISpotifyAuthClient.kt",
            "$SPOTIFY_LIB/domain/data/session/SpotifySessionManager.kt",
            "$SPOTIFY_LIB/domain/datasource/IRemotePlaylistDataSource.kt",
            "$SPOTIFY_LIB/domain/datasource/IRemoteQueueDataSource.kt",
            "$SPOTIFY_LIB/domain/datasource/IRemoteRecentlyPlayedDataSource.kt",
            "$SPOTIFY_LIB/domain/datasource/IRemoteUserDataSource.kt",
        )

    val spotifyLibDomainWrongDirection =
        setOf(
            "$SPOTIFY_LIB/domain/auth/ISpotifyAuthClient.kt",
            "$SPOTIFY_LIB/domain/data/session/SpotifySessionManager.kt",
            "$SPOTIFY_LIB/domain/usecase/PlayerUseCase.kt",
        )

    val appDependsOnSpotifyLibInternals =
        setOf(
            "$APP/di/SpotifySessionEntryPoint.kt",
            "$APP/feature/connection/ConnectionViewModelImpl.kt",
            "$APP/feature/home/HomeViewModelImpl.kt",
            "$APP/feature/library/PlayListViewModelImpl.kt",
            "$APP/feature/library/UserViewModelImpl.kt",
            "$APP/feature/player/PlayerViewModelImpl.kt",
            "$APP/util/RememberSessionManager.kt",
        )

    val androidUtilLog =
        setOf(
            "$APP/feature/library/PlaylistGrid.kt",
            "$APP/util/LifecycleObserverComponent.kt",
        )
}

// `path` is absolute and machine-specific, so entries are matched as project-relative suffixes.
internal fun KoFileDeclaration.isListedIn(debt: Set<String>) = debt.any { path.endsWith(it) }
