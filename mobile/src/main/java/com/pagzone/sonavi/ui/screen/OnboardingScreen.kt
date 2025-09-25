package com.pagzone.sonavi.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pagzone.sonavi.R
import com.pagzone.sonavi.domain.PermissionManager

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf(/*"Welcome",*/ "Permissions"/*, "Setup"*/)

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentStep) {
            // TODO: 0 -> WelcomeStep { currentStep++ }
            0 -> PermissionsStep { onComplete() }
            // TODO: 2 -> SetupStep { onComplete() }
        }

        // Progress indicator
        if (currentStep < steps.size) {
            LinearProgressIndicator(
                progress = { (currentStep + 1) / steps.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun PermissionsStep(onNext: () -> Unit) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    var showDetails by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top spacing
        Spacer(modifier = Modifier.height(40.dp))

        // Hero illustration area
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 200f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_sms_filled),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main headline
        Text(
            text = "Emergency Alerts",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Send SMS alerts to your contacts when critical sounds are detected",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Feature highlights
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FeatureHighlight(
                icon = ImageVector.vectorResource(R.drawable.ic_security),
                title = "Automatic Response",
                description = "Alert contacts when you can't respond to emergencies"
            )

            FeatureHighlight(
                icon = ImageVector.vectorResource(R.drawable.ic_timer),
                title = "Smart Cooldowns",
                description = "Prevents message spam with built-in delays"
            )

            FeatureHighlight(
                icon = ImageVector.vectorResource(R.drawable.ic_brand_awareness),
                title = "Full Control",
                description = "Choose which sounds trigger alerts and customize messages"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Expandable details section
        AnimatedVisibility(
            visible = showDetails,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Privacy & Usage",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "SMS permission is only used for emergency alerts you configure. " +
                                "No messages are sent without your explicit setup of emergency contacts and critical sounds.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Toggle details button
        TextButton(
            onClick = { showDetails = !showDetails },
//            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = if (showDetails) "Hide Details" else "Privacy Details",
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector =
                    if (showDetails) ImageVector.vectorResource(R.drawable.ic_expand_less)
                    else ImageVector.vectorResource(R.drawable.ic_expand_more),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }

        // Action buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (PermissionManager.checkAndRequestSmsPermission(activity)) {
                        onNext()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_security),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Enable Alerts",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            OutlinedButton(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = "Skip for Now",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FeatureHighlight(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}