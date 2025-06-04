package com.pagzone.sonavi.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.pagzone.sonavi.R

@Composable
fun RecordUploadToggle(modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf(
        SegmentedButtonItem(
            "Record",
            ImageVector.vectorResource(R.drawable.ic_mic_none)
        ),
        SegmentedButtonItem(
            "Upload",
            ImageVector.vectorResource(R.drawable.ic_upload_file)
        )
    )

    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, buttonItem ->
            SegmentedButton(
                modifier = modifier,
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { selectedIndex = index },
                selected = index == selectedIndex,
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.secondary,
                    activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    inactiveContainerColor = Color.White,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                label = {
                    Text(
                        text = buttonItem.label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                icon = {
                    if (selectedIndex == index) {
                        Icon(
                            imageVector = buttonItem.icon,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    }
}

data class SegmentedButtonItem(
    val label: String,
    val icon: ImageVector
)