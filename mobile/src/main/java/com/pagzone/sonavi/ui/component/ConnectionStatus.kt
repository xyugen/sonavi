package com.pagzone.sonavi.ui.component

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pagzone.sonavi.R
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import com.pagzone.sonavi.ui.theme.Green50

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

    val context = LocalContext.current

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Welcome!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    onStartWearableActivityClick()
                    Toast.makeText(
                        context,
                        "Reconnecting...",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                },
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = true,
            ) {
                Icon(
                    imageVector = ImageVector
                        .vectorResource(R.drawable.ic_sync),
                    contentDescription = "person"
                )
            }
        }
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