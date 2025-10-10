package com.pagzone.sonavi.ui.screen.page

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pagzone.sonavi.R
import com.pagzone.sonavi.domain.AudioQualityAnalyzer
import com.pagzone.sonavi.domain.RealtimeAudioQualityAnalyzer
import com.pagzone.sonavi.model.AudioSample
import com.pagzone.sonavi.model.AudioSource
import com.pagzone.sonavi.model.RealtimeQuality
import com.pagzone.sonavi.model.Settings
import com.pagzone.sonavi.ui.component.CriticalToggle
import com.pagzone.sonavi.ui.component.ThresholdSlider
import com.pagzone.sonavi.ui.component.VibrationPattern
import com.pagzone.sonavi.ui.component.VibrationPlayer
import com.pagzone.sonavi.ui.theme.Amber50
import com.pagzone.sonavi.ui.theme.Lime50
import com.pagzone.sonavi.ui.theme.Sky50
import com.pagzone.sonavi.util.AudioRecorder
import com.pagzone.sonavi.util.Constants.Classifier.CUSTOM_CONFIDENCE_THRESHOLD
import com.pagzone.sonavi.util.Constants.SoundProfile.DEFAULT_EMERGENCY_COOLDOWN_IN_MINUTES
import com.pagzone.sonavi.util.Constants.SoundProfile.DEFAULT_VIBRATION_PATTERN
import com.pagzone.sonavi.util.Helper.Companion.stepsToVibrationPattern
import com.pagzone.sonavi.viewmodel.ProfileSettingsViewModel
import com.pagzone.sonavi.viewmodel.SoundViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Preview(showBackground = true)
@Composable
fun AddSoundPage(
    modifier: Modifier = Modifier,
    viewModel: SoundViewModel = hiltViewModel(),
    profileSettingsViewModel: ProfileSettingsViewModel = hiltViewModel(),
    onSoundCreated: () -> Unit = {}
) {
    val context = LocalContext.current
    val audioRecorder = remember { AudioRecorder(context) }
    val coroutineScope = rememberCoroutineScope()

    var soundName by remember { mutableStateOf("") }
    var soundThreshold by remember { mutableFloatStateOf(CUSTOM_CONFIDENCE_THRESHOLD) }
    var isCriticalSoundEnabled by remember { mutableStateOf(false) }
    var selectedCooldown by remember { mutableIntStateOf(DEFAULT_EMERGENCY_COOLDOWN_IN_MINUTES) }
    var vibrationPattern by remember { mutableStateOf(emptyList<Long>()) }
    var selectedVibrationPattern by remember {
        mutableStateOf("Default")
    }

    var currentStep by remember { mutableStateOf(RecordingStep.NAME) }
    var samples by remember { mutableStateOf<List<AudioSample>>(emptyList()) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentAudioAmplitude by remember { mutableFloatStateOf(0f) }

    val settings by profileSettingsViewModel.settings.collectAsStateWithLifecycle()

    // File picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                viewModel.processAudioFile(it).collect { state ->
                    when (state) {
                        is SoundViewModel.ProcessingState.Processing -> {
                            errorMessage = "Processing audio file..."
                        }

                        is SoundViewModel.ProcessingState.Success -> {
                            samples = samples + state.sample
                            errorMessage = null
                        }

                        is SoundViewModel.ProcessingState.Error -> {
                            errorMessage = state.message
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    // Monitor audio amplitude during recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                currentAudioAmplitude = audioRecorder.getCurrentAmplitude()
                delay(50)
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
                    val quality = AudioQualityAnalyzer().analyzeQuality(audioData)
                    samples = samples + AudioSample(
                        source = AudioSource.Recording(audioData),
                        quality = quality
                    )
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
        isCriticalSoundEnabled = false
        soundThreshold = CUSTOM_CONFIDENCE_THRESHOLD
        selectedCooldown = DEFAULT_EMERGENCY_COOLDOWN_IN_MINUTES
        vibrationPattern = DEFAULT_VIBRATION_PATTERN
        selectedVibrationPattern = "Default"
        currentStep = RecordingStep.NAME
        samples = emptyList()
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
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = { showHelpDialog = true },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(ImageVector.vectorResource(R.drawable.ic_info), contentDescription = "Help")
            }
        }

        StepProgressIndicator(currentStep, Modifier.fillMaxWidth())

        when (currentStep) {
            RecordingStep.NAME -> {
                SoundDetailsStep(
                    soundName = soundName,
                    soundThreshold = soundThreshold,
                    isCriticalSoundEnabled = isCriticalSoundEnabled,
                    selectedCooldown = selectedCooldown,
                    vibrationPattern = vibrationPattern,
                    selectedVibrationPattern = selectedVibrationPattern,
                    onSoundNameChange = { soundName = it },
                    onCriticalChange = { isCriticalSoundEnabled = it },
                    onCooldownChange = { selectedCooldown = it },
                    onSoundThresholdChange = { soundThreshold = it },
                    onVibrationPatternChange = { vibrationPattern = it },
                    onDefaultVibrationClick = {
                        selectedVibrationPattern = "Default"
                        vibrationPattern = DEFAULT_VIBRATION_PATTERN
                    },
                    onCustomVibrationClick = { selectedVibrationPattern = "Custom" },
                    onDontShowAgainClick = {
                        profileSettingsViewModel.updateShouldShowCriticalInfoDialog(
                            false
                        )
                    },
                    onNext = {
                        if (soundName.isNotBlank()) {
                            currentStep = RecordingStep.RECORD
                        }
                    },
                    settings = settings
                )
            }

            RecordingStep.RECORD -> {
                AudioSamplesStep(
                    samples = samples,
                    audioRecorder = audioRecorder,
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
                            val quality = AudioQualityAnalyzer().analyzeQuality(audioData)
                            samples = samples + AudioSample(
                                source = AudioSource.Recording(audioData),
                                quality = quality
                            )
                            errorMessage = null
                        } else {
                            errorMessage = "Recording too short (minimum 1 second)"
                        }
                        isRecording = false
                        recordingDuration = 0
                    },
                    onUploadAudio = {
                        audioPickerLauncher.launch("audio/*")
                    },
                    onDeleteSample = { id ->
                        samples = samples.filter { it.id != id }
                    },
                    onDeleteLast = {
                        if (samples.isNotEmpty()) {
                            samples = samples.dropLast(1)
                        }
                    },
                    onNext = {
                        if (samples.size >= 3) {
                            currentStep = RecordingStep.PREVIEW
                        }
                    },
                    onBack = { currentStep = RecordingStep.NAME }
                )
            }

            RecordingStep.PREVIEW -> {
                val isDefaultPattern =
                    selectedVibrationPattern == "Default"
                PreviewAndSaveStep(
                    soundName = soundName,
                    recordingCount = samples.size,
                    threshold = soundThreshold,
                    isCritical = isCriticalSoundEnabled,
                    emergencyCooldownMinutes = selectedCooldown,
                    vibrationPattern = vibrationPattern,
                    onSave = {
                        coroutineScope.launch {
                            viewModel.createCustomSound(
                                soundName,
                                samples,
                                vibrationPattern =
                                    if (isDefaultPattern) DEFAULT_VIBRATION_PATTERN
                                    else vibrationPattern,
                                threshold = soundThreshold,
                                isCritical = isCriticalSoundEnabled,
                                emergencyCooldownMinutes = selectedCooldown
                            )
                            resetAddSound()
                            Toast.makeText(context, "Sound saved successfully", Toast.LENGTH_LONG)
                                .show()
                            onSoundCreated()
                        }
                    },
                    onBack = { currentStep = RecordingStep.RECORD },
                    onTrash = {
                        resetAddSound()
                    }
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
            modifier = Modifier
                .weight(1f)
                .offset(y = (-10).dp)
        )

        StepIndicatorItem(
            label = "Record",
            isActive = currentStep == RecordingStep.RECORD,
            isCompleted = currentStep.ordinal > RecordingStep.RECORD.ordinal
        )

        StepConnector(
            isCompleted = currentStep.ordinal > RecordingStep.RECORD.ordinal,
            isActive = currentStep >= RecordingStep.PREVIEW,
            modifier = Modifier
                .weight(1f)
                .offset(y = (-10).dp)
        )

        StepIndicatorItem(
            label = "Save",
            isActive = currentStep == RecordingStep.PREVIEW,
            isCompleted = false
        )
    }
}

@Composable
private fun AudioSamplesStep(
    samples: List<AudioSample>,
    audioRecorder: AudioRecorder,
    isRecording: Boolean,
    recordingDuration: Int,
    currentAmplitude: Float,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onUploadAudio: () -> Unit,
    onDeleteSample: (String) -> Unit,
    onDeleteLast: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val realtimeAnalyzer = remember { RealtimeAudioQualityAnalyzer() }
    var realtimeQuality by remember { mutableStateOf<RealtimeQuality?>(null) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                val currentBuffer = audioRecorder.getCurrentBuffer()
                realtimeQuality = realtimeAnalyzer.analyzeBuffer(currentBuffer)
                delay(500)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with undo button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Add audio samples",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${samples.size} added (minimum 3)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (samples.isNotEmpty() && !isRecording) {
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

        // Sample list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp), // ensures visibility
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(samples) { index, sample ->
                AudioSampleItem(
                    index = index + 1,
                    sample = sample,
                    onDelete = { onDeleteSample(sample.id) }
                )
            }
        }

        // Info card
        InfoCard(
            text = "Record or upload audio of the same sound from different angles or distances. " +
                    "Upload accepts different audio file types (min 1 second, max 10 seconds)."
        )

        // REAL-TIME FEEDBACK (during recording)
        if (isRecording && realtimeQuality != null) {
            RealtimeFeedbackCard(
                quality = realtimeQuality!!,
                amplitude = currentAmplitude
            )
        }

        // Action buttons (Record and Upload)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Record button
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                RecordButton(
                    isRecording = isRecording,
                    onClick = if (isRecording) onStopRecording else onStartRecording,
                    enabled = samples.size < 10
                )
            }

            // Divider
            Text(
                text = "OR",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Upload button
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                UploadButton(
                    onClick = onUploadAudio,
                    enabled = !isRecording && samples.size < 10
                )
            }
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
                Text("Back", style = MaterialTheme.typography.labelLarge)
            }

            Button(
                onClick = onNext,
                enabled = samples.size >= 3 && !isRecording,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Continue", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun RealtimeFeedbackCard(
    quality: RealtimeQuality,
    amplitude: Float
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with status
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

                // Status icon
                Icon(
                    imageVector = when {
                        quality.canUse -> Icons.Default.Check
                        quality.isPeaking -> ImageVector.vectorResource(R.drawable.ic_triangle_alert)
                        quality.isQuiet -> ImageVector.vectorResource(R.drawable.ic_volume_off)
                        quality.isNoisy -> ImageVector.vectorResource(R.drawable.ic_audio_lines)
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when {
                        quality.canUse -> Lime50
                        else -> Color(0xFFFFC107)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            // Waveform visualization
            WaveformVisualizer(
                amplitude = amplitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )

            // Audio levels display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LevelIndicator(
                    label = "Quality",
                    value = (quality.snr / 40f).coerceIn(0f, 1f) * 100,
                    max = 100f,
                    modifier = Modifier.weight(1f)
                )

                LevelIndicator(
                    label = "Loudness",
                    value = (quality.rmsLevel / 0.3f).coerceIn(0f, 1f) * 100,
                    max = 100f,
                    isWarning = quality.isPeaking,
                    modifier = Modifier.weight(1f)
                )
            }

            // Suggestion text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = when {
                            quality.canUse -> Lime50.copy(alpha = 0.1f)
                            quality.isPeaking -> Amber50.copy(alpha = 0.1f)
                            quality.isQuiet -> Amber50.copy(alpha = 0.1f)
                            else -> Sky50.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when {
                        quality.canUse -> Icons.Default.Check
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when {
                        quality.canUse -> Lime50
                        else -> Color(0xFFFFC107)
                    }
                )
                Text(
                    text = quality.suggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LevelIndicator(
    label: String,
    value: Float,
    max: Float,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "%.2f".format(value),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { (value / max).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = when {
                isWarning -> Amber50
                value < max * 0.3f -> Sky50
                value < max * 0.7f -> Lime50
                else -> Color(0xFFFFC107)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun AudioSampleItem(
    index: Int,
    sample: AudioSample,
    onDelete: () -> Unit
) {
    val source = sample.source
    val duration = sample.quality?.duration

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon + Info
            Icon(
                imageVector = when (source) {
                    is AudioSource.Recording -> ImageVector.vectorResource(R.drawable.ic_mic_filled)
                    is AudioSource.Upload -> ImageVector.vectorResource(R.drawable.ic_upload_file)
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Sample $index",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "($duration seconds)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (source is AudioSource.Upload) {
                    Text(
                        text = source.fileName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
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
private fun SoundDetailsStep(
    soundName: String,
    soundThreshold: Float,
    isCriticalSoundEnabled: Boolean,
    selectedCooldown: Int,
    vibrationPattern: List<Long>,
    selectedVibrationPattern: String,
    onSoundNameChange: (String) -> Unit,
    onCriticalChange: (Boolean) -> Unit,
    onCooldownChange: (Int) -> Unit,
    onSoundThresholdChange: (Float) -> Unit,
    onVibrationPatternChange: (List<Long>) -> Unit,
    onDontShowAgainClick: () -> Unit,
    onDefaultVibrationClick: () -> Unit = {},
    onCustomVibrationClick: () -> Unit = {},
    onNext: () -> Unit,
    settings: Settings,
) {
    val isFormValid = soundName.isNotBlank() && vibrationPattern.size >= 3

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "What sound do you want to detect?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = soundName,
                onValueChange = onSoundNameChange,
                label = {
                    Text(
                        "Sound name"
                    )
                },
                placeholder = {
                    Text(
                        "e.g., My dog barking, Baby crying",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                CriticalToggle(
                    modifier = Modifier.padding(20.dp),
                    isCriticalEnabled = isCriticalSoundEnabled,
                    selectedCooldown = selectedCooldown,
                    onCriticalChanged = onCriticalChange,
                    onCooldownChanged = onCooldownChange,
                    shouldShowCriticalInfoDialog = settings.shouldShowCriticalInfoDialog,
                    onDontShowAgainClick = onDontShowAgainClick
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                ThresholdSlider(
                    modifier = Modifier.padding(20.dp),
                    initialValue = soundThreshold,
                    onThresholdChange = onSoundThresholdChange
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                VibrationPattern(
                    modifier = Modifier.padding(20.dp),
                    selectedVibrationPattern = selectedVibrationPattern,
                    onVibrationPatternChanged = {
                        val newVibrationPattern = stepsToVibrationPattern(it, 200).toList()
                        onVibrationPatternChange(newVibrationPattern)
                    },
                    onDefaultVibrationClick = onDefaultVibrationClick,
                    onCustomVibrationClick = onCustomVibrationClick
                )
            }
        }

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
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Next", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
    val amplitudeHistory = remember {
        mutableStateListOf<Float>().apply { repeat(50) { add(0f) } }
    }

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
private fun InfoChip(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UploadButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable(enabled = enabled) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_upload_file),
                contentDescription = "Upload audio",
                tint = if (enabled) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "Upload",
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(if (isRecording) scale else 1f)
                .clip(CircleShape)
                .background(
                    if (isRecording) MaterialTheme.colorScheme.error
                    else if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable(enabled = enabled) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRecording)
                    ImageVector.vectorResource(R.drawable.ic_stop)
                else
                    ImageVector.vectorResource(R.drawable.ic_mic_none),
                contentDescription = if (isRecording) "Stop recording" else "Start recording",
                tint = if (enabled || isRecording) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = if (isRecording) "Stop" else "Record",
            style = MaterialTheme.typography.labelMedium,
            color = if (isRecording) MaterialTheme.colorScheme.error
            else if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PreviewAndSaveStep(
    soundName: String,
    recordingCount: Int,
    threshold: Float,
    isCritical: Boolean,
    vibrationPattern: List<Long>,
    emergencyCooldownMinutes: Int,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onTrash: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Review your custom sound",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
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
                InfoRow(
                    label = "Detection threshold",
                    value = "${(threshold * 100).roundToInt()}%"
                )
                InfoRow(
                    label = "Is critical",
                    value = if (isCritical) "Yes" else "No"
                )
                if (isCritical) {
                    InfoRow(
                        label = "Message cooldown",
                        value = "$emergencyCooldownMinutes minutes"
                    )
                }
                InfoRow(
                    label = "Vibration pattern",
                ) {
                    VibrationPlayer(
                        pattern = vibrationPattern
                    )
                }
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
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Your samples will be processed to create a unique sound signature. " +
                                "The app will then alert you when it detects similar sounds.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                    Text("Back", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Create Sound",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Clear button
            TextButton(
                onClick = onTrash,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_trash),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Discard and Start Over",
                    style = MaterialTheme.typography.labelMedium
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
private fun InfoRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tips for Custom Sounds", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Works best with:",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )

                BulletPoint("Repeating mechanical sounds (beeps, chimes, alarms)")
                BulletPoint("Your baby's cry pattern")
                BulletPoint("Unique household sounds (doorbell, kettle)")

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Recording tips:",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )

                BulletPoint("Add 3-5 samples from different angles/distances")
                BulletPoint("Make sure sound is clear and loud enough")
                BulletPoint("Minimize background noise")

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Upload options:",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(4.dp))

                BulletPoint("Accepts MP3, M4A, WAV, OGG, FLAC")

                Text(
                    text = "Duration:",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )

                BulletPoint("Minimum of 1 second per sample")
                BulletPoint("Maximum of 10 seconds per sample")

                Spacer(Modifier.height(4.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_lightbulb),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Mix recordings and uploads! Upload existing audio files and supplement with fresh recordings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text("•", style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}