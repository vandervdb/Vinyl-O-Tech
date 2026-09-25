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
            "$APP/feature/player/PlayerViewModelImpl.kt",
            "$APP/util/RememberSessionManager.kt",
        )

    // Declaration-level rules: entries are fully qualified names, not paths.
    val contractsToMoveToCoreDomain =
        setOf(
            "$SPOTIFY_LIB_PACKAGE.domain.data.session.SpotifySessionManager",
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
        )

    // Naming rules span every module, so entries spell out the full package.
    val interfacesWithIPrefix =
        setOf(
            "org.vander.core.domain.auth.IAuthRepository",
            "org.vander.core.domain.auth.ITokenProvider",
            "org.vander.spotifyclient.domain.auth.IAuthRemoteDatasource",
            "org.vander.spotifyclient.domain.auth.IDataStoreManager",
            "org.vander.spotifyclient.domain.auth.ISpotifyAuthClient",
            "org.vander.spotifyclient.domain.datasource.IRemoteLibraryDataSource",
            "org.vander.spotifyclient.domain.datasource.IRemotePlaylistDataSource",
            "org.vander.spotifyclient.domain.datasource.IRemoteQueueDataSource",
            "org.vander.spotifyclient.domain.datasource.IRemoteRecentlyPlayedDataSource",
            "org.vander.spotifyclient.domain.datasource.IRemoteUserDataSource",
        )

    val classesWithImplSuffix =
        setOf(
            "org.vander.android.vinylotech.feature.connection.ConnectionViewModelImpl",
            "org.vander.android.vinylotech.feature.home.HomeViewModelImpl",
            "org.vander.android.vinylotech.feature.library.PlayListViewModelImpl",
            "org.vander.android.vinylotech.feature.library.UserViewModelImpl",
            "org.vander.android.vinylotech.feature.player.PlayerViewModelImpl",
            "org.vander.core.logger.KermitLoggerImpl",
            "org.vander.spotifyclient.data.session.SpotifySessionManagerImpl",
        )

    // Declaration-level rule: entries are class names, not paths.
    val viewModelsDependingOnSpotifyLib =
        setOf(
            "ConnectionViewModelImpl",
            "PlayerViewModelImpl",
        )

    val androidUtilLog =
        setOf(
            "$APP/feature/library/PlaylistGrid.kt",
            "$APP/util/LifecycleObserverComponent.kt",
        )
}

// `path` is absolute and machine-specific, so entries are matched as project-relative suffixes.
internal fun KoFileDeclaration.isListedIn(debt: Set<String>) = debt.any { path.endsWith(it) }
