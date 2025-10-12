package com.pagzone.sonavi.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.HorizontalPagerScaffold
import com.pagzone.sonavi.presentation.model.SoundPrediction

@Composable
fun ListeningPagerScreen(
    onStopListening: () -> Unit,
    detectedSound: SoundPrediction?
) {
    val pageCount = 2
    val initialPage = 1

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )

    var listeningStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    Box {
        HorizontalPagerScaffold(
            pagerState = pagerState,
            pageIndicator = {
                HorizontalPageIndicator(pagerState = pagerState)
            },
        ) {
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> StopListeningPage(
                        onStop = onStopListening,
                        startTime = listeningStartTime
                    )
                    1 -> DetectedSoundsPage(detectedSound)
                }
            }
        }
    }
}