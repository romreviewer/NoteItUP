package com.romreviewertools.noteitup.presentation.screens.aisettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.romreviewertools.noteitup.data.ai.AvailableModels
import com.romreviewertools.noteitup.data.ai.ModelDownloadState
import com.romreviewertools.noteitup.domain.model.AIProvider
import com.romreviewertools.noteitup.presentation.components.FilePickerLauncher
import com.romreviewertools.noteitup.presentation.components.rememberFilePickerLauncher
import com.romreviewertools.noteitup.util.PlatformCapabilities

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    viewModel: AISettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error or test result in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onIntent(AISettingsIntent.DismissError)
        }
    }

    LaunchedEffect(uiState.testResult) {
        when (val result = uiState.testResult) {
            is TestResult.Success -> {
                snackbarHostState.showSnackbar("Connection successful!")
                viewModel.onIntent(AISettingsIntent.DismissError)
            }
            is TestResult.Failure -> {
                snackbarHostState.showSnackbar("Connection failed: ${result.message}")
                viewModel.onIntent(AISettingsIntent.DismissError)
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "AI-Powered Writing Assistant",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Use cloud APIs with your own key, or run AI privately on your device with Gemma 4.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Enable AI Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                ListItem(
                    headlineContent = { Text("Enable AI Features") },
                    supportingContent = { Text("Use AI to improve your writing") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = uiState.settings.enabled,
                            onCheckedChange = { viewModel.onIntent(AISettingsIntent.UpdateAIEnabled(it)) }
                        )
                    }
                )
            }

            // Provider Selection
            if (uiState.settings.enabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AI Provider",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ProviderSelector(
                            selectedProvider = uiState.settings.selectedProvider,
                            onProviderSelected = { viewModel.onIntent(AISettingsIntent.SelectProvider(it)) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Provider info
                        val provider = uiState.settings.selectedProvider
                        Text(
                            text = provider.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (provider.isLocal) {
                            Text(
                                text = "On-device - No API key needed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (provider.hasFreeTier) {
                            Text(
                                text = "Free tier available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                val provider = uiState.settings.selectedProvider

                if (provider.isLocal) {
                    // File picker for importing existing .litertlm model files
                    val modelFilePickerLauncher = rememberFilePickerLauncher(
                        mimeType = "*/*", // .litertlm has no standard MIME type
                        onFilePicked = { uri ->
                            viewModel.onIntent(AISettingsIntent.ImportModelFile(uri))
                        }
                    )

                    // --- Local Model Management Card ---
                    LocalModelCard(
                        downloadState = uiState.modelDownloadState,
                        isModelLoaded = uiState.isModelLoaded,
                        isModelLoading = uiState.isModelLoading,
                        onDownload = { viewModel.onIntent(AISettingsIntent.DownloadModel) },
                        onCancel = { viewModel.onIntent(AISettingsIntent.CancelDownload) },
                        onDelete = { viewModel.onIntent(AISettingsIntent.DeleteModel) },
                        onLoad = { viewModel.onIntent(AISettingsIntent.LoadModel) },
                        onUnload = { viewModel.onIntent(AISettingsIntent.UnloadModel) },
                        onImport = { modelFilePickerLauncher.launch() },
                        onTest = { viewModel.onIntent(AISettingsIntent.TestConnection) },
                        isTestingConnection = uiState.isTestingConnection
                    )
                } else {
                    // --- Cloud API Key Card ---
                    ApiKeyCard(
                        apiKey = uiState.settings.apiKey,
                        isTestingConnection = uiState.isTestingConnection,
                        providerDisplayName = provider.displayName,
                        onApiKeyChange = { viewModel.onIntent(AISettingsIntent.UpdateApiKey(it)) },
                        onTestConnection = { viewModel.onIntent(AISettingsIntent.TestConnection) },
                        onClearApiKey = { viewModel.onIntent(AISettingsIntent.ClearApiKey) },
                        onOpenApiKeyUrl = { viewModel.onIntent(AISettingsIntent.OpenApiKeyUrl) }
                    )

                    // Additional Settings
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Advanced Settings",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ListItem(
                                headlineContent = { Text("Enable Streaming") },
                                supportingContent = { Text("Get real-time AI responses") },
                                trailingContent = {
                                    Switch(
                                        checked = uiState.settings.streamingEnabled,
                                        onCheckedChange = {
                                            viewModel.onIntent(AISettingsIntent.UpdateStreamingEnabled(it))
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---- Local Model Management Card ----

@Composable
private fun LocalModelCard(
    downloadState: ModelDownloadState,
    isModelLoaded: Boolean,
    isModelLoading: Boolean,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onLoad: () -> Unit,
    onUnload: () -> Unit,
    onImport: () -> Unit,
    onTest: () -> Unit,
    isTestingConnection: Boolean,
    modifier: Modifier = Modifier
) {
    val model = AvailableModels.GEMMA_4_E2B
    val sizeGb = ((model.sizeBytes / 100_000_000) / 10.0).toString().take(3) // ~1.6

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Local Model",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${model.name} (~${sizeGb} GB)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = model.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (downloadState) {
                is ModelDownloadState.NotDownloaded -> {
                    // Download / Import buttons
                    Text(
                        text = "Status: Not downloaded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDownload,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download")
                        }

                        OutlinedButton(
                            onClick = onImport,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.FileOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Select File")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Download from HuggingFace or select a .litertlm file you already have.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ModelDownloadState.Downloading -> {
                    // Progress indicator
                    val percent = (downloadState.progress * 100).toInt()
                    Text(
                        text = "Downloading... $percent%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { downloadState.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancel Download")
                    }
                }

                is ModelDownloadState.Downloaded -> {
                    // Model ready - show load/delete/test actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isModelLoaded) "Model loaded and ready" else "Model downloaded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Load / Unload button
                    if (!isModelLoaded) {
                        Button(
                            onClick = onLoad,
                            enabled = !isModelLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isModelLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loading model...")
                            } else {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Load Model")
                            }
                        }
                        Text(
                            text = "Loading takes ~5-10 seconds. The model stays in memory until you unload it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Test & Unload
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onTest,
                                enabled = !isTestingConnection,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                } 
                                Text(if (isTestingConnection) "Testing..." else "Test Model")
                            }

                            OutlinedButton(
                                onClick = onUnload,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Unload")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Delete button
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Model (~${sizeGb} GB)")
                    }
                }

                is ModelDownloadState.Error -> {
                    Text(
                        text = downloadState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDownload,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retry Download")
                        }
                        OutlinedButton(
                            onClick = onImport,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Select File")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Privacy: Model runs entirely on your device. Your diary content never leaves your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Requires 8GB+ RAM. Supported format: .litertlm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ---- Cloud API Key Card ----

@Composable
private fun ApiKeyCard(
    apiKey: String,
    isTestingConnection: Boolean,
    providerDisplayName: String,
    onApiKeyChange: (String) -> Unit,
    onTestConnection: () -> Unit,
    onClearApiKey: () -> Unit,
    onOpenApiKeyUrl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "API Key",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            ApiKeyInput(
                apiKey = apiKey,
                onApiKeyChange = onApiKeyChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your API key is stored securely on your device and never shared.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Get API Key button
            TextButton(
                onClick = onOpenApiKeyUrl,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't have an API key? Get one from $providerDisplayName")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestConnection,
                    enabled = apiKey.isNotBlank() && !isTestingConnection,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isTestingConnection) "Testing..." else "Test Connection")
                }

                if (apiKey.isNotBlank()) {
                    OutlinedButton(
                        onClick = onClearApiKey,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}

// ---- Provider Selector ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderSelector(
    selectedProvider: AIProvider,
    onProviderSelected: (AIProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Filter providers based on platform capabilities
    val availableProviders = AIProvider.entries.filter { provider ->
        if (provider.isLocal) PlatformCapabilities.hasLocalAISupport() else true
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedProvider.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Provider") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableProviders.forEach { provider ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(provider.displayName)
                            Text(
                                text = when {
                                    provider.isLocal -> "On-device, no API key needed"
                                    provider.hasFreeTier -> "Free tier available"
                                    else -> "Paid only"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (provider.isLocal)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onProviderSelected(provider)
                        expanded = false
                    },
                    leadingIcon = {
                        if (provider == selectedProvider) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}

// ---- API Key Input ----

@Composable
private fun ApiKeyInput(
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = apiKey,
        onValueChange = onApiKeyChange,
        label = { Text("API Key") },
        placeholder = { Text("Enter your API key") },
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = if (passwordVisible) "Hide API key" else "Show API key"
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}
