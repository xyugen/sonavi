package com.pagzone.sonavi.ui.screen.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pagzone.sonavi.model.ClassificationResult
import com.pagzone.sonavi.ui.component.ConnectionStatus
import com.pagzone.sonavi.util.Helper.Companion.formatTimestamp
import com.pagzone.sonavi.viewmodel.ClassificationResultViewModel
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import kotlin.math.roundToInt

@Preview(showBackground = true)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: ClientDataViewModel = viewModel(),
    classificationViewModel: ClassificationResultViewModel = viewModel()
) {
    val classificationResult by classificationViewModel.classificationResults.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        ConnectionStatus(viewModel = viewModel)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sound History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(classificationResult.takeLast(10)) { result ->
                SoundHistoryItem(result = result)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SoundHistoryItem(
    result: ClassificationResult,
    modifier: Modifier = Modifier,
) {
    val confidence = (result.confidence * 100).roundToInt()
    val (confidenceText, confidenceColor) = remember(result.confidence) {
        when {
            result.confidence >= 0.9 -> "High confidence" to Color(0xFF26AB15)
            result.confidence >= 0.8 -> "Medium confidence" to Color(0xFFB29036)
            result.confidence >= 0.0 -> "Low confidence" to Color(0xFFC75A28)
            else -> "Unknown confidence" to Color.Transparent
        }
    }

    val formattedDate = remember(result.timestamp) {
        formatTimestamp(result.timestamp)
    }

    Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = result.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$confidence%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = confidenceText,
                    style = MaterialTheme.typography.bodySmall,
                    color = confidenceColor
                )
            }
        }
    }
}