package com.pagzone.sonavi.presentation.ui

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.pagzone.sonavi.presentation.util.formatDuration
import kotlinx.coroutines.delay

@Composable
fun StopListeningPage(
    onStop: () -> Unit,
    startTime: Long = System.currentTimeMillis()
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val elapsedSeconds = remember(currentTime) {
        ((currentTime - startTime) / 1000).toInt()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Simple pulsing dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    Color(0xFFE53935).copy(alpha = pulseAlpha),
                    CircleShape
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Duration (large and prominent)
        Text(
            text = formatDuration(elapsedSeconds),
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Listening",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        CompactChip(
            label = { Text("Stop", fontWeight = FontWeight.SemiBold) },
            onClick = onStop,
            colors = ChipDefaults.chipColors(
                backgroundColor = Color(0xFFE53935),
                contentColor = Color.White
            )
        )
    }
}