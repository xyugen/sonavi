package com.pagzone.sonavi.ui.component

import android.os.VibrationEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.pagzone.sonavi.R
import com.pagzone.sonavi.util.VibrationHelper
import kotlinx.coroutines.delay

@Composable
fun VibrationPlayer(pattern: List<Long>) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val vibrator = VibrationHelper(LocalContext.current)

    val fixedPattern = remember(pattern) {
        if (pattern.size % 2 != 0) pattern.dropLast(1) else pattern
    }

    val totalDuration = remember(fixedPattern) { fixedPattern.sum() }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val startTime = System.currentTimeMillis()
            while (isPlaying && progress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                progress = (elapsed.toFloat() / totalDuration).coerceAtMost(1f)
                if (progress >= 1f) {
                    isPlaying = false
                    progress = 0f
                }
                delay(16)
            }
            progress = 0f
        } else progress = 0f
    }

    DisposableEffect(Unit) {
        onDispose {
            vibrator.cancel()
            isPlaying = false
        }
    }

    Box {
        if (isPlaying) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                strokeWidth = 2.dp
            )
        }

        IconButton(
            modifier = Modifier.size(28.dp).padding(0.dp),
            onClick = {
                if (!isPlaying) {
                    isPlaying = true
                    progress = 0f
                    val vibrationEffect =
                        VibrationEffect.createWaveform(fixedPattern.toLongArray(), -1)
                    vibrator.vibrate(vibrationEffect)
                } else {
                    vibrator.cancel()
                    isPlaying = false
                    progress = 0f
                }
            }
        ) {
            Icon(
                imageVector = if (isPlaying)
                    ImageVector.vectorResource(R.drawable.ic_stop)
                else
                    Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Stop" else "Play",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
