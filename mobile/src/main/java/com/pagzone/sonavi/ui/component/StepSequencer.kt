package com.pagzone.sonavi.ui.component

import android.os.VibrationEffect
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pagzone.sonavi.R
import com.pagzone.sonavi.util.Helper.Companion.stepsToVibrationPattern
import com.pagzone.sonavi.util.Helper.Companion.vibrationPatternToSteps
import com.pagzone.sonavi.util.VibrationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

@Composable
fun StepSequencer(
    initialPattern: List<Long>? = null,
    stepCount: Int = 25,
    stepDurationMs: Long = 200,
    columns: Int = 5,
    onPatternChanged: (List<Boolean>) -> Unit
) {
    var steps by remember {
        mutableStateOf(
            if (initialPattern != null)
                vibrationPatternToSteps(initialPattern, stepDurationMs, stepCount)
            else
                List(stepCount) { false }
        )
    }
    var isPlaying by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(-1) }
    var animationJob by remember { mutableStateOf<Job?>(null) }
    val vibrator = VibrationHelper(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()

    val animatedStepAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(stepDurationMs.toInt()),
        label = "stepAlpha"
    )

    LaunchedEffect(steps) {
        onPatternChanged(steps)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        PatternHeader(
            stepCount = stepCount,
            stepDurationMs = stepDurationMs,
            activeSteps = steps.count { it },
            steps = steps
        )

        // Step Grid
        StepGrid(
            steps = steps,
            columns = columns,
            stepDurationMs = stepDurationMs,
            currentStep = if (isPlaying) currentStep else -1,
            animatedStepAlpha = animatedStepAlpha,
            onStepToggle = { index ->
                steps = steps.toMutableList().apply {
                    this[index] = !this[index]
                }
            }
        )

        // Control buttons
        ControlButtons(
            steps = steps,
            isPlaying = isPlaying,
            onClear = {
                animationJob?.cancel()
                steps = List(stepCount) { false }
                isPlaying = false
                currentStep = -1
            },
            onRandomize = {
                animationJob?.cancel()
                steps = List(stepCount) { Random.nextFloat() < 0.3f }
                isPlaying = false
                currentStep = -1
            },
            onPlay = {
                if (!isPlaying && steps.contains(true)) {
                    isPlaying = true
                    currentStep = 0

                    val pattern = stepsToVibrationPattern(steps, stepDurationMs)
                    val vibrationEffect = VibrationEffect.createWaveform(pattern, -1)
                    vibrator.vibrate(vibrationEffect)

                    // Animate
                    animationJob = coroutineScope.launch {
                        try {
                            repeat(stepCount) { step ->
                                currentStep = step
                                delay(stepDurationMs)
                            }
                        } catch (_: CancellationException) {
                            // Animation cancel
                        } finally {
                            isPlaying = false
                            currentStep = -1
                        }
                    }
                }
            },
            onStop = {
                animationJob?.cancel()
                vibrator.cancel()
                isPlaying = false
                currentStep = -1
            },
        )
    }
}

@Composable
private fun PatternHeader(
    stepCount: Int,
    stepDurationMs: Long,
    activeSteps: Int,
    steps: List<Boolean>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Vibration Pattern",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(stepCount * stepDurationMs) / 1000f}s total • $activeSteps active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Pattern visualization mini-bar
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val segments = 10
                val stepsPerSegment = (stepCount.toFloat() / segments).coerceAtLeast(1f)

                repeat(segments) { index ->
                    // Range of steps that fall into this segment
                    val start = (index * stepsPerSegment).toInt()
                    val end = ((index + 1) * stepsPerSegment).toInt().coerceAtMost(stepCount)

                    // Check if any step in this segment is active
                    val isActive = steps.subList(start, end).any { it }

                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(if (isActive) 16.dp else 8.dp)
                            .background(
                                if (isActive)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun StepGrid(
    steps: List<Boolean>,
    columns: Int,
    stepDurationMs: Long,
    currentStep: Int,
    animatedStepAlpha: Float,
    onStepToggle: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        itemsIndexed(steps) { index, isActive ->
            StepButton(
                index = index,
                isActive = isActive,
                isCurrent = index == currentStep,
                stepDurationMs = stepDurationMs,
                animatedStepAlpha = animatedStepAlpha,
                onClick = { onStepToggle(index) }
            )
        }
    }
}

@Composable
private fun StepButton(
    index: Int,
    isActive: Boolean,
    isCurrent: Boolean,
    stepDurationMs: Long,
    animatedStepAlpha: Float,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "buttonScale"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            isCurrent -> 8.dp
            isActive -> 4.dp
            else -> 1.dp
        },
        label = "buttonElevation"
    )

    Surface(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        color = when {
            isCurrent -> MaterialTheme.colorScheme.tertiary.copy(alpha = animatedStepAlpha)
            isActive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isActive || isCurrent) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_watch_vibration),
                    contentDescription = "Active step",
                    tint = when {
                        isCurrent -> MaterialTheme.colorScheme.onTertiary
                        isActive -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${"%.1f".format((index) * stepDurationMs / 1000f)}s",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .drawWithContent {
                            drawContent()
                            drawRoundRect(
                                color = Color.White.copy(alpha = animatedStepAlpha * 0.8f),
                                style = Stroke(width = 3.dp.toPx()),
                                cornerRadius = CornerRadius(14.dp.toPx())
                            )
                        }
                )
            }
        }
    }
}

@Composable
private fun ControlButtons(
    steps: List<Boolean>,
    isPlaying: Boolean,
    onClear: () -> Unit,
    onRandomize: () -> Unit,
    onPlay: () -> Unit,
    onStop: () -> Unit
) {
    val hasActiveSteps = steps.contains(true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Clear button
        IconButton(
            onClick = onClear,
            enabled = hasActiveSteps && !isPlaying,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_reset),
                contentDescription = "Clear pattern"
            )
        }

        // Randomize button
        IconButton(
            onClick = onRandomize,
            enabled = !isPlaying,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_shuffle),
                contentDescription = "Randomize pattern"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Play/Test button
        Button(
            onClick = if (isPlaying) onStop else onPlay,
            enabled = hasActiveSteps,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) ImageVector.vectorResource(R.drawable.ic_stop) else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isPlaying) "Stop" else "Play",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}