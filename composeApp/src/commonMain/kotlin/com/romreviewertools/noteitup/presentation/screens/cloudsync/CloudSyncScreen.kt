package com.romreviewertools.noteitup.presentation.screens.cloudsync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.romreviewertools.noteitup.data.cloud.BackupInfo
import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import com.romreviewertools.noteitup.data.cloud.QuotaInfo
import com.romreviewertools.noteitup.data.cloud.SyncState
import com.romreviewertools.noteitup.domain.model.AutoSyncInterval
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    onNavigateBack: () -> Unit,
    viewModel: CloudSyncViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Check for pending OAuth callback on initial composition
    LaunchedEffect(Unit) {
        consumePendingOAuthCallback()?.let { callback ->
            viewModel.handleIntent(
                CloudSyncIntent.HandleOAuthCallback(
                    provider = callback.provider,
                    code = callback.code
                )
            )
        }
    }

    // Also collect from the shared flow for callbacks that arrive while screen is visible
    LaunchedEffect(Unit) {
        OAuthCallbackEmitter.callbacks.collect { callback ->
            viewModel.handleIntent(
                CloudSyncIntent.HandleOAuthCallback(
                    provider = callback.provider,
                    code = callback.code
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloud Sync") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sync Status Card
            item {
                SyncStatusCard(
                    syncState = uiState.syncState,
                    progress = uiState.syncProgress,
                    currentOperation = uiState.currentOperation,
                    lastSyncTime = uiState.lastSyncTime
                )
            }

            // Cloud Providers Section
            item {
                Text(
                    text = "Cloud Providers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Google Drive - Coming Soon
            item {
                ComingSoonProviderCard(
                    providerName = "Google Drive",
                )
            }

            item {
                ProviderCard(
                    provider = CloudProviderType.DROPBOX,
                    isConnected = uiState.dropboxConnected,
                    quota = uiState.dropboxQuota,
                    isSyncing = uiState.isSyncing,
                    onConnect = { viewModel.handleIntent(CloudSyncIntent.ConnectProvider(CloudProviderType.DROPBOX)) },
                    onDisconnect = { viewModel.handleIntent(CloudSyncIntent.DisconnectProvider(CloudProviderType.DROPBOX)) },
                    onBackup = { viewModel.handleIntent(CloudSyncIntent.StartBackup(CloudProviderType.DROPBOX)) },
                    onRestore = { viewModel.handleIntent(CloudSyncIntent.ShowBackupList(CloudProviderType.DROPBOX)) }
                )
            }

            // Auto Sync Settings Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Auto Sync Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                AutoSyncSettingsCard(
                    autoSyncEnabled = uiState.autoSyncEnabled,
                    autoSyncInterval = uiState.autoSyncInterval,
                    syncOnWifiOnly = uiState.syncOnWifiOnly,
                    isAnyProviderConnected = uiState.isAnyProviderConnected,
                    onAutoSyncEnabledChange = { viewModel.handleIntent(CloudSyncIntent.SetAutoSyncEnabled(it)) },
                    onAutoSyncIntervalChange = { viewModel.handleIntent(CloudSyncIntent.SetAutoSyncInterval(it)) },
                    onSyncOnWifiOnlyChange = { viewModel.handleIntent(CloudSyncIntent.SetSyncOnWifiOnly(it)) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Dialogs
        if (uiState.showPasswordDialog) {
            PasswordDialog(
                action = uiState.passwordDialogAction,
                onConfirm = { password ->
                    if (uiState.passwordDialogAction == PasswordDialogAction.BACKUP) {
                        viewModel.handleIntent(CloudSyncIntent.ConfirmBackupWithPassword(password))
                    } else {
                        viewModel.handleIntent(CloudSyncIntent.ConfirmRestoreWithPassword(password))
                    }
                },
                onDismiss = { viewModel.handleIntent(CloudSyncIntent.HidePasswordDialog) }
            )
        }

        if (uiState.showBackupListDialog) {
            BackupListDialog(
                backups = uiState.backups,
                isLoading = uiState.isLoadingBackups,
                onSelectBackup = { viewModel.handleIntent(CloudSyncIntent.SelectBackupForRestore(it)) },
                onDeleteBackup = { viewModel.handleIntent(CloudSyncIntent.DeleteBackup(it)) },
                onDismiss = { viewModel.handleIntent(CloudSyncIntent.HideBackupList) }
            )
        }

        if (uiState.showDeleteConfirmDialog && uiState.backupToDelete != null) {
            AlertDialog(
                onDismissRequest = { viewModel.handleIntent(CloudSyncIntent.CancelDeleteBackup) },
                title = { Text("Delete Backup?") },
                text = { Text("This will permanently delete the backup. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.handleIntent(CloudSyncIntent.ConfirmDeleteBackup(uiState.backupToDelete!!)) }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.handleIntent(CloudSyncIntent.CancelDeleteBackup) }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Authenticating dialog
        if (uiState.isAuthenticating) {
            AlertDialog(
                onDismissRequest = { /* Cannot dismiss while authenticating */ },
                title = { Text("Connecting...") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Connecting to ${uiState.authenticatingProvider?.name?.replace("_", " ") ?: "cloud provider"}...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = { }
            )
        }

        if (uiState.showDisconnectConfirmDialog && uiState.providerToDisconnect != null) {
            AlertDialog(
                onDismissRequest = { viewModel.handleIntent(CloudSyncIntent.CancelDisconnect) },
                title = { Text("Disconnect?") },
                text = { Text("This will remove the connection to ${uiState.providerToDisconnect!!.name.replace("_", " ")}. Your backups will remain in the cloud.") },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.handleIntent(CloudSyncIntent.ConfirmDisconnect(uiState.providerToDisconnect!!)) }
                    ) {
                        Text("Disconnect")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.handleIntent(CloudSyncIntent.CancelDisconnect) }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Snackbars
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(3000)
                viewModel.handleIntent(CloudSyncIntent.DismissError)
            }
        }

        uiState.successMessage?.let { success ->
            LaunchedEffect(success) {
                kotlinx.coroutines.delay(3000)
                viewModel.handleIntent(CloudSyncIntent.DismissSuccess)
            }
        }
    }
}

@Composable
private fun SyncStatusCard(
    syncState: SyncState,
    progress: Float,
    currentOperation: String?,
    lastSyncTime: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (syncState) {
                SyncState.SYNCING -> MaterialTheme.colorScheme.primaryContainer
                SyncState.SUCCESS -> MaterialTheme.colorScheme.secondaryContainer
                SyncState.ERROR -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (syncState) {
                        SyncState.SYNCING -> Icons.Default.Sync
                        SyncState.SUCCESS -> Icons.Default.CheckCircle
                        SyncState.ERROR -> Icons.Default.Error
                        else -> Icons.Default.Cloud
                    },
                    contentDescription = null
                )
                Text(
                    text = when (syncState) {
                        SyncState.SYNCING -> currentOperation ?: "Syncing..."
                        SyncState.SUCCESS -> "Sync complete"
                        SyncState.ERROR -> "Sync failed"
                        else -> "Ready to sync"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedVisibility(visible = syncState == SyncState.SYNCING) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            lastSyncTime?.let { time ->
                Text(
                    text = "Last sync: ${formatDateTime(time)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProviderCard(
    provider: CloudProviderType,
    isConnected: Boolean,
    quota: QuotaInfo?,
    isSyncing: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Column {
                        Text(
                            text = provider.name.replace("_", " "),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (isConnected) "Connected" else "Not connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (isConnected) {
                    TextButton(onClick = onDisconnect) {
                        Text("Disconnect")
                    }
                } else {
                    Button(onClick = onConnect) {
                        Text("Connect")
                    }
                }
            }

            if (isConnected) {
                Spacer(modifier = Modifier.height(12.dp))

                // Quota info
                quota?.let { q ->
                    val usedGB = q.used / (1024.0 * 1024 * 1024)
                    val totalGB = q.total / (1024.0 * 1024 * 1024)
                    val percentage = (q.used.toFloat() / q.total.toFloat()).coerceIn(0f, 1f)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { percentage },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "%.1f GB / %.1f GB used".format(usedGB, totalGB),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackup,
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Backup")
                    }
                    OutlinedButton(
                        onClick = onRestore,
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore")
                    }
                }
            }
        }
    }
}

@Composable
private fun ComingSoonProviderCard(
    providerName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Column {
                        Text(
                            text = providerName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Coming soon",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoSyncSettingsCard(
    autoSyncEnabled: Boolean,
    autoSyncInterval: AutoSyncInterval,
    syncOnWifiOnly: Boolean,
    isAnyProviderConnected: Boolean,
    onAutoSyncEnabledChange: (Boolean) -> Unit,
    onAutoSyncIntervalChange: (AutoSyncInterval) -> Unit,
    onSyncOnWifiOnlyChange: (Boolean) -> Unit
) {
    var showIntervalDropdown by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Auto-sync toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Auto Sync",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Automatically backup changes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = onAutoSyncEnabledChange,
                    enabled = isAnyProviderConnected
                )
            }

            AnimatedVisibility(visible = autoSyncEnabled && isAnyProviderConnected) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Interval selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sync Interval",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Box {
                            TextButton(onClick = { showIntervalDropdown = true }) {
                                Text(autoSyncInterval.label)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showIntervalDropdown,
                                onDismissRequest = { showIntervalDropdown = false }
                            ) {
                                AutoSyncInterval.entries.forEach { interval ->
                                    DropdownMenuItem(
                                        text = { Text(interval.label) },
                                        onClick = {
                                            onAutoSyncIntervalChange(interval)
                                            showIntervalDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // WiFi only toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WiFi Only",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Only sync on WiFi networks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = syncOnWifiOnly,
                            onCheckedChange = onSyncOnWifiOnlyChange
                        )
                    }
                }
            }

            if (!isAnyProviderConnected) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connect a cloud provider to enable auto-sync",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun PasswordDialog(
    action: PasswordDialogAction,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val isBackup = action == PasswordDialogAction.BACKUP

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBackup) "Encrypt Backup" else "Decrypt Backup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isBackup) {
                        "Enter a password to encrypt your backup. You'll need this password to restore."
                    } else {
                        "Enter the password used when creating this backup."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        error = null
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isBackup) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            error = null
                        },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        password.length < 4 -> error = "Password must be at least 4 characters"
                        isBackup && password != confirmPassword -> error = "Passwords don't match"
                        else -> onConfirm(password)
                    }
                }
            ) {
                Text(if (isBackup) "Encrypt & Upload" else "Decrypt & Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BackupListDialog(
    backups: List<BackupInfo>,
    isLoading: Boolean,
    onSelectBackup: (BackupInfo) -> Unit,
    onDeleteBackup: (BackupInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Backup to Restore") },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (backups.isEmpty()) {
                    Text(
                        text = "No backups found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(backups) { backup ->
                            BackupListItem(
                                backup = backup,
                                onSelect = { onSelectBackup(backup) },
                                onDelete = { onDeleteBackup(backup) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BackupListItem(
    backup: BackupInfo,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = backup.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatDateTime(backup.createdAt)} - ${backup.formattedSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.year}-${dateTime.monthNumber.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')} " +
            "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}
