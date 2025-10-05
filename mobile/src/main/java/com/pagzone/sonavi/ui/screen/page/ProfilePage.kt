package com.pagzone.sonavi.ui.screen.page

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.pagzone.sonavi.R
import com.pagzone.sonavi.model.EmergencyContact
import com.pagzone.sonavi.ui.component.CustomMenuItem
import com.pagzone.sonavi.ui.theme.Lime50
import com.pagzone.sonavi.viewmodel.EmergencyContactViewModel
import com.pagzone.sonavi.viewmodel.ProfileSettingsViewModel

fun Context.getContactInfo(uri: Uri): EmergencyContact? {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val name = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            )
            val id = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            )

            var phoneNumber: String? = null
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(id),
                null
            )?.use { phoneCursor ->
                if (phoneCursor.moveToFirst()) {
                    phoneNumber = phoneCursor.getString(
                        phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    )
                }
            }

            if (phoneNumber == null) return null

            return EmergencyContact(name = name, number = phoneNumber)
        }
    }
    return null
}

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    viewModel: EmergencyContactViewModel = hiltViewModel()
) {
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()

    val filterIcon = if (sortAscending)
        ImageVector.vectorResource(id = R.drawable.ic_filter_a_z) else
        ImageVector.vectorResource(id = R.drawable.ic_filter_z_a)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        ProfileCard()

        Spacer(modifier = Modifier.height(12.dp))

        Column {
            // Title and Sort Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emergency Contacts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { viewModel.toggleSort() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = filterIcon,
                        contentDescription = "Sort contacts",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AddContactButton(
                        viewModel,
                        emergencyContacts
                    )
                }

                items(
                    items = emergencyContacts,
                    key = { it.id },
                ) { emergencyContact ->
                    EmergencyContactCard(
                        contact = emergencyContact,
                        onMenuClick = { item ->
                            when (item) {
                                "delete" -> viewModel.deleteEmergencyContact(emergencyContact)
                            }
                        },
                        onToggleActive = { newContact ->
                            viewModel.updateEmergencyContact(newContact)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddContactButton(
    viewModel: EmergencyContactViewModel,
    emergencyContacts: List<EmergencyContact>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            val contact = context.getContactInfo(it)
            if (contact != null) {
                val (_, name, phoneNumber) = contact

                val alreadyExists =
                    emergencyContacts.any { contact -> contact.number == phoneNumber }
                if (alreadyExists) {
                    Toast.makeText(context, "Phone number is already added!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    viewModel.addEmergencyContact(name, phoneNumber)
                }
            } else { // Phone number is null
                Toast.makeText(context, "Phone number must not be empty!", Toast.LENGTH_SHORT)
                    .show()
                return@let
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickContactLauncher.launch(null)
        } else {
            Toast.makeText(context, "Contacts permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        onClick = {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    pickContactLauncher.launch(null)
                }

                else -> {
                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = .5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_circle), // or Icons.Default.Add
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(12.dp)
                )
            }

            Text(
                text = "Add emergency contact",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun EmergencyContactCard(
    contact: EmergencyContact,
    onToggleActive: (EmergencyContact) -> Unit,
    onMenuClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    // Animation for active state changes
    val animatedScale by animateFloatAsState(
        targetValue = if (contact.isActive) 1f else 0.985f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (contact.isActive) 1f else 0.8f,
        animationSpec = tween(500),
        label = "card_alpha"
    )

    val statusColor by animateColorAsState(
        targetValue = if (contact.isActive)
            Color(0xFF55A15A) else
            Color(0xFFB75151),
        animationSpec = tween(500),
        label = "card_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .scale(animatedScale)
            .alpha(animatedAlpha)
            // Here
            .clickable(
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onToggleActive(contact.copy(isActive = !contact.isActive))
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contact.isActive) {
                MaterialTheme.colorScheme.primary.copy(0.15f)
            } else {
                MaterialTheme.colorScheme.outline.copy(0.06f)
            }
        ),
        border = BorderStroke(
            width = if (contact.isActive) 1.dp else .5.dp,
            color = if (contact.isActive) {
                MaterialTheme.colorScheme.primary.copy(0.175f)
            } else {
                MaterialTheme.colorScheme.outline.copy(0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sound Icon - compact size
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor)
                    .scale(1.2f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_sos),
                    contentDescription = "SOS",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Contact Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (contact.isActive) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(0.7f)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Status chip
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = contact.number,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Menu Button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_kebab_menu),
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
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
                        text = if (contact.isActive) "Deactivate" else "Activate",
                        icon = if (contact.isActive) R.drawable.ic_close else R.drawable.ic_check,
                        onClick = {
                            onToggleActive(contact.copy(isActive = !contact.isActive))
                            showMenu = false
                        }
                    )

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

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileSettingsViewModel = hiltViewModel()
) {
    val profileSettings by profileViewModel.profileSettings.collectAsState()
    val uiState by profileViewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    val sendLocation = profileSettings.hasCurrentLocation

    // Handle error states
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            println("Profile error: $error")
        }
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = if (profileSettings.name.isNotEmpty()) {
                            profileSettings.name.split(" ")
                                .map { it.first().uppercaseChar() }
                                .joinToString("")
                                .take(2)
                        } else "?"

                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // User Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = profileSettings.name.ifEmpty { "No name set" },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profileSettings.address.ifEmpty { "No address set" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Location Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (sendLocation)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = sendLocation,
                            onValueChange = {
                                profileViewModel.updateHasCurrentLocation(it)
                            },
                            role = Role.Checkbox
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_map_pin),
                        contentDescription = null,
                        tint = if (sendLocation)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Include Current Location",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Send GPS coordinates with emergency alerts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = sendLocation,
                        onCheckedChange = null, // null because parent handles it
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        // Overlapping Edit Button
        IconButton(
            onClick = { showEditDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-5).dp, y = (-15).dp)
                .size(42.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_edit),
                    contentDescription = "Edit"
                )
            }
        }

        // Success indicator
        AnimatedVisibility(
            visible = uiState.isSuccess,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-5).dp, y = (-15).dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = Lime50,
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_check),
                        contentDescription = "Success",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
            }
        }

        // Edit Dialog
        if (showEditDialog) {
            EditProfileDialog(
                currentName = profileSettings.name,
                currentAddress = profileSettings.address,
                isLoading = uiState.isLoading,
                onDismiss = { showEditDialog = false },
                onConfirm = { newName, newAddress ->
                    profileViewModel.updateProfile(newName, newAddress)
                    showEditDialog = false
                }
            )
        }
    }
}


@Composable
fun EditProfileDialog(
    currentName: String,
    currentAddress: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var address by remember { mutableStateOf(currentAddress) }

    val isFormValid = name.isNotBlank()
    val hasChanges = name != currentName || address != currentAddress

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("Enter your name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = !name.isNotBlank() && name.isNotEmpty(),
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_person_outline),
                            contentDescription = "Name"
                        )
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (Optional)") },
                    placeholder = { Text("Enter your address") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = !address.isNotBlank() && address.isNotEmpty(),
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_location),
                            contentDescription = "Address"
                        )
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), address.trim()) },
                enabled = isFormValid && hasChanges && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancel", fontSize = 16.sp)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}