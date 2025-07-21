package com.pagzone.sonavi.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pagzone.sonavi.R
import com.pagzone.sonavi.ui.theme.Green50
import com.pagzone.sonavi.viewmodel.ClientDataViewModel

@Preview(showBackground = true)
@Composable
fun ConnectionStatus(
    modifier: Modifier = Modifier,
    viewModel: ClientDataViewModel = viewModel(),
    onStartWearableActivityClick: () -> Unit = {}
) {

    val isConnected by viewModel.isConnected.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()

    val color = if (isConnected) {
        Green50
    } else {
        MaterialTheme.colorScheme.error
    }

    val iconRes = if (isConnected) {
        R.drawable.ic_check_circle
    } else {
        R.drawable.ic_cancel
    }

    val text = if (isConnected) {
        "Connected to $deviceName"
    } else {
        "Disconnected"
    }

    Column(modifier = modifier) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ImageVector
                    .vectorResource(iconRes),
                contentDescription = "check",
                tint = color
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}