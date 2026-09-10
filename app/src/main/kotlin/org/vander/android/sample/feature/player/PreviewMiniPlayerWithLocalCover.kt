package org.vander.android.sample.feature.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import org.vander.android.sample.R
import org.vander.core.logger.Logger
import org.vander.core.logger.test.FakeLogger
import org.vander.fake.spotify.FakePlayerViewModel

@Preview(showBackground = true)
@Suppress("FunctionNaming")
@Composable
fun PreviewMiniPlayerWithLocalCover() {
    val fakeViewModel = remember { FakePlayerViewModel() }
    val logger: Logger = FakeLogger()

    MiniPlayerWithPainter(
        viewModel = fakeViewModel,
        coverPainter = painterResource(id = R.drawable.mr_scurff_test_cover),
        logger = logger,
    )
}
