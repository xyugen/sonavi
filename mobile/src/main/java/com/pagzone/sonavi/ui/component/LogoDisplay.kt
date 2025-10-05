package com.pagzone.sonavi.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.pagzone.sonavi.R

@Composable
fun LogoDisplay(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier,
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo),
        tint = MaterialTheme.colorScheme.primary,
        contentDescription = "Sonavi Logo"
    )
}