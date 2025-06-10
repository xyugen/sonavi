package com.pagzone.sonavi.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun CustomAlertDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmText: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        showDialog -> {
            AlertDialog(
                onDismissRequest = { onDismissRequest() },
                modifier = modifier,
                title = {
                    Text(title)
                },
                text = {
                    Text(
                        text = text,
                        textAlign = TextAlign.Justify
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onConfirmClick() }) {
                        Text(
                            text = confirmText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}