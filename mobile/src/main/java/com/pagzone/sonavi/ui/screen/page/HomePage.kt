package com.pagzone.sonavi.ui.screen.page

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pagzone.sonavi.R
import com.pagzone.sonavi.model.ClassificationResult
import com.pagzone.sonavi.model.SoundStats
import com.pagzone.sonavi.util.Helper.Companion.formatTimestamp
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
                    .maxByOrNull { it.value.size }?.key ?: "None"
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
                isConnected = isConnected
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
    isConnected: Boolean
) {
    val deviceName by viewModel.deviceName.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Your sound monitoring is active",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Animated pulse for connection status
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_alpha"
                )

                Surface(
                    shape = CircleShape,
                    color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(if (isConnected) 1f else pulseAlpha)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            if (isConnected) R.drawable.ic_check_circle else R.drawable.ic_cancel
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connection Status Bar
            ConnectionStatusBar(
                isConnected = isConnected,
                deviceName = deviceName ?: ""
            )
        }
    }
}

@Composable
private fun ConnectionStatusBar(
    isConnected: Boolean,
    deviceName: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isConnected)
            Color(0xFF4CAF50).copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    if (isConnected) R.drawable.ic_watch else R.drawable.ic_watch_off
                ),
                contentDescription = null,
                tint = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isConnected) "Connected to $deviceName" else "Watch disconnected",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isConnected) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(8.dp)
                ) {}
            }
        }
    }
}

@Composable
private fun StatsOverviewCard(stats: SoundStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.70f
            )
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp, MaterialTheme.colorScheme.surface.copy(
                alpha = 0.9f
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_analytics_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Today's Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(
                    title = "Total Sounds",
                    value = "${stats.totalSounds}",
                    icon = R.drawable.ic_volume_up,
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    title = "High Confidence",
                    value = "${stats.highConfidenceCount}",
                    icon = R.drawable.ic_verified,
                    modifier = Modifier.weight(1f)
                )
            }

            if (stats.mostFrequentSound != "None") {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Most frequent: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = stats.mostFrequentSound,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
    val (confidenceText, confidenceColor) = remember(result.confidence) {
        when {
            result.confidence >= 0.9 -> "High confidence" to Color(0xFF26AB15)
            result.confidence >= 0.8 -> "Medium confidence" to Color(0xFFB29036)
            result.confidence >= 0.0 -> "Low confidence" to Color(0xFFC75A28)
            else -> "Unknown confidence" to Color.Transparent
        }
    }

    val formattedDate = remember(result.timestamp) {
        formatTimestamp(result.timestamp)
    }

    val backgroundColor = if (isLatest) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = result.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$confidence%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = confidenceText,
                    style = MaterialTheme.typography.bodySmall,
                    color = confidenceColor
                )
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