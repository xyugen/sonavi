package com.pagzone.sonavi.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.*
import com.pagzone.sonavi.presentation.data.repository.WearRepositoryImpl.clearPrediction
import com.pagzone.sonavi.presentation.model.SoundPrediction
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import com.pagzone.sonavi.R

@Composable
fun DetectedSoundsPage(detectedSound: SoundPrediction?) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(detectedSound) {
        if (detectedSound != null) {
            isVisible = true
            delay(5000) // wait 5 seconds
            isVisible = false
            delay(200) // Small delay for exit animation
            clearPrediction()   // call back to clear
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (detectedSound?.isCritical == true) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.background
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (detectedSound != null) {
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                DetectedSoundCard(detectedSound)
            }
        } else {
            EmptyStateContent()
        }
    }
}

@Composable
private fun DetectedSoundCard(detectedSound: SoundPrediction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Critical indicator with pulsing animation
        if (detectedSound.isCritical) {
            CriticalSoundIndicator()
        }

        // Sound icon with background circle
        SoundIcon(detectedSound.isCritical)

        // Title
        Text(
            text = "Sound Detected",
            style = MaterialTheme.typography.titleSmall,
            color = if (detectedSound.isCritical) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center
        )

        // Detected sound label with emphasis
        Text(
            text = detectedSound.label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (detectedSound.isCritical) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Confidence section
        ConfidenceSection(
            confidence = detectedSound.confidence,
            isCritical = detectedSound.isCritical
        )
    }
}

@Composable
private fun CriticalSoundIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "critical_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Critical Alert",
            tint = MaterialTheme.colorScheme.error.copy(alpha = alpha),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "CRITICAL ALERT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error.copy(alpha = alpha),
            letterSpacing = 0.5.sp
        )
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Critical Alert",
            tint = MaterialTheme.colorScheme.error.copy(alpha = alpha),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SoundIcon(isCritical: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "sound_wave")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCritical) 1.2f else 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sound_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(64.dp)
    ) {
        // Background circle
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
        ) {
            drawCircle(
                color = if (isCritical)
                    Color.Red.copy(alpha = 0.2f)
                else
                    Color.Blue.copy(alpha = 0.2f),
                radius = size.minDimension / 2
            )
        }

        // Sound wave icon
        Icon(
            imageVector =
                if (isCritical) ImageVector.vectorResource(R.drawable.ic_volume_up)
                else ImageVector.vectorResource(R.drawable.ic_graphic_eq),
            contentDescription = "Sound Wave",
            tint = if (isCritical) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun ConfidenceSection(confidence: Float, isCritical: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Animated progress bar
        val animatedProgress by animateFloatAsState(
            targetValue = confidence,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "confidence_progress"
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(8.dp)),
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = if (isCritical) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        // Confidence percentage with better styling
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Confidence:",
                style = MaterialTheme.typography.bodySmall,
                color = if (isCritical) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = "${(confidence * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isCritical) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

@Composable
private fun EmptyStateContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Listening animation
        val infiniteTransition = rememberInfiniteTransition(label = "listening")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "listening_alpha"
        )

        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_hearing),
            contentDescription = "Listening",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = "Listening...",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Waiting for sound detection",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}