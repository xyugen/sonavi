package com.pagzone.sonavi.ui.screen.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pagzone.sonavi.model.SoundPreference
import com.pagzone.sonavi.ui.component.CustomSearchBar
import com.pagzone.sonavi.ui.component.SoundFilterChips
import com.pagzone.sonavi.viewmodel.SoundPreferencesViewModel

@Composable
fun LibraryPage(viewModel: SoundPreferencesViewModel, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    val filters = listOf("All", "Recorded", "Uploaded", "Built-in")
    val prefs by viewModel.preferencesFlow.collectAsState()

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

        LazyColumn {
            items(
                items = prefs,
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
    val iconColor = if (sound.enabled) Color(0xFF0288D1) else Color.Gray

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFB3E5FC)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE1F5FE), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                IconToggleButton(
                    checked = sound.enabled,
                    onCheckedChange = { onToggleClick(it) }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = if (sound.enabled) "Enabled" else "Disabled",
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title + Date
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = sound.label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = "02-02-25", // Replace with actual date property
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            // Overflow menu (3 dots)
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color.Black
                )
            }
        }
    }
}