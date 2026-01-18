package com.romreviewertools.noteitup.presentation.screens.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import com.romreviewertools.noteitup.presentation.components.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.romreviewertools.noteitup.data.ai.ImprovementType
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.ImageAttachment
import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.model.Tag
import com.romreviewertools.noteitup.presentation.components.AIToolbar
import com.romreviewertools.noteitup.presentation.components.ImagePreview
import com.romreviewertools.noteitup.presentation.components.MediaPermissionHandler
import com.romreviewertools.noteitup.presentation.components.RichTextToolbar
import com.romreviewertools.noteitup.util.PlatformCapabilities
import kotlinx.coroutines.FlowPreview

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class, FlowPreview::class
)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    entryId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToAISettings: () -> Unit = {},
    onNavigateToBrainstorm: () -> Unit = {},
    onRequestReview: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Rich text state for WYSIWYG editing
    val richTextState = rememberRichTextState()

    // Options panel expanded state (collapsed by default to give more writing space)
    var isOptionsExpanded by remember { mutableStateOf(false) }

    // Debounce navigation to prevent multiple rapid taps
    var isNavigating by remember { mutableStateOf(false) }

    // Unsaved changes dialog state
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    // Track if title field is focused (to keep it visible when editing in landscape)
    var isTitleFocused by remember { mutableStateOf(false) }

    // Keyboard visibility detection
    val density = LocalDensity.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        // Sync when content changes from ViewModel (e.g., when loading entry)
        LaunchedEffect(uiState.content) {
            if (richTextState.toMarkdown() != uiState.content) {
                richTextState.setMarkdown(uiState.content)
            }
        }

        // Sync content from Editor back to ViewModel to track changes
        // Optimized to reduce recompositions: triggers only when content actually changes
        LaunchedEffect(richTextState) {
            snapshotFlow { richTextState.annotatedString }
                .debounce(500) // Debounce edits
                .distinctUntilChanged() // Only emit if text actually changed
                .collect {
                    val newContent = richTextState.toMarkdown()
                    if (newContent != uiState.content) {
                        viewModel.processIntent(EditorIntent.UpdateContent(newContent))
                    }
                }
        }

        LaunchedEffect(entryId) {
            if (entryId != null) {
                viewModel.processIntent(EditorIntent.LoadEntry(entryId))
            }
        }

        LaunchedEffect(uiState.isSaved, uiState.shouldShowReviewPrompt) {
            if (uiState.isSaved && !isNavigating) {
                isNavigating = true
                // Trigger in-app review if conditions are met
                if (uiState.shouldShowReviewPrompt) {
                    onRequestReview()
                }
                onNavigateBack()
            }
        }

        LaunchedEffect(uiState.error) {
            uiState.error?.let { error ->
                snackbarHostState.showSnackbar(error)
                viewModel.processIntent(EditorIntent.DismissError)
            }
        }

        LaunchedEffect(uiState.aiError) {
            uiState.aiError?.let { error ->
                val result = snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = "Settings",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onNavigateToAISettings()
                }
                viewModel.processIntent(EditorIntent.DismissAISuggestion)
            }
        }

        BackHandler(enabled = !isNavigating) {
            if (uiState.hasUnsavedChanges) {
                showUnsavedChangesDialog = true
            } else {
                isNavigating = true
                onNavigateBack()
            }
        }

        MediaPermissionHandler(
            onImagePicked = { filePath ->
                viewModel.processIntent(EditorIntent.AddImageFromPath(filePath))
            },
            onImagePickCancelled = {
                viewModel.processIntent(EditorIntent.CancelImagePicking)
            },
            onLocationPermissionResult = { granted ->
                if (granted) {
                    viewModel.processIntent(EditorIntent.AddLocation)
                }
            }
        ) { launchGallery, launchCamera, launchLocationPermission ->

            Scaffold(
                topBar = {
                    // Always show top bar on tablets, hide in landscape with keyboard on phones
                    if (!isLandscape || !isKeyboardOpen) {
                        TopAppBar(
                            title = {
                                Text(if (uiState.isNewEntry) "New Entry" else "Edit Entry")
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        if (!isNavigating) {
                                            if (uiState.hasUnsavedChanges) {
                                                showUnsavedChangesDialog = true
                                            } else {
                                                isNavigating = true
                                                onNavigateBack()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.processIntent(EditorIntent.ToggleFavorite) }
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = if (uiState.isFavorite) "Remove from favorites" else "Add to favorites",
                                        tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (!isNavigating && !uiState.isSaving) {
                                            // Save markdown content from rich text editor
                                            viewModel.processIntent(
                                                EditorIntent.UpdateContent(
                                                    richTextState.toMarkdown()
                                                )
                                            )
                                            viewModel.processIntent(EditorIntent.Save)
                                        }
                                    },
                                    enabled = !uiState.isSaving && !isNavigating
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Save"
                                        )
                                    }
                                }
                            }
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .imePadding()
                            .padding(16.dp)
                            // Only enable vertical scroll in landscape when keyboard is NOT open
                            // (weight modifier for editor requires non-scrolling parent)
                            .then(
                                if (isLandscape) Modifier.verticalScroll(
                                    scrollState
                                ) else Modifier
                            )
                    ) {
                        // Title field - hide when keyboard is open and editing content (unless title is focused)
                        if (!isKeyboardOpen || isTitleFocused) {
                            BasicTextField(
                                value = uiState.title,
                                onValueChange = {
                                    viewModel.processIntent(
                                        EditorIntent.UpdateTitle(it)
                                    )
                                },
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        isTitleFocused = focusState.isFocused
                                    },
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (uiState.title.isEmpty()) {
                                            Text(
                                                text = "Title",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.6f
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Rich text formatting toolbar - hide in landscape with keyboard
                        if (!isLandscape || !isKeyboardOpen) {
                            RichTextToolbar(
                                richTextState = richTextState,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // AI improvement toolbar - show in portrait with keyboard, hide in landscape with keyboard
                        if (!isKeyboardOpen || !isLandscape) {
                            AIToolbar(
                                onImprovementSelected = { improvementType: ImprovementType ->
                                    // Save current content before improving
                                    viewModel.processIntent(EditorIntent.UpdateContent(richTextState.toMarkdown()))
                                    viewModel.processIntent(EditorIntent.ImproveText(improvementType))
                                },
                                isLoading = uiState.isImprovingText,
                                onBrainstormClick = onNavigateToBrainstorm,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Rich Text Editor with WYSIWYG markdown editing
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isLandscape && isKeyboardOpen) {
                                        // In landscape with keyboard, fill remaining space
                                        Modifier.weight(1f)
                                    } else if (isLandscape) {
                                        // In landscape without keyboard, use fixed height
                                        Modifier.height(300.dp)
                                    } else {
                                        // In portrait, fill remaining space
                                        Modifier.weight(1f)
                                    }
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            RichTextEditor(
                                state = richTextState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                placeholder = {
                                    Text(
                                        text = "Write your thoughts...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )
                                }
                            )
                        }

                        // Collapsible Options Panel - hide when keyboard is open
                        if (!isKeyboardOpen) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Options header (clickable to expand/collapse)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { isOptionsExpanded = !isOptionsExpanded }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Show summary of selected options
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Options",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    // Show indicators for selected options
                                    uiState.mood?.let {
                                        Text(
                                            text = it.emoji,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    if (uiState.selectedFolderId != null) {
                                        Text(
                                            text = "📁",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    if (uiState.selectedTagIds.isNotEmpty()) {
                                        Text(
                                            text = "🏷️${uiState.selectedTagIds.size}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (uiState.images.isNotEmpty()) {
                                        Text(
                                            text = "🖼️${uiState.images.size}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (uiState.location != null) {
                                        Text(
                                            text = "📍",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (isOptionsExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    contentDescription = if (isOptionsExpanded) "Collapse options" else "Expand options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Expandable options content
                            AnimatedVisibility(
                                visible = isOptionsExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 400.dp)
                                        .padding(top = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                    // Mood selector
                                    Text(
                                        text = "How are you feeling?",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Mood.entries.forEach { mood ->
                                            MoodChip(
                                                mood = mood,
                                                isSelected = uiState.mood == mood,
                                                onClick = {
                                                    viewModel.processIntent(
                                                        EditorIntent.UpdateMood(
                                                            if (uiState.mood == mood) null else mood
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    // Folder selector
                                    if (uiState.availableFolders.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = "Folder",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // "None" option to remove from folder
                                            FilterChip(
                                                selected = uiState.selectedFolderId == null,
                                                onClick = {
                                                    viewModel.processIntent(
                                                        EditorIntent.SelectFolder(
                                                            null
                                                        )
                                                    )
                                                },
                                                label = { Text("None") }
                                            )
                                            uiState.availableFolders.forEach { folder ->
                                                FolderChip(
                                                    folder = folder,
                                                    isSelected = folder.id == uiState.selectedFolderId,
                                                    onClick = {
                                                        viewModel.processIntent(
                                                            EditorIntent.SelectFolder(
                                                                folder.id
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Tag selector
                                    if (uiState.availableTags.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = "Tags",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            uiState.availableTags.forEach { tag ->
                                                TagChip(
                                                    tag = tag,
                                                    isSelected = tag.id in uiState.selectedTagIds,
                                                    onClick = {
                                                        viewModel.processIntent(
                                                            EditorIntent.ToggleTag(
                                                                tag.id
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Image Attachments section
                                    Spacer(modifier = Modifier.height(16.dp))

                                    ImageAttachmentSection(
                                        images = uiState.images,
                                        isAddingImage = uiState.isAddingImage,
                                        onPickFromGallery = launchGallery,
                                        onTakePhoto = launchCamera,
                                        onRemoveImage = { imageId ->
                                            viewModel.processIntent(
                                                EditorIntent.RemoveImage(imageId)
                                            )
                                        }
                                    )

                                    // Location section - only show on platforms with GPS support
                                    if (PlatformCapabilities.hasLocationSupport() && uiState.isLocationAvailable) {
                                        Spacer(modifier = Modifier.height(16.dp))

                                        LocationSection(
                                            location = uiState.location,
                                            isLoading = uiState.isLoadingLocation,
                                            onAddLocation = launchLocationPermission,
                                            onRemoveLocation = {
                                                viewModel.processIntent(
                                                    EditorIntent.RemoveLocation
                                                )
                                            }
                                        )
                                    }

                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Unsaved Changes Dialog
                if (showUnsavedChangesDialog) {
                    AlertDialog(
                        onDismissRequest = { showUnsavedChangesDialog = false },
                        title = { Text("Unsaved Changes") },
                        text = { Text("You have unsaved changes. Do you want to save them?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showUnsavedChangesDialog = false
                                    // Save markdown content first
                                    viewModel.processIntent(EditorIntent.UpdateContent(richTextState.toMarkdown()))
                                    viewModel.processIntent(EditorIntent.Save)
                                }
                            ) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            Row {
                                TextButton(onClick = { showUnsavedChangesDialog = false }) {
                                    Text("Cancel")
                                }
                                TextButton(
                                    onClick = {
                                        showUnsavedChangesDialog = false
                                        isNavigating = true
                                        onNavigateBack()
                                    }
                                ) {
                                    Text("Discard")
                                }
                            }
                        }
                    )
                }

                // AI Suggestion Dialog
                if (uiState.aiSuggestion != null) {
                    AISuggestionDialog(
                        suggestion = uiState.aiSuggestion!!,
                        onAccept = {
                            viewModel.processIntent(EditorIntent.AcceptAISuggestion)
                            richTextState.setMarkdown(uiState.aiSuggestion!!)
                        },
                        onDismiss = {
                            viewModel.processIntent(EditorIntent.DismissAISuggestion)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodChip(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = mood.emoji)
                Text(text = mood.label)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun TagChip(
    tag: Tag,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tag.color?.let { color ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(color))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(text = tag.name)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun FolderChip(
    folder: Folder,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                folder.color?.let { color ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(color))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(text = folder.name)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ImageAttachmentSection(
    images: List<ImageAttachment>,
    isAddingImage: Boolean,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemoveImage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Attachments",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPickFromGallery,
                enabled = !isAddingImage
            ) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gallery")
            }

            // Only show camera button on platforms that support it
            if (PlatformCapabilities.hasCameraSupport()) {
                OutlinedButton(
                    onClick = onTakePhoto,
                    enabled = !isAddingImage
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Camera")
                }
            }

            if (isAddingImage) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        // Image thumbnails
        if (images.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.forEach { image ->
                    ImageThumbnail(
                        image = image,
                        onRemove = { onRemoveImage(image.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageThumbnail(
    image: ImageAttachment,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Display actual image using platform-specific ImagePreview
            ImagePreview(
                filePath = image.thumbnailPath ?: image.filePath,
                contentDescription = "Attached image",
                modifier = Modifier.fillMaxSize()
            )

            // Remove button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove image",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@Composable
private fun LocationSection(
    location: Location?,
    isLoading: Boolean,
    onAddLocation: () -> Unit,
    onRemoveLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Location",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (location != null) {
            // Show location chip
            AssistChip(
                onClick = onRemoveLocation,
                label = {
                    Text(
                        text = location.address ?: "${location.latitude}, ${location.longitude}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove location",
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        } else {
            // Show add location button
            OutlinedButton(
                onClick = onAddLocation,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Getting location...")
                } else {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Location")
                }
            }
        }
    }
}

@Composable
private fun AISuggestionDialog(
    suggestion: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("AI Suggestion")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "AI has improved your text:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("Accept")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
