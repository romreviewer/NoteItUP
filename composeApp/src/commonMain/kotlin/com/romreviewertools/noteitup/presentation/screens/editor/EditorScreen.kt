package com.romreviewertools.noteitup.presentation.screens.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.romreviewertools.noteitup.domain.model.Folder
import com.romreviewertools.noteitup.domain.model.ImageAttachment
import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.model.Tag
import com.romreviewertools.noteitup.presentation.components.ImagePreview
import com.romreviewertools.noteitup.presentation.components.MarkdownToolbar
import com.romreviewertools.noteitup.presentation.components.MediaPermissionHandler
import com.romreviewertools.noteitup.presentation.components.applyMarkdownFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    entryId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Local state for content TextFieldValue to support text selection for markdown
    var contentTextFieldValue by remember { mutableStateOf(TextFieldValue(uiState.content)) }

    // Preview mode toggle
    var isPreviewMode by remember { mutableStateOf(false) }

    // Options panel expanded state (collapsed by default to give more writing space)
    var isOptionsExpanded by remember { mutableStateOf(false) }

    // Sync when content changes from ViewModel (e.g., when loading entry)
    LaunchedEffect(uiState.content) {
        if (contentTextFieldValue.text != uiState.content) {
            contentTextFieldValue = TextFieldValue(uiState.content)
        }
    }

    LaunchedEffect(entryId) {
        if (entryId != null) {
            viewModel.processIntent(EditorIntent.LoadEntry(entryId))
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.processIntent(EditorIntent.DismissError)
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
            TopAppBar(
                title = {
                    Text(if (uiState.isNewEntry) "New Entry" else "Edit Entry")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Preview/Edit toggle
                    IconButton(
                        onClick = { isPreviewMode = !isPreviewMode }
                    ) {
                        Icon(
                            imageVector = if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                            contentDescription = if (isPreviewMode) "Edit" else "Preview",
                            tint = if (isPreviewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
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
                        onClick = { viewModel.processIntent(EditorIntent.Save) },
                        enabled = !uiState.isSaving
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
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .padding(16.dp)
            ) {
                // Title field
                BasicTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.processIntent(EditorIntent.UpdateTitle(it)) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.title.isEmpty()) {
                                Text(
                                    text = "Title",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Markdown toolbar (always visible in edit mode)
                if (!isPreviewMode) {
                    MarkdownToolbar(
                        onFormatClick = { format ->
                            contentTextFieldValue = applyMarkdownFormat(contentTextFieldValue, format)
                            viewModel.processIntent(EditorIntent.UpdateContent(contentTextFieldValue.text))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Content area - takes most of the space
                if (isPreviewMode) {
                    // Preview mode - render markdown
                    if (contentTextFieldValue.text.isNotBlank()) {
                        Markdown(
                            content = contentTextFieldValue.text,
                            colors = markdownColor(
                                text = MaterialTheme.colorScheme.onSurface
                            ),
                            typography = markdownTypography(
                                paragraph = MaterialTheme.typography.bodyLarge
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nothing to preview",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Edit mode - content field
                    BasicTextField(
                        value = contentTextFieldValue,
                        onValueChange = { newValue ->
                            contentTextFieldValue = newValue
                            viewModel.processIntent(EditorIntent.UpdateContent(newValue.text))
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        decorationBox = { innerTextField ->
                            Box {
                                if (contentTextFieldValue.text.isEmpty()) {
                                    Text(
                                        text = "Write your thoughts...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // Collapsible Options Panel
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
                            Text(text = it.emoji, style = MaterialTheme.typography.bodyMedium)
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
                            .verticalScroll(rememberScrollState())
                            .padding(top = 8.dp)
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
                                        viewModel.processIntent(EditorIntent.SelectFolder(null))
                                    },
                                    label = { Text("None") }
                                )
                                uiState.availableFolders.forEach { folder ->
                                    FolderChip(
                                        folder = folder,
                                        isSelected = folder.id == uiState.selectedFolderId,
                                        onClick = {
                                            viewModel.processIntent(EditorIntent.SelectFolder(folder.id))
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
                                            viewModel.processIntent(EditorIntent.ToggleTag(tag.id))
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
                            onRemoveImage = { imageId -> viewModel.processIntent(EditorIntent.RemoveImage(imageId)) }
                        )

                        // Location section
                        if (uiState.isLocationAvailable) {
                            Spacer(modifier = Modifier.height(16.dp))

                            LocationSection(
                                location = uiState.location,
                                isLoading = uiState.isLoadingLocation,
                                onAddLocation = launchLocationPermission,
                                onRemoveLocation = { viewModel.processIntent(EditorIntent.RemoveLocation) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    } // MediaPermissionHandler
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
                        text = location.address ?: "%.4f, %.4f".format(location.latitude, location.longitude),
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
