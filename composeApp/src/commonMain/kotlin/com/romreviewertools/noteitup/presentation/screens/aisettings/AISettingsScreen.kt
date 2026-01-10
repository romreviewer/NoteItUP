package com.romreviewertools.noteitup.presentation.screens.aisettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import com.romreviewertools.noteitup.domain.model.AIProvider

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
                text = "Bring your own API key to enhance your writing with AI suggestions and improvements.",
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
                        if (provider.hasFreeTier) {
                            Text(
                                text = "✓ Has free tier",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // API Key Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "API Key",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ApiKeyInput(
                            apiKey = uiState.settings.apiKey,
                            onApiKeyChange = { viewModel.onIntent(AISettingsIntent.UpdateApiKey(it)) }
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
                            onClick = { viewModel.onIntent(AISettingsIntent.OpenApiKeyUrl) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Don't have an API key? Get one from ${uiState.settings.selectedProvider.displayName}")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.onIntent(AISettingsIntent.TestConnection) },
                                enabled = uiState.settings.apiKey.isNotBlank() && !uiState.isTestingConnection,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isTestingConnection) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (uiState.isTestingConnection) "Testing..." else "Test Connection")
                            }

                            if (uiState.settings.apiKey.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { viewModel.onIntent(AISettingsIntent.ClearApiKey) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Clear")
                                }
                            }
                        }
                    }
                }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderSelector(
    selectedProvider: AIProvider,
    onProviderSelected: (AIProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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
            AIProvider.entries.forEach { provider ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(provider.displayName)
                            Text(
                                text = if (provider.hasFreeTier) "Free tier available" else "Paid only",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
