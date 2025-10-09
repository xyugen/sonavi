package com.pagzone.sonavi.ui.screen.page

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pagzone.sonavi.R
import com.pagzone.sonavi.model.ClassificationResult
import com.pagzone.sonavi.model.SoundStats
import com.pagzone.sonavi.ui.component.RelativeTimeText
import com.pagzone.sonavi.ui.theme.Amber50
import com.pagzone.sonavi.ui.theme.Green50
import com.pagzone.sonavi.ui.theme.Red50
import com.pagzone.sonavi.viewmodel.ClassificationResultViewModel
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import kotlin.math.roundToInt

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: ClientDataViewModel = viewModel(),
    classificationViewModel: ClassificationResultViewModel = viewModel()
) {
    val classificationResult by classificationViewModel.classificationResults.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Optimize data processing with derivedStateOf
    val recentResults by remember {
        derivedStateOf {
            classificationResult.takeLast(15) // Show more items for better UX
        }
    }

    val todayStats by remember {
        derivedStateOf {
            val today = System.currentTimeMillis()
            val oneDayAgo = today - (24 * 60 * 60 * 1000)
            val todayResults = classificationResult.filter { it.timestamp >= oneDayAgo }

            SoundStats(
                totalSounds = todayResults.size,
                highConfidenceCount = todayResults.count { it.confidence >= 0.9 },
                mostFrequentSound = todayResults.groupBy { it.label }
                    .maxByOrNull { it.value.size }?.key ?: "None",
                criticalSounds = todayResults.count { it.isCritical }
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section with Welcome & Connection Status
        item {
            HeroSection(
                viewModel = viewModel,
                isConnected = isConnected,
                onRetryClick = {
                    viewModel.retryConnection()
                    Toast.makeText(
                        context,
                        "Retrying connection...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        // Stats Overview Card
        item {
            StatsOverviewCard(stats = todayStats)
        }

        // Sound History Header with Enhanced Design
        item {
            SectionHeader(
                title = "Recent Activity",
                subtitle = "${recentResults.size} sounds detected",
                icon = R.drawable.ic_library_music
            )
        }

        // Optimized Sound History List
        item {
            if (recentResults.isEmpty()) {
                EmptyStateCard()
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Tight spacing
                ) {
                    recentResults.reversed().forEachIndexed { index, result ->
                        SoundHistoryItem(
                            result = result,
                            isLatest = index == 0,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HeroSection(
    viewModel: ClientDataViewModel,
    isConnected: Boolean,
    onRetryClick: () -> Unit
) {
    val deviceName by viewModel.deviceName.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Modern Status Card
        ConnectionStatusCard(
            isConnected = isConnected,
            deviceName = deviceName ?: "Unknown Device",
            onRetryClick = onRetryClick
        )
    }
}

@Composable
private fun ConnectionStatusCard(
    isConnected: Boolean,
    deviceName: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animated Status Icon
        ConnectionStatusIcon(isConnected = isConnected)

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = if (isConnected) deviceName else "Not Connected",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pulsing dot
                if (isConnected) {
                    PulsingDot()
                } else {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                MaterialTheme.colorScheme.error,
                                CircleShape
                            )
                    )
                }

                Text(
                    text = if (isConnected) "Connected" else "Tap to reconnect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        // Right side icon
        if (isConnected) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_circle_check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                onClick = onRetryClick
            ) {
                Text(
                    text = "Retry",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusIcon(isConnected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_animation")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isConnected) {
            // Pulsing glow ring
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Icon container
        Surface(
            shape = CircleShape,
            color = if (isConnected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = if (!isConnected) Modifier.rotate(rotation) else Modifier
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (isConnected) R.drawable.ic_watch else R.drawable.ic_watch_off
                    ),
                    contentDescription = null,
                    tint = if (isConnected) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(scale)
                .background(
                    Color(0xFF10B981).copy(alpha = alpha * 0.3f),
                    CircleShape
                )
        )
        // Inner dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color(0xFF10B981), CircleShape)
        )
    }
}

@Composable
private fun StatsOverviewCard(stats: SoundStats) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Today's Activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatItem(
                title = "Detected",
                value = "${stats.totalSounds}",
                icon = R.drawable.ic_volume_up,
                modifier = Modifier.weight(1f)
            )

            StatItem(
                title = "Critical",
                value = "${stats.criticalSounds}",
                icon = R.drawable.ic_emergency_home_filled,
                modifier = Modifier.weight(1f),
                tint = MaterialTheme.colorScheme.error
            )
        }

        // Most Frequent (only if exists)
        if (stats.mostFrequentSound != "None") {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_trending_up),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Most detected: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = stats.mostFrequentSound,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SoundHistoryItem(
    result: ClassificationResult,
    isLatest: Boolean,
    modifier: Modifier = Modifier,
) {
    val confidence = (result.confidence * 100).roundToInt()

    val (confidenceText, confidenceColor, confidenceBgColor) = remember(result.confidence) {
        when {
            result.confidence >= 0.9 -> Triple(
                "High",
                Green50,
                Green50.copy(alpha = 0.12f)
            )

            result.confidence >= 0.75 -> Triple(
                "Medium",
                Amber50,
                Amber50.copy(alpha = 0.12f)
            )

            else -> Triple(
                "Low",
                Red50,
                Red50.copy(alpha = 0.12f)
            )
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (result.isCritical) {
            MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
        } else if (isLatest) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "backgroundColor"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        tonalElevation = if (isLatest) 0.5.dp else 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color =
                    if (result.isCritical) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector =
                            if (result.isCritical) ImageVector.vectorResource(R.drawable.ic_emergency_home_filled)
                            else ImageVector.vectorResource(R.drawable.ic_audio_lines),
                        contentDescription = null,
                        tint =
                            if (result.isCritical) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Time and confidence row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sound label
                    Text(
                        text = result.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (result.isCritical) {
                        Box(
                            modifier = Modifier
                                .size(2.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    CircleShape
                                )
                        )

                        Text(
                            text = "Critical",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Time and confidence row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RelativeTimeText(
                        timestamp = result.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Box(
                        modifier = Modifier
                            .size(2.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = confidenceBgColor
                    ) {
                        Text(
                            text = confidenceText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = confidenceColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Right side - fixed width
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLatest) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        )
                    }

                    Text(
                        text = "$confidence%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Compact confidence bar
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(3.dp)
                        .background(
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(1.5.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(result.confidence)
                            .background(
                                confidenceColor,
                                RoundedCornerShape(1.5.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_volume_off),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No sounds detected yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Make sure your watch is connected and monitoring is active",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}