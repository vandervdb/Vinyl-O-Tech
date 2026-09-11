package org.vander.android.sample.feature.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.vander.fake.spotify.FakePlaylistViewModel

@Preview(showBackground = true)
@Composable
fun PreviewPlaylistComponent() {
    val fakeViewModel = remember { FakePlaylistViewModel() }

    PlaylistComponent(
        viewModel = fakeViewModel,
        modifier = Modifier.fillMaxSize(),
    )
}
