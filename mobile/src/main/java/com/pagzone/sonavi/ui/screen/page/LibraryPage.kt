package com.pagzone.sonavi.ui.screen.page

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pagzone.sonavi.R
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.ui.component.CustomMenuItem
import com.pagzone.sonavi.ui.component.CustomSearchBar
import com.pagzone.sonavi.ui.component.SoundFilterChips
import com.pagzone.sonavi.ui.component.StepSequencer
import com.pagzone.sonavi.ui.component.stepsToVibrationPattern
import com.pagzone.sonavi.util.Constants.Classifier.CONFIDENCE_THRESHOLD
import com.pagzone.sonavi.util.Constants.SoundProfile.DEFAULT_VIBRATION_PATTERN
import com.pagzone.sonavi.viewmodel.SoundViewModel

@Composable
fun LibraryPage(
    modifier: Modifier = Modifier,
    viewModel: SoundViewModel = hiltViewModel()
) {
    var selectedSound by remember { mutableStateOf<SoundProfile?>(null) }

    var query by rememberSaveable { mutableStateOf("") }
    val filters = listOf("All", "Built-in", "Critical")
    var selectedFilter by rememberSaveable { mutableStateOf<String?>("All") }

    val sounds by viewModel.sounds.collectAsStateWithLifecycle()

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
                        "Critical" -> sound.isCritical
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
        }

        // Sound List
        SoundList(
            sounds = filteredSounds,
            onToggleSound = viewModel::setSoundProfileEnabled,
            onEditSound = { selectedSound = it }
        )

        selectedSound?.let { soundProfile ->
            EditModalSheet(
                showSheet = true,
                soundProfile = soundProfile,
                onSave = { newSoundProfile ->
                    viewModel.updateSoundProfile(newSoundProfile)
                    Toast.makeText(context, "Sound profile has been saved!", Toast.LENGTH_SHORT)
                        .show()
                },
                onDismissRequest = { selectedSound = null },
            )
        }
    }
}

@Composable
private fun SoundList(
    sounds: List<SoundProfile>,
    onToggleSound: (Long, Boolean) -> Unit,
    onEditSound: (SoundProfile) -> Unit
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
                onMenuClick = { action ->
                    when (action) {
                        "edit" -> onEditSound(sound)
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    color = MaterialTheme.colorScheme.onSurface
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
                            fontWeight = FontWeight.SemiBold
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
fun EditModalSheet(
    showSheet: Boolean,
    soundProfile: SoundProfile,
    onSave: (SoundProfile) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(soundProfile.displayName) }
    var isCriticalSoundEnabled by remember { mutableStateOf(soundProfile.isCritical) }
    var soundThreshold by remember { mutableFloatStateOf(soundProfile.threshold) }
    var vibrationPattern by remember { mutableStateOf(soundProfile.vibrationPattern) }

    val isDefaultVibrationPattern = soundProfile.vibrationPattern == DEFAULT_VIBRATION_PATTERN
    var selectedVibrationPattern by remember {
        mutableStateOf(
            if (isDefaultVibrationPattern) "Default" else "Custom"
        )
    }

    val isFormValid = name.isNotBlank() && vibrationPattern.size >= 3
    val hasChanges =
        name != soundProfile.displayName || isCriticalSoundEnabled != soundProfile.isCritical ||
                soundThreshold != soundProfile.threshold || vibrationPattern != soundProfile.vibrationPattern

    // Animation states for micro-interactions
    val switchScale by animateFloatAsState(
        targetValue = if (isCriticalSoundEnabled) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "switch_scale"
    )

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
                                onClick = { /* Handle help action */ },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_help_outline),
                                        contentDescription = "Help",
                                        tint = MaterialTheme.colorScheme.primary,
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
                                    fontWeight = FontWeight.SemiBold,
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon( // TODO: change icon
                                    imageVector = if (isCriticalSoundEnabled)
                                        ImageVector.vectorResource(R.drawable.ic_emergency_home_filled) else
                                        ImageVector.vectorResource(R.drawable.ic_emergency_home),
                                    contentDescription = null,
                                    tint = if (isCriticalSoundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Critical Sound",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = if (isCriticalSoundEnabled) "Enabled" else "Disabled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isCriticalSoundEnabled,
                                onCheckedChange = { isCriticalSoundEnabled = it },
                                modifier = Modifier.scale(switchScale),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(
                                        alpha = 0.5f
                                    )
                                )
                            )
                        }
                    }
                }

                // Sound detection threshold slider
                item {
                    val confidenceThreshold = CONFIDENCE_THRESHOLD

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
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_music_note),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Detection Threshold",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = "Controls how strict sound detection is",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Value display and reset button in a row
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "${(soundThreshold * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            )
                                        )
                                    }
                                    // Reset button
                                    if (soundThreshold != confidenceThreshold)
                                        Surface(
                                            onClick = { soundThreshold = confidenceThreshold },
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(
                                                alpha = 0.8f
                                            ),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(R.drawable.ic_reset),
                                                    contentDescription = "Reset to default",
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Low",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Slider(
                                        value = soundThreshold,
                                        onValueChange = { soundThreshold = it },
                                        valueRange = 0.01f..1f,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 16.dp),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(
                                                alpha = 0.2f
                                            )
                                        )
                                    )

                                    Text(
                                        text = "High",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Threshold indicators
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 32.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "1",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )

                                    Text(
                                        text = "25",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )

                                    Text(
                                        text = "50",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )

                                    Text(
                                        text = "75",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )

                                    Text(
                                        text = "100",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )
                                }
                            }
                        }
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
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_watch_vibration),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Vibration Pattern",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = "Currently using: $selectedVibrationPattern",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Current selection indicator
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Default button with selection state
                                val isDefaultSelected = selectedVibrationPattern == "Default"

                                Button(
                                    onClick = {
                                        selectedVibrationPattern = "Default"
                                        vibrationPattern = DEFAULT_VIBRATION_PATTERN
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = if (isDefaultSelected) {
                                        ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        )
                                    } else {
                                        ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    border = if (!isDefaultSelected) {
                                        BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        )
                                    } else null
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isDefaultSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Text(
                                            text = "Default",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Custom button with selection state
                                val isCustomSelected = selectedVibrationPattern == "Custom"

                                Button(
                                    onClick = {
                                        selectedVibrationPattern = "Custom"
                                        /* Handle create/select custom vibration */
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = if (isCustomSelected) {
                                        ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        )
                                    } else {
                                        ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    border = if (!isCustomSelected) {
                                        BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        )
                                    } else null
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isCustomSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Text(
                                            text = "Custom",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Show additional info when custom is selected
                            AnimatedVisibility(
                                visible = selectedVibrationPattern == "Custom",
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))

                                    StepSequencer(
                                        initialPattern = if (isDefaultVibrationPattern) null else soundProfile.vibrationPattern,
                                        onPatternChanged = {
                                            vibrationPattern =
                                                stepsToVibrationPattern(it, 200).toList()
                                        }
                                    )
                                }
                            }
                        }
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
                                            else DEFAULT_VIBRATION_PATTERN
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
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundCard(
    sound: SoundProfile,
    modifier: Modifier = Modifier,
    onToggleClick: (enabled: Boolean) -> Unit = {},
    onMenuClick: (String) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    val cardBackgroundColor = if (sound.isEnabled) MaterialTheme.colorScheme.secondary else
        MaterialTheme.colorScheme.surfaceVariant
    val iconButtonColor = if (sound.isEnabled) MaterialTheme.colorScheme.primary else
        MaterialTheme.colorScheme.secondary
    val icon = if (sound.isEnabled)
        ImageVector.vectorResource(id = R.drawable.ic_sensors) else
        ImageVector.vectorResource(id = R.drawable.ic_sensors_off)

    val iconScale = if (sound.isEnabled) 1.1f else 1f
    val contentAlpha = if (sound.isEnabled) 1f else 0.7f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggleClick(!sound.isEnabled) },
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
                    Icon(
                        imageVector = icon,
                        contentDescription = sound.displayName,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Custom vibration indicator badge (positioned outside clipping area)
                if (sound.vibrationPattern != DEFAULT_VIBRATION_PATTERN) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_watch_vibration),
                            contentDescription = "Custom vibration",
                            tint = MaterialTheme.colorScheme.onTertiary,
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
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Built-in${if (sound.displayName != sound.name) "*" else ""}",
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
                                    if (sound.isEnabled) Color(0xFF4CAF50) else Color(0xFFE18D17)
                                )
                        )

                        Text(
                            text = if (sound.isEnabled) "Listening" else "Paused",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = if (sound.isEnabled) {
                                Color(0xFF4CAF50)
                            } else {
                                Color(0xFFE18D17)
                            }
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
                                color = MaterialTheme.colorScheme.primary
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

                    if (!sound.isBuiltIn) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(0.2f)
                        )

                        CustomMenuItem(
                            text = "Delete",
                            icon = R.drawable.ic_trash_x,
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