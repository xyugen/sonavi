package com.pagzone.sonavi.ui.screen.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import com.pagzone.sonavi.ui.component.ConnectionStatus

@Preview(showBackground = true)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: ClientDataViewModel = viewModel(),
    onStartWearableActivityClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        ConnectionStatus(
            viewModel = viewModel
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sound History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        LazyColumn(
            content = {
                items(4) {
                    Surface(
                        color = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(16.dp),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Column {
                            Text(
                                text = "Truck Horn",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "20 minutes ago",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        )
    }
}