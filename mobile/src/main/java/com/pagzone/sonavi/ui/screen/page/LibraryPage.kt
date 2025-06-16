package com.pagzone.sonavi.ui.screen.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pagzone.sonavi.ui.component.CustomSearchBar
import com.pagzone.sonavi.ui.component.SoundFilterChips

@Preview(showBackground = true)
@Composable
fun LibraryPage(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    val filters = listOf("All", "Recorded", "Uploaded", "Built-in")

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
    }
}