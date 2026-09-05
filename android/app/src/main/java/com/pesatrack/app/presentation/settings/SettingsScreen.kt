package com.pesatrack.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pesatrack.app.core.LocalProfileController
import com.pesatrack.app.navigation.Screen
import com.pesatrack.app.ui.theme.Background
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.FuelContainer
import com.pesatrack.app.ui.theme.FuelContainerDark
import com.pesatrack.app.ui.theme.FuelContentDark
import com.pesatrack.app.ui.theme.Income
import com.pesatrack.app.ui.theme.LocalDarkModeController
import com.pesatrack.app.ui.theme.LocalPesaTrackColors
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.StatusBarIcons
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import com.pesatrack.app.ui.theme.components.PesaBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var showAbout by remember { mutableStateOf(false) }
    var showProfileEdit by remember { mutableStateOf(false) }
    val darkModeController = LocalDarkModeController.current
    val profileController = LocalProfileController.current

    StatusBarIcons(darkIcons = false)

    Scaffold(
        bottomBar = { PesaBottomBar(navController) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Surface)
                .padding(horizontal = 20.dp)
        ) {
            ProfileRow(
                name = profileController.name,
                email = profileController.email,
                onClick = { showProfileEdit = true }
            )
            HorizontalDivider(color = Divider)
            SettingsRow(
                icon = Icons.Outlined.Category,
                label = "Categories",
                onClick = { navController.navigate(Screen.Categories.route) }
            )
            HorizontalDivider(color = Divider)
            SettingsRow(
                icon = Icons.AutoMirrored.Outlined.Message,
                label = "Import M-Pesa SMS",
                onClick = { navController.navigate(Screen.MpesaImport.route) }
            )
            HorizontalDivider(color = Divider)
            SettingsRow(
                icon = Icons.Outlined.DarkMode,
                label = "Dark mode",
                trailing = {
                    Switch(
                        checked = darkModeController.isDarkMode,
                        onCheckedChange = darkModeController.setDarkMode
                    )
                }
            )
            HorizontalDivider(color = Divider)
            SettingsRow(
                icon = Icons.Outlined.CreditCard,
                label = "Currency",
                trailingText = "Kenyan Shilling (KSh)"
            )
            HorizontalDivider(color = Divider)
            SettingsRow(
                icon = Icons.Outlined.Info,
                label = "About PesaTrack",
                trailingText = "Version 1.0.0",
                onClick = { showAbout = true }
            )
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About PesaTrack") },
            text = { Text("Version 1.0.0\n\nTrack every shilling.") },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showProfileEdit) {
        val sheetState = rememberModalBottomSheetState()
        var name by remember { mutableStateOf(profileController.name.orEmpty()) }
        var email by remember { mutableStateOf(profileController.email.orEmpty()) }

        ModalBottomSheet(
            onDismissRequest = { showProfileEdit = false },
            sheetState = sheetState,
            containerColor = Surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit profile",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        profileController.setProfile(name.trim(), email.trim())
                        showProfileEdit = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(
    name: String?,
    email: String?,
    onClick: () -> Unit
) {
    val isDark = LocalPesaTrackColors.current.isDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isDark) FuelContainerDark else FuelContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = if (isDark) FuelContentDark else Income
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name?.takeIf { it.isNotBlank() } ?: "Add your name",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = email?.takeIf { it.isNotBlank() } ?: "Add your email",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
    trailingText: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        trailingText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        when {
            trailing != null -> trailing()
            onClick != null -> Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}
