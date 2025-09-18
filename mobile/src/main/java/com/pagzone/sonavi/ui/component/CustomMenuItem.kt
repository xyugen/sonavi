package com.pagzone.sonavi.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

@Composable
fun CustomMenuItem(
    text: String,
    icon: Int,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDestructive)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.onSurface

    val iconColor = if (isDestructive)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    DropdownMenuItem(
        onClick = onClick,
        modifier = modifier,
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = icon),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    )
}