package com.pagzone.sonavi.ui.screen.page

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pagzone.sonavi.ui.component.CustomSearchBar
import com.pagzone.sonavi.ui.component.SoundFilterChips
import com.pagzone.sonavi.viewmodel.SoundPreferencesViewModel

@Composable
fun LibraryPage(viewModel: SoundPreferencesViewModel, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    val filters = listOf("All", "Recorded", "Uploaded", "Built-in")
    val prefs by viewModel.preferencesFlow.collectAsState()

    LaunchedEffect(prefs) {
        Log.d("LibraryPage", "prefs: $prefs")
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

        // TODO: Get list of sounds based on filter
//        val filteredSounds = getSounds().filter { sound ->
//            selectedFilter == "All" || sound.category == selectedFilter
//        }

//        LazyColumn {
//            items(filteredSounds) { sound ->
//                SoundItem(sound)
//            }
//        }


        LazyColumn {
            items(prefs) { pref ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(pref.label, modifier = Modifier.weight(1f))
                    Switch(
                        checked = pref.enabled,
                        onCheckedChange = { enabled -> viewModel.toggleSound(pref.label, enabled) }
                    )
                    if (!pref.enabled) {
                        TextButton(onClick = { viewModel.snoozeSound(pref.label, 30) }) {
                            Text("Snooze 30m")
                        }
                    }
                }
            }
        }
    }
}