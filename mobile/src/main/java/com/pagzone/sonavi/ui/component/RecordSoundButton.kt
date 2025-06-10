package com.pagzone.sonavi.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pagzone.sonavi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RecordSoundButton(modifier: Modifier = Modifier) {
    val circleCount = 4
    val circleColors = List(circleCount) { MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) }

    val scopes = remember { List(circleCount) { Animatable(0f) } }
    var isAnimating by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf<Int?>(null) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        scopes.forEachIndexed { index, anim ->
            val scale = anim.value
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = 1f + scale
                        scaleY = 1f + scale
                        alpha = 1f - scale
                    }
                    .background(
                        color = circleColors[index],
                        shape = CircleShape
                    )
            )
        }

        Button(
            onClick = {
                isAnimating = !isAnimating
                if (isAnimating) {
                    countdown = 10
                }
            },
            modifier = Modifier.size(132.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            if (countdown != null) {
                Text(
                    text = countdown.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_mic_none),
                    contentDescription = "Start",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    // Animation loop
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            // Start countdown
            launch {
                for (i in 10 downTo 1) {
                    countdown = i
                    delay(1000)
                }
                countdown = null // Show mic again after 10s
                isAnimating = false
            }

            while (true) {
                scopes.forEachIndexed { i, anim ->
                    launch {
                        anim.snapTo(0f)
                        anim.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 1000,
                                delayMillis = i * 300,
                                easing = LinearEasing
                            )
                        )
                    }
                }
                delay(1000)
            }
        } else {
            scopes.forEach { it.snapTo(0f) }
            countdown = null
        }
    }
}