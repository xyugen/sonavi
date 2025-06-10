package com.pagzone.sonavi.ui.screen.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pagzone.sonavi.ui.component.RecordSoundButton
import com.pagzone.sonavi.ui.component.RecordUploadSegmentedToggle

@Preview(showBackground = true)
@Composable
fun AddSoundPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RecordSoundButton(
            modifier = Modifier
                .weight(1.5f)
        )

        RecordUploadSegmentedToggle(
            modifier = Modifier
                .weight(0.5f)
        )
    }
}

