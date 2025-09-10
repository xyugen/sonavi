package com.pagzone.sonavi.ui.screen.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pagzone.sonavi.R
import com.pagzone.sonavi.ui.navigation.NavRoute

@Composable
fun ProfilePage(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top Bar
            TopBar("Profile", navController)

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Content
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ProfileCard()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    name: String = "Alex Arias",
    address: String = "Blk 1 Lot 1, San Isidro, Mandaluyong",
) {
    var showEditDialog by remember { mutableStateOf(false) }

    var currentName by remember { mutableStateOf(name) }
    var currentAddress by remember { mutableStateOf(address) }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
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
                    Text(
                        text = currentName.split(" ").map { it.first() }.joinToString("").take(2),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // User Info
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currentName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }


        // Overlapping Edit Button
        IconButton(
            onClick = {
                showEditDialog = true
            },
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
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_edit),
                contentDescription = "Edit"
            )
        }
    }


    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            currentName = currentName,
            currentAddress = currentAddress,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName, newAddress ->
                currentName = newName
                currentAddress = newAddress
                // TODO: Update profile
//                onProfileUpdated(newName, newAddress)
                showEditDialog = false
            }
        )
    }
}


@Composable
fun EditProfileDialog(
    currentName: String,
    currentAddress: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var address by remember { mutableStateOf(currentAddress) }

    // Validation state
    val isNameValid = name.isNotBlank()
    val isAddressValid = address.isNotBlank()
    val isFormValid = isNameValid && isAddressValid


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name TextField
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("Enter your name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isNameValid && name.isNotEmpty(),
                    supportingText = {
                        if (!isNameValid && name.isNotEmpty()) {
                            Text(
                                text = "Name cannot be empty",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_person_outline),
                            contentDescription = "Name"
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Address TextField
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    placeholder = { Text("Enter your address") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isAddressValid && address.isNotEmpty(),
                    supportingText = {
                        if (!isAddressValid && address.isNotEmpty()) {
                            Text(
                                text = "Address cannot be empty",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_location),
                            contentDescription = "Address"
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isFormValid) {
                        onConfirm(name.trim(), address.trim())
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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

@Composable
fun TopBar(title: String, navController: NavHostController, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_left),
                    contentDescription = "Back",
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
        Row {
            IconButton(
                onClick = {
                    navController.navigate(NavRoute.Profile.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_notifications_none),
                    contentDescription = "Notifications"
                )
            }
        }
    }
}