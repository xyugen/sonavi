package com.pagzone.sonavi.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pagzone.sonavi.ui.theme.Blue50
import com.pagzone.sonavi.ui.theme.Blue75
import com.pagzone.sonavi.ui.theme.Blue80

@Preview
@Composable
fun ListenModeToggle(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = false,
    onChange: (Boolean) -> Unit = {}
) {
    val gradient = Brush.horizontalGradient(
        listOf(
            Blue75,
            Blue50,
            Blue80
        )
    )

    var toggleState by remember { mutableStateOf(true) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient, shape = RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .padding(16.dp),
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Enable Listen Mode",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "Listens to nearby sounds.",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Switch(
                colors = SwitchDefaults.colors(
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Transparent,
                    uncheckedBorderColor = Color.White,
                    checkedThumbColor = Blue80,
                    checkedTrackColor = Color.White,
                    checkedBorderColor = Color.White
                ),
                checked = toggleState,
                onCheckedChange = {
                    toggleState = it
                    onChange(it)
                }
            )
        }
    }
}