package com.pagzone.sonavi.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    title: String = "",
    isListenModeChecked: Boolean = false,
    isListenModeEnabled: Boolean = false,
    onListenModeChange: (Boolean) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 21.dp)
            .padding(bottom = 14.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo
            LogoDisplay(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterStart)
            )

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )

            // Theme toggle
            ThemeToggleButton(
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        ListenModeToggle(
            checked = isListenModeChecked,
            enabled = isListenModeEnabled,
            onChange = onListenModeChange
        )
    }
}