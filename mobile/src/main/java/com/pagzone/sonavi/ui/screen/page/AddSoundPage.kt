package com.pagzone.sonavi.ui.screen.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pagzone.sonavi.ui.component.RecordUploadToggle

@Preview(showBackground = true)
@Composable
fun AddSoundPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        RecordUploadToggle()
    }
}