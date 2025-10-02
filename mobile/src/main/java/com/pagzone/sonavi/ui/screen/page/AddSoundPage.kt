package com.pagzone.sonavi.ui.screen.page

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pagzone.sonavi.R
import com.pagzone.sonavi.util.AudioRecorder
import com.pagzone.sonavi.viewmodel.SoundViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun AddSoundPage(
    modifier: Modifier = Modifier,
    viewModel: SoundViewModel = hiltViewModel(),
    onSoundCreated: () -> Unit = {}
) {
    val context = LocalContext.current
    val audioRecorder = remember { AudioRecorder(context) }
    val coroutineScope = rememberCoroutineScope()

    var soundName by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(RecordingStep.NAME) }
    var recordings by remember { mutableStateOf<List<FloatArray>>(emptyList()) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentAudioAmplitude by remember { mutableFloatStateOf(0f) }

    // Monitor audio amplitude during recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                currentAudioAmplitude = audioRecorder.getCurrentAmplitude()
                delay(50) // Update 20 times per second
            }
        } else {
            currentAudioAmplitude = 0f
        }
    }

    // Timer for recording duration
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording && recordingDuration < 10) {
                delay(1000)
                recordingDuration++
            }
            if (recordingDuration >= 10) {
                val audioData = audioRecorder.stopRecording()
                if (audioData.size >= 16000) {
                    recordings = recordings + audioData
                }
                isRecording = false
                recordingDuration = 0
            }
        } else {
            recordingDuration = 0
        }
    }

    fun resetAddSound() {
        soundName = ""
        currentStep = RecordingStep.NAME
        recordings = emptyList()
        isRecording = false
        recordingDuration = 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Create Custom Sound",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            IconButton(onClick = { showHelpDialog = true }) {
                Icon(Icons.Default.Info, contentDescription = "Help")
            }
        }

        StepProgressIndicator(currentStep, Modifier.fillMaxWidth())

        when (currentStep) {
            RecordingStep.NAME -> {
                NameInputStep(
                    soundName = soundName,
                    onNameChanged = { soundName = it },
                    onNext = {
                        if (soundName.isNotBlank()) {
                            currentStep = RecordingStep.RECORD
                        }
                    }
                )
            }

            RecordingStep.RECORD -> {
                RecordingSamplesStep(
                    recordings = recordings,
                    isRecording = isRecording,
                    recordingDuration = recordingDuration,
                    currentAmplitude = currentAudioAmplitude,
                    onStartRecording = {
                        val success = audioRecorder.startRecording { error ->
                            errorMessage = error
                        }
                        if (success) {
                            isRecording = true
                            errorMessage = null
                        }
                    },
                    onStopRecording = {
                        val audioData = audioRecorder.stopRecording()
                        if (audioData.size >= 16000) {
                            recordings = recordings + audioData
                            errorMessage = null
                        } else {
                            errorMessage = "Recording too short (minimum 1 second)"
                        }
                        isRecording = false
                        recordingDuration = 0
                    },
                    onDeleteRecording = { index ->
                        recordings = recordings.filterIndexed { i, _ -> i != index }
                    },
                    onDeleteLast = {
                        if (recordings.isNotEmpty()) {
                            recordings = recordings.dropLast(1)
                        }
                    },
                    onNext = {
                        if (recordings.size >= 3) {
                            currentStep = RecordingStep.PREVIEW
                        }
                    },
                    onBack = { currentStep = RecordingStep.NAME }
                )
            }

            RecordingStep.PREVIEW -> {
                PreviewAndSaveStep(
                    soundName = soundName,
                    recordingCount = recordings.size,
                    onSave = {
                        coroutineScope.launch {
                            viewModel.createCustomSound(soundName, recordings)
                            resetAddSound()
                            Toast.makeText(context, "Sound saved successfully", Toast.LENGTH_LONG)
                                .show()
                            onSoundCreated()
                        }
                    },
                    onBack = { currentStep = RecordingStep.RECORD }
                )
            }
        }
    }

    errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            delay(3000)
            errorMessage = null
        }
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    DisposableEffect(Unit) {
        onDispose {
            if (audioRecorder.isRecording()) {
                audioRecorder.stopRecording()
            }
        }
    }
}

enum class RecordingStep {
    NAME, RECORD, PREVIEW
}

@Composable
fun StepProgressIndicator(
    currentStep: RecordingStep,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepIndicatorItem(
            label = "Name",
            isActive = currentStep == RecordingStep.NAME,
            isCompleted = currentStep.ordinal > RecordingStep.NAME.ordinal
        )

        StepConnector(
            isCompleted = currentStep.ordinal > RecordingStep.NAME.ordinal,
            isActive = currentStep >= RecordingStep.RECORD,
            modifier = Modifier.weight(1f).offset(y = (-10).dp)
        )

        StepIndicatorItem(
            label = "Record",
            isActive = currentStep == RecordingStep.RECORD,
            isCompleted = currentStep.ordinal > RecordingStep.RECORD.ordinal
        )

        StepConnector(
            isCompleted = currentStep.ordinal > RecordingStep.RECORD.ordinal,
            isActive = currentStep >= RecordingStep.PREVIEW,
            modifier = Modifier.weight(1f).offset(y = (-10).dp)
        )

        StepIndicatorItem(
            label = "Save",
            isActive = currentStep == RecordingStep.PREVIEW,
            isCompleted = false
        )
    }
}

@Composable
private fun StepIndicatorItem(
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp) // bigger than before
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                isActive -> MaterialTheme.colorScheme.primary
                isCompleted -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun StepConnector(
    isCompleted: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(2.dp)
            .background(
                when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }
            )
    )
}


@Composable
private fun NameInputStep(
    soundName: String,
    onNameChanged: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "What sound do you want to detect?",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = soundName,
            onValueChange = onNameChanged,
            label = { Text("Sound name") },
            placeholder = { Text("e.g., My dog barking, Baby crying") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_lightbulb),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Choose a specific, unique sound. Generic sounds like \"music\" won't work well.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = soundName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun RecordingSamplesStep(
    recordings: List<FloatArray>,
    isRecording: Boolean,
    recordingDuration: Int,
    currentAmplitude: Float,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onDeleteRecording: (Int) -> Unit,
    onDeleteLast: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Record samples",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${recordings.size} recorded (minimum 3)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (recordings.isNotEmpty() && !isRecording) {
                TextButton(
                    onClick = onDeleteLast,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_undo),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Undo Last", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Text(
            text = "Record the same sound from different angles or distances.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Visual waveform feedback
        if (isRecording) {
            AudioVisualizerCard(
                amplitude = currentAmplitude,
                duration = recordingDuration
            )
        }

        // Recording list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 150.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(recordings) { index, _ ->
                RecordingItem(
                    index = index + 1,
                    onDelete = { onDeleteRecording(index) }
                )
            }

            // Show placeholders only if less than 3
            if (recordings.size < 3) {
                items(3 - recordings.size) { index ->
                    PlaceholderRecordingItem(index = recordings.size + index + 1)
                }
            }
        }

        // Record button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            RecordButton(
                isRecording = isRecording,
                onClick = if (isRecording) onStopRecording else onStartRecording,
                enabled = recordings.size < 10 // Max 10 samples
            )
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                enabled = !isRecording,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Back")
            }

            Button(
                onClick = onNext,
                enabled = recordings.size >= 3 && !isRecording,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AudioVisualizerCard(
    amplitude: Float,
    duration: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )
                    Text(
                        text = "Recording",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${duration}s / 10s",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Waveform visualization
            WaveformVisualizer(
                amplitude = amplitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }
    }
}

@Composable
private fun WaveformVisualizer(
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val amplitudeHistory = remember { mutableStateListOf<Float>().apply { repeat(50) { add(0f) } } }

    LaunchedEffect(amplitude) {
        if (amplitudeHistory.size >= 50) amplitudeHistory.removeAt(0)
        amplitudeHistory.add(amplitude)
    }

    Canvas(modifier = modifier) {
        val barWidth = size.width / amplitudeHistory.size
        val centerY = size.height / 2

        amplitudeHistory.forEachIndexed { index, amp ->
            val barHeight = (amp * size.height * 0.8f).coerceIn(4f, size.height)
            val x = index * barWidth
            drawRoundRect(
                color = Color(0xFF4CAF50),
                topLeft = Offset(x, centerY - barHeight / 2),
                size = Size(barWidth - 2f, barHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

@Composable
private fun RecordingItem(
    index: Int,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sample $index",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun PlaceholderRecordingItem(index: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        CircleShape
                    )
            )
            Text(
                text = "Sample $index",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recordPulse"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(if (isRecording) scale else 1f)
            .clip(CircleShape)
            .background(
                if (isRecording) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector =
                if (isRecording) ImageVector.vectorResource(R.drawable.ic_stop)
                else ImageVector.vectorResource(R.drawable.ic_mic_none),
            contentDescription = if (isRecording) "Stop recording" else "Start recording",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun PreviewAndSaveStep(
    soundName: String,
    recordingCount: Int,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Review your custom sound",
            style = MaterialTheme.typography.titleMedium
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    label = "Sound name",
                    value = soundName
                )
                InfoRow(
                    label = "Samples recorded",
                    value = "$recordingCount"
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Column {
                    Text(
                        text = "What happens next?",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Your samples will be processed to create a unique sound signature. " +
                                "The app will then alert you when it detects similar sounds.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Back", style = MaterialTheme.typography.labelMedium)
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Create Sound",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to Create Custom Sounds") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("1. Name your sound specifically (e.g., \"My baby crying\" not just \"baby\")")
                Text("2. Record 3-5 samples of the same sound from different positions or at different times")
                Text("3. Make sure the sound is clear and distinct in each recording")
                Text("4. The app will learn to recognize this specific sound")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

