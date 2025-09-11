package com.pagzone.sonavi.ui.screen.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pagzone.sonavi.R
import com.pagzone.sonavi.model.SoundPreference
import com.pagzone.sonavi.ui.component.CustomSearchBar
import com.pagzone.sonavi.ui.component.SoundFilterChips
import com.pagzone.sonavi.viewmodel.SoundPreferencesViewModel

@Composable
fun LibraryPage(viewModel: SoundPreferencesViewModel, modifier: Modifier = Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    val filters = listOf("All", "Recorded", "Uploaded", "Built-in")
    var selectedFilter by rememberSaveable { mutableStateOf<String?>("All") }

    val prefs by viewModel.preferencesFlow
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Filter sounds based on search query
    val filteredPrefs = remember(prefs, query) {
        if (query.isBlank()) {
            prefs
        } else {
            prefs.filter { pref ->
                pref.label.contains(query, ignoreCase = true)
            }
        }
    }

    // Check if all sounds are enabled
    val allEnabled = remember(filteredPrefs) {
        filteredPrefs.isNotEmpty() && filteredPrefs.all { it.enabled }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        CustomSearchBar(
            query,
            onQueryChange = { query = it },
            placeholder = {
                Text("Search sounds...")
            }
        )

        SoundFilterChips(
            filters = filters,
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )

        // Select All/Deselect All Button
        if (filteredPrefs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredPrefs.size} sound${if (filteredPrefs.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = {
                        val targetState = !allEnabled
                        filteredPrefs.forEach { pref ->
                            viewModel.toggleSound(pref.label, targetState)
                        }
                    }
                ) {
                    Text(
                        text = if (allEnabled) "Disable All" else "Enable All",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredPrefs,
                key = { it.label }
            ) { pref ->
                SoundCard(
                    sound = pref,
                    onToggleClick = { enabled ->
                        Log.d("LibraryPage", "Toggling ${pref.label} to enabled: $enabled")
                        viewModel.toggleSound(pref.label, enabled)
                    },
                    onMenuClick = {
                        // Handle menu click
                    }
                )
            }
        }
    }
}

@Composable
fun SoundCard(
    sound: SoundPreference,
    modifier: Modifier = Modifier,
    onToggleClick: (enabled: Boolean) -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val cardBackgroundColor = if (sound.enabled) MaterialTheme.colorScheme.secondary else
        MaterialTheme.colorScheme.surfaceVariant
    val iconButtonColor = if (sound.enabled) MaterialTheme.colorScheme.primary else
        MaterialTheme.colorScheme.secondary
    val icon = if (sound.enabled)
        ImageVector.vectorResource(id = R.drawable.ic_sensors) else
        ImageVector.vectorResource(id = R.drawable.ic_sensors_off)

    val iconScale = if (sound.enabled) 1.1f else 1f
    val contentAlpha = if (sound.enabled) 1f else 0.7f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggleClick(!sound.enabled) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (sound.enabled) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sound Icon - compact size
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconButtonColor)
                    .scale(iconScale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = sound.label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content section - compact
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title row with sound name and type badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sound.label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Built-in type badge - smaller
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Built-in",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Compact status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (sound.enabled) Color(0xFF4CAF50) else Color(0xFFE18D17)
                            )
                    )

                    Text(
                        text = if (sound.enabled) "Listening" else "Paused",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = if (sound.enabled) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFE18D17)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Compact menu button
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}