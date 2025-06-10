package com.pagzone.sonavi.ui.screen.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pagzone.sonavi.ui.component.RecordSoundButton
import com.pagzone.sonavi.ui.component.RecordUploadSegmentedToggle

@Preview(showBackground = true)
@Composable
fun AddSoundPage(modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (selectedIndex) {
            0 -> {
                RecordSoundButton(
                    modifier = Modifier
                        .weight(1.5f)
                )
            }
            1 -> {
                Text(text = "Upload", modifier = Modifier.weight(1.5f))
            }
        }

        RecordUploadSegmentedToggle(
            selectedIndex = selectedIndex,
            onSelectionChanged = { selectedIndex = it },
            modifier = Modifier
                .weight(0.5f)
        )
    }
}

