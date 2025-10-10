package com.pagzone.sonavi.ui.screen.page

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pagzone.sonavi.R
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.ui.component.CriticalToggle
import com.pagzone.sonavi.ui.component.CustomMenuItem
import com.pagzone.sonavi.ui.component.CustomSearchBar
import com.pagzone.sonavi.ui.component.SoundFilterChips
import com.pagzone.sonavi.ui.component.ThresholdSlider
import com.pagzone.sonavi.ui.component.VibrationPattern
import com.pagzone.sonavi.util.Constants.Classifier.CONFIDENCE_THRESHOLD
import com.pagzone.sonavi.util.Constants.SoundProfile.DEFAULT_VIBRATION_PATTERN
import com.pagzone.sonavi.util.Helper.Companion.stepsToVibrationPattern
import com.pagzone.sonavi.viewmodel.ProfileSettingsViewModel
import com.pagzone.sonavi.viewmodel.SoundViewModel

@Composable
fun LibraryPage(
    modifier: Modifier = Modifier,
    viewModel: SoundViewModel = hiltViewModel(),
    profileSettingsViewModel: ProfileSettingsViewModel = hiltViewModel()
) {
    var selectedDeleteSound by remember { mutableStateOf<SoundProfile?>(null) }
    var selectedEditSound by remember { mutableStateOf<SoundProfile?>(null) }
    var selectedSnoozeSound by remember { mutableStateOf<SoundProfile?>(null) }

    var query by rememberSaveable { mutableStateOf("") }
    val filters = listOf("All", "Built-in", "Custom", "Critical", "Snoozed")
    var selectedFilter by rememberSaveable { mutableStateOf<String?>("All") }

    val sounds by viewModel.sounds.collectAsStateWithLifecycle()
    val snoozeStatuses by viewModel.snoozeStatuses.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Use derivedStateOf for expensive computations to avoid unnecessary recalculations
    val filteredSounds by remember {
        derivedStateOf {
            var filtered = sounds

            // Apply search filter
            if (query.isNotBlank()) {
                filtered = filtered.filter { sound ->
                    sound.displayName.contains(query, ignoreCase = true)
                }
            }

            // Apply category filter
            if (selectedFilter != "All" && selectedFilter != null) {
                filtered = filtered.filter { sound ->
                    when (selectedFilter) {
                        "Built-in" -> sound.isBuiltIn
                        "Custom" -> !sound.isBuiltIn
                        "Critical" -> sound.isCritical
                        "Snoozed" -> snoozeStatuses[sound.id] ?: false
                        else -> true
                    }
                }
            }

            filtered
        }
    }

    // Separate the enabled check to avoid recalculation
    val allEnabled by remember {
        derivedStateOf {
            filteredSounds.isNotEmpty() && filteredSounds.all { it.isEnabled }
        }
    }

    // Memoize the toggle all callback to prevent recreation
    val onToggleAllClick = remember {
        {
            val targetState = !allEnabled
            filteredSounds.forEach { sound ->
                viewModel.setSoundProfileEnabled(sound.id, targetState)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        CustomSearchBar(
            query,
            onQueryChange = { query = it },
            onClearQuery = { query = "" },
            placeholder = {
                Text("Search sounds...")
            }
        )

        SoundFilterChips(
            filters = filters,
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )

        // Stats and Toggle All Section
        if (filteredSounds.isNotEmpty()) {
            StatsAndToggleSection(
                soundCount = filteredSounds.size,
                allEnabled = allEnabled,
                onToggleAllClick = onToggleAllClick
            )
        } else {
            EmptySoundsCard()
        }

        // Sound List
        SoundList(
            sounds = filteredSounds,
            snoozeStatuses = snoozeStatuses,
            onToggleSound = viewModel::setSoundProfileEnabled,
            onEditSound = { selectedEditSound = it },
            onSnoozeSound = { selectedSnoozeSound = it },
            onUnsnoozeSound = { viewModel.unsnoozeSound(it.id) },
            onDeleteSound = { selectedDeleteSound = it }
        )

        selectedDeleteSound?.let { soundProfile ->
            ConfirmDeleteDialog(
                title = "Delete this sound profile?",
                message = "Are you sure you want to delete \"${soundProfile.displayName}\"? This action cannot be undone.",
                onConfirm = {
                    viewModel.deleteSoundProfile(soundProfile)
                    selectedDeleteSound = null
                },
                onDismiss = {
                    selectedDeleteSound = null
                }
            )
        }

        selectedEditSound?.let { soundProfile ->
            EditModalSheet(
                showSheet = true,
                soundProfile = soundProfile,
                onSave = { newSoundProfile ->
                    viewModel.updateSoundProfile(newSoundProfile)
                    Toast.makeText(context, "Sound profile has been saved!", Toast.LENGTH_SHORT)
                        .show()
                },
                onDismissRequest = { selectedEditSound = null },
                profileSettingsViewModel = profileSettingsViewModel
            )
        }

        selectedSnoozeSound?.let { soundProfile ->
            SnoozeBottomSheet(
                soundProfile.displayName,
                onDismissRequest = { selectedSnoozeSound = null },
                onSnooze = { minutes ->
                    viewModel.snoozeSound(soundProfile.id, minutes)
                }
            )
        }
    }
}

@Composable
private fun EmptySoundsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_music_off),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No sounds found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteDialog(
    title: String = "Delete Item?",
    message: String = "Are you sure you want to delete this item? This action cannot be undone.",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Delete",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun SoundList(
    sounds: List<SoundProfile>,
    snoozeStatuses: Map<Long, Boolean>,
    onToggleSound: (Long, Boolean) -> Unit,
    onEditSound: (SoundProfile) -> Unit,
    onSnoozeSound: (SoundProfile) -> Unit,
    onUnsnoozeSound: (SoundProfile) -> Unit,
    onDeleteSound: (SoundProfile) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = sounds,
            key = { it.id },
            contentType = { "sound_card" }
        ) { sound ->
            SoundCard(
                sound = sound,
                onToggleClick = { enabled ->
                    onToggleSound(sound.id, enabled)
                },
                onUnsnoozeClick = { onUnsnoozeSound(sound) },
                isSnoozed = snoozeStatuses[sound.id] ?: false,
                onMenuClick = { action ->
                    when (action) {
                        "edit" -> onEditSound(sound)
                        "snooze" -> onSnoozeSound(sound)
                        "unsnooze" -> onUnsnoozeSound(sound)
                        "delete" -> onDeleteSound(sound)
                    }
                }
            )
        }
    }
}

@Composable
private fun StatsAndToggleSection(
    soundCount: Int,
    allEnabled: Boolean,
    onToggleAllClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sound count with icon
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_music_note),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "$soundCount sound${if (soundCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Custom button to control sizing
            Surface(
                onClick = onToggleAllClick,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            if (allEnabled) R.drawable.ic_sensors_off else R.drawable.ic_sensors
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )

                    Text(
                        text = if (allEnabled) "Disable All" else "Enable All",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeBottomSheet(
    soundName: String,
    onDismissRequest: () -> Unit,
    onSnooze: (durationMinutes: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val quickDurations = listOf(
        5 to "5 min",
        15 to "15 min",
        30 to "30 min",
        60 to "1 hour",
        120 to "2 hours",
        480 to "8 hours"
    )

    var showCustomDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with better visual hierarchy
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_snooze),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Snooze Detection",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Temporarily pause \"$soundName\" detection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick duration options with improved grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickDurations) { (minutes, label) ->
                    SnoozeOptionCard(
                        label = label,
                        onClick = {
                            onSnooze(minutes)
                            onDismissRequest()
                        }
                    )
                }
            }

            // Custom duration option with better styling
            Card(
                onClick = { showCustomDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_schedule),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Custom Duration",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Custom duration dialog
    if (showCustomDialog) {
        CustomSnoozeDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { minutes ->
                onSnooze(minutes)
                showCustomDialog = false
                onDismissRequest()
            }
        )
    }
}

@Composable
private fun SnoozeOptionCard(
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "cardScale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1.2f)
            .scale(scale),
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Duration icon
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_timer),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CustomSnoozeDialog(
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(15) }

    val totalMinutes = hours * 60 + minutes
    val isValid = totalMinutes > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_schedule),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        title = {
            Text(
                text = "Custom Duration",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hours picker with better styling
                TimePickerRow(
                    label = "Hours",
                    value = hours,
                    onDecrease = { if (hours > 0) hours-- },
                    onIncrease = { if (hours < 23) hours++ },
                    canDecrease = hours > 0,
                    canIncrease = hours < 23
                )

                // Minutes picker
                TimePickerRow(
                    label = "Minutes",
                    value = minutes,
                    onDecrease = { if (minutes >= 5) minutes -= 5 },
                    onIncrease = { if (minutes <= 55) minutes += 5 },
                    canDecrease = minutes > 0,
                    canIncrease = minutes < 60
                )

                // Duration preview with better styling
                if (isValid) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Duration: ${formatDuration(totalMinutes)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(totalMinutes) },
                enabled = isValid,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_snooze),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Snooze")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun TimePickerRow(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    canDecrease: Boolean,
    canIncrease: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(80.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                IconButton(
                    onClick = onDecrease,
                    enabled = canDecrease,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_remove),
                        contentDescription = "Decrease",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = onIncrease,
                    enabled = canIncrease,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditModalSheet(
    showSheet: Boolean,
    soundProfile: SoundProfile,
    onSave: (SoundProfile) -> Unit,
    onDismissRequest: () -> Unit,
    profileSettingsViewModel: ProfileSettingsViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(soundProfile.displayName) }
    var isCriticalSoundEnabled by remember { mutableStateOf(soundProfile.isCritical) }
    var soundThreshold by remember { mutableFloatStateOf(soundProfile.threshold) }
    var vibrationPattern by remember { mutableStateOf(soundProfile.vibrationPattern) }
    var selectedCooldown by remember { mutableIntStateOf(soundProfile.emergencyCooldownMinutes) }
    var showHelp by remember { mutableStateOf(false) }

    val settings by profileSettingsViewModel.settings.collectAsStateWithLifecycle()

    val isDefaultVibrationPattern = soundProfile.vibrationPattern == DEFAULT_VIBRATION_PATTERN
    var selectedVibrationPattern by remember {
        mutableStateOf(
            if (isDefaultVibrationPattern) "Default" else "Custom"
        )
    }

    val isFormValid = name.isNotBlank() && vibrationPattern.size >= 3
    val hasChanges =
        name != soundProfile.displayName || isCriticalSoundEnabled != soundProfile.isCritical ||
                soundThreshold != soundProfile.threshold || vibrationPattern != soundProfile.vibrationPattern ||
                (soundProfile.isCritical && selectedCooldown != soundProfile.emergencyCooldownMinutes)

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = {
                // Custom drag handle with better visual hierarchy
                Surface(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(width = 32.dp, height = 4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ) {}
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Section
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Configure Sound Profile",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                onClick = { showHelp = !showHelp },
                                shape = CircleShape,
                                color =
                                    if (showHelp) MaterialTheme.colorScheme.error.copy(0.3f)
                                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector =
                                            if (showHelp) ImageVector.vectorResource(R.drawable.ic_close)
                                            else ImageVector.vectorResource(R.drawable.ic_help_outline),
                                        contentDescription = "Help",
                                        tint =
                                            if (showHelp) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Customize your sound profile settings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sound name input section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Sound Name",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(soundProfile.name)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (soundProfile.isBuiltIn && name != soundProfile.name)
                                        IconButton(
                                            onClick = { name = soundProfile.name },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.ic_reset),
                                                contentDescription = "Reset"
                                            )
                                        }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
                                        alpha = 0.5f
                                    ),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.6f
                                    ),
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.5f
                                    ),
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            HelpCard(
                                showHelp = showHelp,
                                text = "Customize how this sound appears in your notifications and activity log."
                            )
                        }
                    }
                }

                // Critical sound section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        CriticalToggle(
                            isCriticalEnabled = isCriticalSoundEnabled,
                            selectedCooldown = selectedCooldown,
                            onCriticalChanged = { isCriticalSoundEnabled = it },
                            onCooldownChanged = { selectedCooldown = it },
                            shouldShowCriticalInfoDialog = settings.shouldShowCriticalInfoDialog,
                            modifier = Modifier.padding(20.dp),
                            onDontShowAgainClick = {
                                profileSettingsViewModel.updateShouldShowCriticalInfoDialog(false)
                            },
                            showHelp = showHelp
                        )
                    }
                }

                // Sound detection threshold slider
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        ThresholdSlider(
                            initialValue = soundProfile.threshold,
                            onThresholdChange = { soundThreshold = it },
                            modifier = Modifier.padding(20.dp),
                            showHelp = showHelp
                        )
                    }
                }

                // Vibration section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        VibrationPattern(
                            modifier = Modifier.padding(20.dp),
                            initialVibrationPattern = soundProfile.vibrationPattern,
                            selectedVibrationPattern = selectedVibrationPattern,
                            showHelp = showHelp,
                            onVibrationPatternChanged = {
                                vibrationPattern =
                                    stepsToVibrationPattern(it, 200).toList()
                            },
                            onDefaultVibrationClick = {
                                selectedVibrationPattern = "Default"
                                vibrationPattern = DEFAULT_VIBRATION_PATTERN
                            },
                            onCustomVibrationClick = {
                                selectedVibrationPattern = "Custom"
                                vibrationPattern = emptyList()
                            }
                        )
                    }
                }

                // Single primary action - Save button
                item {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onDismissRequest() },
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceDim,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Button(
                            onClick = {
                                onSave(
                                    soundProfile.copy(
                                        displayName = name.trim(),
                                        isCritical = isCriticalSoundEnabled,
                                        threshold = soundThreshold,
                                        vibrationPattern =
                                            if (selectedVibrationPattern == "Custom") vibrationPattern
                                            else DEFAULT_VIBRATION_PATTERN,
                                        emergencyCooldownMinutes =
                                            if (isCriticalSoundEnabled) selectedCooldown
                                            else 5
                                    )
                                )
                                onDismissRequest()
                            },
                            enabled = isFormValid && hasChanges,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Save Sound Settings",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpCard(
    showHelp: Boolean,
    text: String,
    title: String = "Help",
) {
    if (showHelp) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_help_outline),
                    contentDescription = "Help",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SoundCard(
    sound: SoundProfile,
    modifier: Modifier = Modifier,
    isSnoozed: Boolean = false,
    onToggleClick: (enabled: Boolean) -> Unit = {},
    onUnsnoozeClick: () -> Unit = {},
    onMenuClick: (String) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    val cardBackgroundColor =
        if (isSnoozed) MaterialTheme.colorScheme.tertiaryContainer
        else if (sound.isEnabled) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant

    val iconButtonColor =
        if (isSnoozed) MaterialTheme.colorScheme.tertiary
        else if (sound.isEnabled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant

    val icon =
        if (isSnoozed) ImageVector.vectorResource(id = R.drawable.ic_snooze)
        else if (sound.isEnabled) ImageVector.vectorResource(id = R.drawable.ic_sensors)
        else ImageVector.vectorResource(id = R.drawable.ic_sensors_off)

    val iconScale = if (sound.isEnabled || isSnoozed) 1.1f else 1f
    val contentAlpha = if (sound.isEnabled || isSnoozed) 1f else 0.7f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                if (isSnoozed) onUnsnoozeClick()
                else onToggleClick(!sound.isEnabled)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (sound.isEnabled) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sound Icon - compact size with vibration badge
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                // Main icon background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconButtonColor)
                        .scale(iconScale),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = sound.displayName,
                            tint = if (isSnoozed) MaterialTheme.colorScheme.onTertiary
                            else if (sound.isEnabled) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(24.dp)
                        )

                        if (isSnoozed) {
                            val remainingMinutes = sound.snoozedUntil?.let { date ->
                                val diffMs = date.time - System.currentTimeMillis()
                                if (diffMs > 0) diffMs / 60_000 else 0
                            } ?: 0

                            Text(
                                text = formatDuration(remainingMinutes.toInt()),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                }

                // Custom vibration indicator badge (positioned outside clipping area)
                if (sound.vibrationPattern != DEFAULT_VIBRATION_PATTERN) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_watch_vibration),
                            contentDescription = "Custom vibration",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sound.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        val soundLabel = if (sound.isBuiltIn) "Built-in" else "Custom"
                        Text(
                            text = "$soundLabel${if (sound.displayName != sound.name) "*" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Compact status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSnoozed)
                                        MaterialTheme.colorScheme.tertiary
                                    else if (sound.isEnabled)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        )

                        Text(
                            text =
                                if (isSnoozed) "Snoozed"
                                else if (sound.isEnabled) "Listening"
                                else "Paused",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color =
                                if (isSnoozed)
                                    MaterialTheme.colorScheme.tertiary
                                else if (sound.isEnabled)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Compact status indicator
                    if (sound.isCritical)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(10.dp),
                                imageVector = ImageVector.vectorResource(R.drawable.ic_emergency_home_filled),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )

                            Text(
                                text = "Critical",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                    if (sound.threshold != CONFIDENCE_THRESHOLD)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(12.dp),
                                imageVector = ImageVector.vectorResource(R.drawable.ic_music_note),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "${(sound.threshold * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Menu Button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Dropdown Menu
                DropdownMenu(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 164.dp)
                        .padding(horizontal = 8.dp),
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    CustomMenuItem(
                        text = "Edit",
                        icon = R.drawable.ic_edit,
                        onClick = {
                            onMenuClick("edit")
                            showMenu = false
                        }
                    )

                    CustomMenuItem(
                        text = if (isSnoozed) "Unsnooze" else "Snooze",
                        icon = R.drawable.ic_snooze,
                        onClick = {
                            if (isSnoozed) onMenuClick("unsnooze")
                            else onMenuClick("snooze")
                            showMenu = false
                        }
                    )

                    if (!sound.isBuiltIn) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(0.2f)
                        )

                        CustomMenuItem(
                            text = "Delete",
                            icon = R.drawable.ic_trash,
                            onClick = {
                                onMenuClick("delete")
                                showMenu = false
                            },
                            isDestructive = true
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(minutes: Int): String {
    return when {
        minutes == 0 -> "0 minutes"
        minutes < 60 -> "$minutes minute${if (minutes != 1) "s" else ""}"
        minutes % 60 == 0 -> {
            val hours = minutes / 60
            "$hours hour${if (hours != 1) "s" else ""}"
        }

        else -> {
            val hours = minutes / 60
            val mins = minutes % 60
            "${hours}h ${mins}m"
        }
    }
}