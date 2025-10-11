package com.pagzone.sonavi.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.psoffritti.taptargetcompose.TapTargetDefinition
import com.psoffritti.taptargetcompose.TapTargetStyle
import com.psoffritti.taptargetcompose.TextDefinition

@Composable
fun getStandardTapTargetDefinition(
    precedence: Int,
    title: String,
    description: String,
    onTargetClick: () -> Unit = {},
    onTargetCancel: () -> Unit = {},
): TapTargetDefinition {
    return TapTargetDefinition(
        precedence = precedence,
        title = TextDefinition(
            text = title,
            textStyle = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary
        ),
        description = TextDefinition(
            text = description,
            textStyle = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        ),
        tapTargetStyle = TapTargetStyle(
            backgroundColor = MaterialTheme.colorScheme.primary,
            tapTargetHighlightColor = MaterialTheme.colorScheme.onPrimary
        ),
        onTargetClick = onTargetClick,
        onTargetCancel = onTargetCancel,
    )
}