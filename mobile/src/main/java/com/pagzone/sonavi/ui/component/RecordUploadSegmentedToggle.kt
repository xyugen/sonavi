package com.pagzone.sonavi.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.pagzone.sonavi.R

@Composable
fun RecordUploadSegmentedToggle(
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
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

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, buttonItem ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onSelectionChanged(index) },
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