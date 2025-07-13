package com.pagzone.sonavi.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.HorizontalPagerScaffold

@Composable
fun ListeningPagerScreen(
    onStopListening: () -> Unit,
    detectedSound: String
) {
    val pageCount = 2
    val initialPage = 1

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )

    Box {
        HorizontalPagerScaffold(
            pagerState = pagerState,
            pageIndicator = {
                HorizontalPageIndicator(pagerState = pagerState)
            },
        ) {
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> StopListeningPage { onStopListening() }
                    1 -> DetectedSoundsPage(detectedSound)
                }
            }
        }
    }
}