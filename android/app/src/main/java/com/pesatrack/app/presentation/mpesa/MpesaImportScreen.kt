package com.pesatrack.app.presentation.mpesa

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pesatrack.app.di.AppModule
import com.pesatrack.app.ui.theme.Background
import com.pesatrack.app.ui.theme.Income
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary

private val TrackBackground = Color(0xFFEDEFF3)
private val MessageIconContainer = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MpesaImportScreen(navController: NavController) {
    val context = LocalContext.current
    val smsReader = remember { AppModule.provideSmsReader(context) }
    val transactionRepository = remember { AppModule.provideTransactionRepository(context) }
    val categoryRepository = remember { AppModule.provideCategoryRepository(context) }
    val viewModel: MpesaImportViewModel = viewModel(
        factory = MpesaImportViewModel.Factory(smsReader, transactionRepository, categoryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            viewModel.onPermissionResult(true)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("M-Pesa import") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showRationale = true }) {
                        Icon(Icons.Outlined.Info, contentDescription = "About SMS import")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!uiState.permissionGranted) {
                PermissionRationaleCard(
                    denied = uiState.permissionDenied,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.READ_SMS) }
                )
            } else {
                FoundMessagesCard(count = uiState.foundCount)
                ImportProgressCard(uiState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    StatTile(
                        icon = Icons.Outlined.SwapHoriz,
                        label = "Duplicate skipped",
                        value = uiState.duplicateCount,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        icon = Icons.Outlined.Info,
                        label = "Failed to parse",
                        value = uiState.failedCount,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Done")
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Why PesaTrack asks for SMS access") },
            text = {
                Text(
                    "PesaTrack reads M-Pesa confirmation messages on this device to " +
                        "automatically import and categorise your transactions. This is " +
                        "optional — the app works fully without it, and you can always " +
                        "add transactions manually instead."
                )
            },
            confirmButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun PermissionRationaleCard(
    denied: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MessageIconContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Sms, contentDescription = null, tint = Income)
            }

            Text(
                text = "Import M-Pesa SMS",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Text(
                text = if (denied) {
                    "SMS access was denied. You can still add transactions manually — " +
                        "allow SMS access from your device Settings if you'd like to enable import later."
                } else {
                    "PesaTrack needs permission to read your SMS so it can find and import your " +
                        "M-Pesa confirmation messages. This is optional — you can always add " +
                        "transactions manually instead."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(if (denied) "Try again" else "Grant access")
            }
        }
    }
}

@Composable
private fun FoundMessagesCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Found messages",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MessageIconContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Outlined.Message, contentDescription = null, tint = Income)
            }
        }
    }
}

@Composable
private fun ImportProgressCard(state: MpesaImportUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Import progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "${state.progressPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TrackBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (state.progressPercent / 100f).coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Primary)
                )
            }

            if (state.isImporting || state.isComplete) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Income,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${state.importedCount} imported",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Background)
            .padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
