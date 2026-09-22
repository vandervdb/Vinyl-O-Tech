package org.vander.android.vinylotech.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.vander.android.vinylotech.R
import org.vander.android.vinylotech.designsystem.AndroidAppTheme
import org.vander.android.vinylotech.designsystem.VotDimens
import org.vander.android.vinylotech.designsystem.component.VinylBrandTopBar
import org.vander.android.vinylotech.designsystem.component.VinylFilterChipRow
import org.vander.android.vinylotech.designsystem.component.VinylHeroBackdrop
import org.vander.android.vinylotech.designsystem.component.VinylHeroFeature
import org.vander.android.vinylotech.designsystem.component.VinylSectionHeader
import org.vander.android.vinylotech.designsystem.modifier.fullBleed
import org.vander.core.domain.data.Playlist
import org.vander.core.domain.data.PlaylistCollection

/** Maquette écran 02 : les chips sont suivies de `padding-bottom: 14px`, le bloc héros est à 20 px du bas. */
private val HERO_FEATURE_BOTTOM = VotDimens.space20

/** Titre de section : `padding: 20px 20px 12px`, moins l'écart vertical que la grille ajoute déjà. */
private val SECTION_TOP = VotDimens.space20 - VotDimens.gridGutterV

private const val GRID_COLUMNS = 3

private const val KEY_HERO = "home:hero"

private const val KEY_SECTION = "home:section"

/**
 * The whole Accueil screen (maquette écran 02), without a ViewModel: it draws [state] and
 * reports what the user does.
 *
 * Stateless on purpose. It is previewable in every state, and `HomeScreen` only has to collect
 * the ViewModel and pass values and lambdas through. Every callback defaults to doing nothing,
 * so they can be wired one at a time.
 *
 * One `LazyVerticalGrid` holds everything — hero, section header, tiles — instead of a column
 * wrapping a grid, which would nest two vertical scrolls. The hero and the header span the three
 * columns and escape the grid's side padding through [fullBleed]; the tiles keep it.
 */
@Composable
fun HomeContent(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFilterSelect: (HomeFilter) -> Unit = {},
    onResumeListen: () -> Unit = {},
    onResumeToggleSave: () -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        modifier = modifier,
        contentPadding = PaddingValues(start = VotDimens.listPaddingStart, end = VotDimens.listPaddingEnd),
        verticalArrangement = Arrangement.spacedBy(VotDimens.gridGutterV),
        horizontalArrangement = Arrangement.spacedBy(VotDimens.gridGutterH),
    ) {
        item(key = KEY_HERO, span = { GridItemSpan(maxLineSpan) }, contentType = KEY_HERO) {
            HomeHero(
                state = state,
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
                onFilterSelect = onFilterSelect,
                onResumeListen = onResumeListen,
                onResumeToggleSave = onResumeToggleSave,
                modifier = Modifier.fullBleed(VotDimens.listPaddingStart, VotDimens.listPaddingEnd),
            )
        }

        item(key = KEY_SECTION, span = { GridItemSpan(maxLineSpan) }, contentType = KEY_SECTION) {
            val count = state.playlists.items.size
            VinylSectionHeader(
                title = stringResource(R.string.home_library_title),
                trailing = pluralStringResource(R.plurals.home_library_count, count, count),
                modifier =
                    Modifier
                        .fullBleed(VotDimens.listPaddingStart, VotDimens.listPaddingEnd)
                        .padding(start = VotDimens.space20, end = VotDimens.space20, top = SECTION_TOP),
            )
        }

        playlistGridItems(
            playlists = state.playlists.items,
            playingPlaylistId = state.playingPlaylistId,
            onPlaylistClick = onPlaylistClick,
        )
    }
}

@Composable
private fun HomeHero(
    state: HomeUiState,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFilterSelect: (HomeFilter) -> Unit,
    onResumeListen: () -> Unit,
    onResumeToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VinylHeroBackdrop(modifier = modifier) {
        Column {
            VinylBrandTopBar(onSearchClick = onSearchClick, onSettingsClick = onSettingsClick)
            VinylFilterChipRow(
                options = HomeFilter.entries,
                selected = state.selectedFilter,
                label = { stringResource(it.labelRes) },
                onSelect = onFilterSelect,
            )
        }

        state.resume?.let { resume ->
            Box(Modifier.align(Alignment.BottomStart).padding(bottom = HERO_FEATURE_BOTTOM)) {
                VinylHeroFeature(
                    kicker = stringResource(R.string.home_resume_kicker),
                    title = resume.title,
                    subtitle = resume.subtitle,
                    isSaved = resume.isSaved,
                    onListen = onResumeListen,
                    onToggleSave = onResumeToggleSave,
                )
            }
        }
    }
}

private val previewState =
    HomeUiState(
        playlists =
            PlaylistCollection(
                listOf("Sillons", "Marée haute", "Braise", "Gris perle", "Or mat", "Onde longue", "Souffle")
                    .mapIndexed { index, name -> Playlist(id = "p$index", name = name, coverUrl = "") },
            ),
        playingPlaylistId = "p2",
        resume = ResumeListening(title = "Nuits blanches", subtitle = "Elia Faure · 2023 · 11 titres", isSaved = false),
    )

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10, heightDp = 900)
@Composable
private fun HomeContentPreview() {
    AndroidAppTheme { HomeContent(state = previewState, modifier = Modifier.fillMaxSize()) }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10, heightDp = 900, name = "Nothing to resume")
@Composable
private fun HomeContentNoResumePreview() {
    AndroidAppTheme { HomeContent(state = previewState.copy(resume = null), modifier = Modifier.fillMaxSize()) }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0A10, heightDp = 900, name = "Empty library")
@Composable
private fun HomeContentEmptyPreview() {
    AndroidAppTheme { HomeContent(state = HomeUiState(), modifier = Modifier.fillMaxSize()) }
}
