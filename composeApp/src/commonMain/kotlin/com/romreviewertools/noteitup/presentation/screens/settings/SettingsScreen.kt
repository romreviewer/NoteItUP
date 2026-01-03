package com.romreviewertools.noteitup.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.romreviewertools.noteitup.domain.model.AccentColor
import com.romreviewertools.noteitup.domain.model.FontSize
import com.romreviewertools.noteitup.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onExportClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onCloudSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Theme Mode Section
                SettingsSection(title = "Theme") {
                    ThemeMode.entries.forEach { mode ->
                        ThemeOption(
                            mode = mode,
                            isSelected = uiState.themeMode == mode,
                            onClick = {
                                viewModel.processIntent(SettingsIntent.ChangeThemeMode(mode))
                            }
                        )
                    }
                }

                // Accent Color Section
                SettingsSection(title = "Accent Color") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AccentColor.entries.forEach { color ->
                            ColorOption(
                                color = color,
                                isSelected = uiState.accentColor == color,
                                onClick = {
                                    viewModel.processIntent(SettingsIntent.ChangeAccentColor(color))
                                }
                            )
                        }
                    }
                }

                // Font Size Section
                SettingsSection(title = "Font Size") {
                    FontSizeSelector(
                        currentSize = uiState.fontSize,
                        onSizeChange = { size ->
                            viewModel.processIntent(SettingsIntent.ChangeFontSize(size))
                        }
                    )
                }

                // Reminders Section
                SettingsSection(title = "Daily Reminder") {
                    ReminderToggle(
                        enabled = uiState.reminderSettings.enabled,
                        time = uiState.reminderSettings.time,
                        onToggle = { enabled ->
                            viewModel.processIntent(SettingsIntent.ToggleReminder(enabled))
                        },
                        onTimeChange = { time ->
                            viewModel.processIntent(SettingsIntent.ChangeReminderTime(time))
                        }
                    )
                }

                // Security Section
                SettingsSection(title = "Security") {
                    SettingsNavigationItem(
                        icon = Icons.Default.Lock,
                        title = "App Lock",
                        subtitle = "Protect your diary with PIN or biometrics",
                        onClick = onSecurityClick
                    )
                }

                // Data Section
                SettingsSection(title = "Data") {
                    SettingsNavigationItem(
                        icon = Icons.Default.Cloud,
                        title = "Cloud Sync",
                        subtitle = "Sync your diary to Google Drive or Dropbox",
                        onClick = onCloudSyncClick
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.FileDownload,
                        title = "Local Backup & Restore",
                        subtitle = "Export or import diary entries locally",
                        onClick = onExportClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsContent(
    viewModel: SettingsViewModel,
    onExportClick: () -> Unit,
    onSecurityClick: () -> Unit = {},
    onCloudSyncClick: () -> Unit = {},
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Permission launcher for notifications
    val requestNotificationPermission = rememberNotificationPermissionLauncher { granted ->
        viewModel.processIntent(SettingsIntent.OnNotificationPermissionResult(granted))
    }

    // Notification permission dialog
    if (uiState.showNotificationPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.processIntent(SettingsIntent.DismissPermissionDialog)
            },
            title = { Text("Notification Permission Required") },
            text = { Text("To receive daily reminders, please allow notifications for this app.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.processIntent(SettingsIntent.DismissPermissionDialog)
                    requestNotificationPermission()
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.processIntent(SettingsIntent.DismissPermissionDialog)
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Mode Section
            SettingsSection(title = "Theme") {
                ThemeMode.entries.forEach { mode ->
                    ThemeOption(
                        mode = mode,
                        isSelected = uiState.themeMode == mode,
                        onClick = {
                            viewModel.processIntent(SettingsIntent.ChangeThemeMode(mode))
                        }
                    )
                }
            }

            // Accent Color Section
            SettingsSection(title = "Accent Color") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AccentColor.entries.forEach { color ->
                        ColorOption(
                            color = color,
                            isSelected = uiState.accentColor == color,
                            onClick = {
                                viewModel.processIntent(SettingsIntent.ChangeAccentColor(color))
                            }
                        )
                    }
                }
            }

            // Font Size Section
            SettingsSection(title = "Font Size") {
                FontSizeSelector(
                    currentSize = uiState.fontSize,
                    onSizeChange = { size ->
                        viewModel.processIntent(SettingsIntent.ChangeFontSize(size))
                    }
                )
            }

            // Reminders Section
            SettingsSection(title = "Daily Reminder") {
                ReminderToggle(
                    enabled = uiState.reminderSettings.enabled,
                    time = uiState.reminderSettings.time,
                    onToggle = { enabled ->
                        viewModel.processIntent(SettingsIntent.ToggleReminder(enabled))
                    },
                    onTimeChange = { time ->
                        viewModel.processIntent(SettingsIntent.ChangeReminderTime(time))
                    }
                )
            }

            // Security Section
            SettingsSection(title = "Security") {
                SettingsNavigationItem(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    subtitle = "Protect your diary with PIN or biometrics",
                    onClick = onSecurityClick
                )
            }

            // Data Section
            SettingsSection(title = "Data") {
                SettingsNavigationItem(
                    icon = Icons.Default.Cloud,
                    title = "Cloud Sync",
                    subtitle = "Sync your diary to Google Drive or Dropbox",
                    onClick = onCloudSyncClick
                )
                SettingsNavigationItem(
                    icon = Icons.Default.FileDownload,
                    title = "Local Backup & Restore",
                    subtitle = "Export or import diary entries locally",
                    onClick = onExportClick
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Text(
            text = when (mode) {
                ThemeMode.LIGHT -> "Light"
                ThemeMode.DARK -> "Dark"
                ThemeMode.SYSTEM -> "System default"
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ColorOption(
    color: AccentColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(color.colorValue))
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun FontSizeSelector(
    currentSize: FontSize,
    onSizeChange: (FontSize) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "A",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when (currentSize) {
                    FontSize.SMALL -> "Small"
                    FontSize.MEDIUM -> "Medium"
                    FontSize.LARGE -> "Large"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "A",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = when (currentSize) {
                FontSize.SMALL -> 0f
                FontSize.MEDIUM -> 0.5f
                FontSize.LARGE -> 1f
            },
            onValueChange = { value ->
                val newSize = when {
                    value < 0.33f -> FontSize.SMALL
                    value < 0.66f -> FontSize.MEDIUM
                    else -> FontSize.LARGE
                }
                if (newSize != currentSize) {
                    onSizeChange(newSize)
                }
            },
            steps = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Preview text
        Text(
            text = "Preview: This is how your text will look.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = MaterialTheme.typography.bodyMedium.fontSize * currentSize.scaleFactor
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsNavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReminderToggle(
    enabled: Boolean,
    time: LocalTime,
    onToggle: (Boolean) -> Unit,
    onTimeChange: (LocalTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Column {
        // Enable/Disable toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "Enable daily reminder",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Get reminded to write in your diary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }

        // Time picker (only visible when enabled)
        if (enabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showTimePicker = true }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reminder time",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = formatTime(time),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = time,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { selectedTime ->
                onTimeChange(selectedTime)
                showTimePicker = false
            }
        )
    }
}

@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set reminder time") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Hour selector
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Hour", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { selectedHour = (selectedHour - 1 + 24) % 24 }
                            ) {
                                Text("-", style = MaterialTheme.typography.headlineMedium)
                            }
                            Text(
                                text = selectedHour.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { selectedHour = (selectedHour + 1) % 24 }
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }

                    Text(
                        ":",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Minute selector
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Minute", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { selectedMinute = (selectedMinute - 5 + 60) % 60 }
                            ) {
                                Text("-", style = MaterialTheme.typography.headlineMedium)
                            }
                            Text(
                                text = selectedMinute.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { selectedMinute = (selectedMinute + 5) % 60 }
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onTimeSelected(LocalTime(selectedHour, selectedMinute)) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(time: LocalTime): String {
    val hour = time.hour
    val minute = time.minute.toString().padStart(2, '0')
    return if (hour < 12) {
        val displayHour = if (hour == 0) 12 else hour
        "$displayHour:$minute AM"
    } else {
        val displayHour = if (hour == 12) 12 else hour - 12
        "$displayHour:$minute PM"
    }
}
