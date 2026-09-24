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

    // Declaration-level rules: entries are fully qualified names, not paths.
    val contractsToMoveToCoreDomain =
        setOf(
            "$SPOTIFY_LIB_PACKAGE.domain.data.session.SpotifySessionManager",
            "$SPOTIFY_LIB_PACKAGE.domain.player.PlayerController",
            "$SPOTIFY_LIB_PACKAGE.domain.repository.SpotifyPlaylistRepository",
            "$SPOTIFY_LIB_PACKAGE.domain.repository.UserRepository",
        )

    val contractsToMakeInternal =
        setOf(
            "$SPOTIFY_LIB_PACKAGE.domain.appremote.AppRemoteProvider",
            "$SPOTIFY_LIB_PACKAGE.domain.appremote.RemoteConnector",
            "$SPOTIFY_LIB_PACKAGE.domain.auth.IAuthRemoteDatasource",
            "$SPOTIFY_LIB_PACKAGE.domain.auth.IDataStoreManager",
            "$SPOTIFY_LIB_PACKAGE.domain.auth.ISpotifyAuthClient",
            "$SPOTIFY_LIB_PACKAGE.domain.datasource.IRemoteLibraryDataSource",
            "$SPOTIFY_LIB_PACKAGE.domain.datasource.IRemotePlaylistDataSource",
            "$SPOTIFY_LIB_PACKAGE.domain.datasource.IRemoteQueueDataSource",
            "$SPOTIFY_LIB_PACKAGE.domain.datasource.IRemoteRecentlyPlayedDataSource",
            "$SPOTIFY_LIB_PACKAGE.domain.datasource.IRemoteUserDataSource",
            "$SPOTIFY_LIB_PACKAGE.domain.player.PlayerClient",
            "$SPOTIFY_LIB_PACKAGE.domain.repository.LibraryRepository",
            "$SPOTIFY_LIB_PACKAGE.domain.repository.RecentlyPlayedRepository",
            "$SPOTIFY_LIB_PACKAGE.domain.repository.SpotifyQueueRepository",
            "$SPOTIFY_LIB_PACKAGE.domain.usecase.SpotifyRemoteUseCase",
        )

    // Declaration-level rule: entries are class names, not paths.
    val viewModelsDependingOnSpotifyLib =
        setOf(
            "ConnectionViewModelImpl",
            "HomeViewModelImpl",
            "PlayListViewModelImpl",
            "PlayerViewModelImpl",
            "UserViewModelImpl",
        )

    val androidUtilLog =
        setOf(
            "$APP/feature/library/PlaylistGrid.kt",
            "$APP/util/LifecycleObserverComponent.kt",
        )
}

// `path` is absolute and machine-specific, so entries are matched as project-relative suffixes.
internal fun KoFileDeclaration.isListedIn(debt: Set<String>) = debt.any { path.endsWith(it) }
