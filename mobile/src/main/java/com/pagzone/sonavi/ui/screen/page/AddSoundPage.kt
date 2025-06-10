package com.pagzone.sonavi.ui.screen.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.pagzone.sonavi.R
import com.pagzone.sonavi.ui.component.CustomAlertDialog
import com.pagzone.sonavi.ui.component.RecordSoundButton
import com.pagzone.sonavi.ui.component.RecordUploadSegmentedToggle

@Preview(showBackground = true)
@Composable
fun AddSoundPage(modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.End),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_help_outline),
                contentDescription = "Help"
            )
        }

        CustomAlertDialog(
            showDialog = showDialog,
            onDismissRequest = { showDialog = false },
            title = "Add Sounds",
            text = "Tap the red circle to record up to " +
                    "10 seconds of nearby sounds. Alternatively, " +
                    "use the Upload button to add an audio file or " +
                    "explore developer-created sounds with vibration " +
                    "patterns via the Samples button.",
            confirmText = "OK",
            onConfirmClick = { showDialog = false }
        )

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

