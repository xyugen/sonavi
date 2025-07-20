package com.pagzone.sonavi.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.pagzone.sonavi.R
import com.pagzone.sonavi.presentation.theme.AppTheme

@Preview(device = "id:wearos_small_round")
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    isConnected: Boolean = true,
    onStartListening: () -> Unit = {}
) {
    LaunchedEffect(isConnected) {
        Log.d("WearNavGraph", "isConnected changed to: $isConnected")
    }

    val subtext = if (isConnected) {
        "Click the button above\nto start listening"
    } else {
        "Connect to your phone\nto start listening"
    }

    Scaffold(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .padding(18.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Welcome,\nUser!",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            StartButton(
                isEnabled = isConnected,
                onClick = onStartListening
            )
            Text(
                text = subtext,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StartButton(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    IconButton(
        modifier = modifier.size(76.dp),
        onClick = {
            onClick()
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = AppTheme.colors.primary,
            contentColor = Color.White,
            disabledContainerColor = AppTheme.colors.disabled
        ),
        enabled = isEnabled,
        content = {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_mic),
                contentDescription = "Start listening"
            )
        }
    )
}