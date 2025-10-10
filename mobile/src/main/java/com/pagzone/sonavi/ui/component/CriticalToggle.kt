package com.pagzone.sonavi.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pagzone.sonavi.R
import com.pagzone.sonavi.ui.screen.page.HelpCard
import com.pagzone.sonavi.ui.theme.Amber50

@Composable
fun CriticalToggle(
    isCriticalEnabled: Boolean,
    selectedCooldown: Int,
    onCriticalChanged: (Boolean) -> Unit,
    onCooldownChanged: (Int) -> Unit,
    shouldShowCriticalInfoDialog: Boolean,
    modifier: Modifier = Modifier,
    onDontShowAgainClick: () -> Unit = {},
    showHelp: Boolean = false
) {
    var showCriticalDialog by remember { mutableStateOf(false) }
    var dontShowAgain by remember { mutableStateOf(false) }

    val switchScale by animateFloatAsState(
        targetValue = if (isCriticalEnabled) 1.05f else 1f,  // ✅ Use prop
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "switch_scale"
    )

    Column {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCriticalEnabled)
                            ImageVector.vectorResource(R.drawable.ic_emergency_home_filled)
                        else ImageVector.vectorResource(R.drawable.ic_emergency_home),
                        contentDescription = null,
                        tint = if (isCriticalEnabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Critical Sound",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (isCriticalEnabled) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isCriticalEnabled,
                    onCheckedChange = { newValue ->
                        if (newValue) {
                            // Trying to enable
                            if (shouldShowCriticalInfoDialog) {
                                showCriticalDialog = true
                            } else {
                                onCriticalChanged(true)
                            }
                        } else {
                            // Disabling
                            onCriticalChanged(false)
                        }
                    },
                    modifier = Modifier.scale(switchScale),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
            }

            if (showHelp)
                Spacer(modifier = Modifier.height(12.dp))

            HelpCard(
                showHelp = showHelp,
                text = "Enable emergency SMS alerts to your contacts when this sound is detected." +
                        " A cooldown prevents spam during continuous detection."
            )
        }

        // Cooldown selection
        AnimatedVisibility(
            visible = isCriticalEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                EmergencyCooldownSelector(
                    selectedCooldown = selectedCooldown,
                    onCooldownChanged = onCooldownChanged,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }

    if (showCriticalDialog) {
        AlertDialog(
            onDismissRequest = { showCriticalDialog = false },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_triangle_alert),
                    contentDescription = null,
                    tint = Amber50
                )
            },
            title = { Text("Enable Critical Sound?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "This will send automatic SMS alerts to your emergency contacts when this sound is detected.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Please inform your contacts that they may receive automated safety alerts from your number.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dontShowAgain = !dontShowAgain }
                    ) {
                        Checkbox(
                            checked = dontShowAgain,
                            onCheckedChange = { dontShowAgain = it }
                        )
                        Text(
                            text = "Don't show again",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (dontShowAgain) {
                            onDontShowAgainClick()
                        }
                        showCriticalDialog = false
                        onCriticalChanged(true)
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCriticalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmergencyCooldownSelector(
    selectedCooldown: Int,
    onCooldownChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cooldownOptions = listOf(1, 2, 5, 10, 15, 30, 60)
    val currentIndex = cooldownOptions.indexOf(selectedCooldown).takeIf { it >= 0 } ?: 2

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_timer),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Message Cooldown",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = {
                    if (currentIndex > 0) {
                        onCooldownChanged(cooldownOptions[currentIndex - 1])
                    }
                },
                enabled = currentIndex > 0,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_remove),
                    contentDescription = "Decrease",
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = when (selectedCooldown) {
                    60 -> "1h"
                    1 -> "1m"
                    else -> "${selectedCooldown}m"
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.defaultMinSize(minWidth = 32.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {
                    if (currentIndex < cooldownOptions.size - 1) {
                        onCooldownChanged(cooldownOptions[currentIndex + 1])
                    }
                },
                enabled = currentIndex < cooldownOptions.size - 1,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}