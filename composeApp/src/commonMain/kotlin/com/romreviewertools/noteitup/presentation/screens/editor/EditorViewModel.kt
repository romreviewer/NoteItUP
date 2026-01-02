package com.romreviewertools.noteitup.presentation.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.ImageAttachment
import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import com.romreviewertools.noteitup.domain.usecase.CreateEntryUseCase
import com.romreviewertools.noteitup.domain.usecase.GetAllFoldersUseCase
import com.romreviewertools.noteitup.domain.usecase.GetAllTagsUseCase
import com.romreviewertools.noteitup.domain.usecase.GetEntryByIdUseCase
import com.romreviewertools.noteitup.domain.usecase.UpdateEntryUseCase
import com.romreviewertools.noteitup.data.media.ImagePicker
import com.romreviewertools.noteitup.data.media.ImagePickerResult
import com.romreviewertools.noteitup.data.location.LocationService
import com.romreviewertools.noteitup.data.location.LocationPermissionStatus
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class EditorViewModel(
    private val getEntryByIdUseCase: GetEntryByIdUseCase,
    private val createEntryUseCase: CreateEntryUseCase,
    private val updateEntryUseCase: UpdateEntryUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val repository: DiaryRepository,
    private val imagePicker: ImagePicker,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var originalEntry: DiaryEntry? = null
    private var originalTagIds: Set<String> = emptySet()
    private var originalFolderId: String? = null
    private var originalImages: List<ImageAttachment> = emptyList()

    init {
        loadAvailableTags()
        loadAvailableFolders()
        checkLocationAvailability()
    }

    private fun loadAvailableTags() {
        viewModelScope.launch {
            getAllTagsUseCase()
                .catch { /* ignore errors loading tags */ }
                .collect { tags ->
                    _uiState.update { it.copy(availableTags = tags) }
                }
        }
    }

    private fun loadAvailableFolders() {
        viewModelScope.launch {
            getAllFoldersUseCase()
                .catch { /* ignore errors loading folders */ }
                .collect { folders ->
                    _uiState.update { it.copy(availableFolders = folders) }
                }
        }
    }

    private fun checkLocationAvailability() {
        _uiState.update { it.copy(isLocationAvailable = locationService.isLocationAvailable()) }
    }

    fun processIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.LoadEntry -> loadEntry(intent.entryId)
            is EditorIntent.UpdateTitle -> updateTitle(intent.title)
            is EditorIntent.UpdateContent -> updateContent(intent.content)
            is EditorIntent.UpdateMood -> updateMood(intent.mood)
            is EditorIntent.ToggleTag -> toggleTag(intent.tagId)
            is EditorIntent.SelectFolder -> selectFolder(intent.folderId)
            is EditorIntent.ToggleFavorite -> toggleFavorite()
            is EditorIntent.Save -> save()
            is EditorIntent.DismissError -> dismissError()
            // Image intents
            is EditorIntent.PickImageFromGallery -> setAddingImage()
            is EditorIntent.TakePhoto -> setAddingImage()
            is EditorIntent.AddImageFromPath -> addImageFromPath(intent.filePath)
            is EditorIntent.RemoveImage -> removeImage(intent.imageId)
            is EditorIntent.CancelImagePicking -> cancelImagePicking()
            // Location intents
            is EditorIntent.AddLocation -> addLocation()
            is EditorIntent.RemoveLocation -> removeLocation()
        }
    }

    private fun loadEntry(entryId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val entry = getEntryByIdUseCase(entryId)
                if (entry != null) {
                    originalEntry = entry
                    originalTagIds = entry.tags.map { it.id }.toSet()
                    originalFolderId = entry.folderId
                    originalImages = entry.images
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            entryId = entry.id,
                            title = entry.title,
                            content = entry.content,
                            mood = entry.mood,
                            isFavorite = entry.isFavorite,
                            isNewEntry = false,
                            selectedTagIds = originalTagIds,
                            selectedFolderId = originalFolderId,
                            images = entry.images,
                            location = entry.location
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Entry not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load entry"
                    )
                }
            }
        }
    }

    private fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    private fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    private fun updateMood(mood: Mood?) {
        _uiState.update { it.copy(mood = mood) }
    }

    private fun toggleTag(tagId: String) {
        _uiState.update { state ->
            val newSelectedTags = if (tagId in state.selectedTagIds) {
                state.selectedTagIds - tagId
            } else {
                state.selectedTagIds + tagId
            }
            state.copy(selectedTagIds = newSelectedTags)
        }
    }

    private fun selectFolder(folderId: String?) {
        _uiState.update { it.copy(selectedFolderId = folderId) }
    }

    private fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    private fun save() {
        val currentState = _uiState.value
        if (currentState.title.isBlank() && currentState.content.isBlank()) {
            _uiState.update { it.copy(error = "Please add a title or content") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val result = if (currentState.isNewEntry) {
                createEntryUseCase(
                    title = currentState.title,
                    content = currentState.content,
                    folderId = currentState.selectedFolderId,
                    mood = currentState.mood,
                    location = currentState.location
                )
            } else {
                val now = Clock.System.now()
                val entry = originalEntry?.copy(
                    title = currentState.title.ifBlank { "Untitled" },
                    content = currentState.content,
                    updatedAt = now,
                    folderId = currentState.selectedFolderId,
                    isFavorite = currentState.isFavorite,
                    mood = currentState.mood,
                    location = currentState.location
                ) ?: DiaryEntry(
                    id = currentState.entryId ?: uuid4().toString(),
                    title = currentState.title.ifBlank { "Untitled" },
                    content = currentState.content,
                    createdAt = now,
                    updatedAt = now,
                    folderId = currentState.selectedFolderId,
                    isFavorite = currentState.isFavorite,
                    mood = currentState.mood,
                    location = currentState.location
                )
                updateEntryUseCase(entry)
            }

            result
                .onSuccess { savedEntry ->
                    // Update tag associations
                    updateTagAssociations(savedEntry.id, currentState.selectedTagIds)
                    // Update image associations
                    updateImageAssociations(savedEntry.id, currentState.images)

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = e.message ?: "Failed to save entry"
                        )
                    }
                }
        }
    }

    private suspend fun updateTagAssociations(entryId: String, selectedTagIds: Set<String>) {
        // Tags to add (in selectedTagIds but not in originalTagIds)
        val tagsToAdd = selectedTagIds - originalTagIds
        // Tags to remove (in originalTagIds but not in selectedTagIds)
        val tagsToRemove = originalTagIds - selectedTagIds

        tagsToAdd.forEach { tagId ->
            repository.addTagToEntry(entryId, tagId)
        }

        tagsToRemove.forEach { tagId ->
            repository.removeTagFromEntry(entryId, tagId)
        }
    }

    private suspend fun updateImageAssociations(entryId: String, currentImages: List<ImageAttachment>) {
        val originalImageIds = originalImages.map { it.id }.toSet()
        val currentImageIds = currentImages.map { it.id }.toSet()

        // Images to add (new images)
        val imagesToAdd = currentImages.filter { it.id !in originalImageIds }
        // Images to remove
        val imagesToRemove = originalImages.filter { it.id !in currentImageIds }

        imagesToAdd.forEach { image ->
            repository.addImageToEntry(entryId, image)
        }

        imagesToRemove.forEach { image ->
            repository.removeImageFromEntry(image.id)
            // Also delete the file
            imagePicker.deleteImage(image.filePath)
            image.thumbnailPath?.let { imagePicker.deleteImage(it) }
        }
    }

    // Image handling
    private fun setAddingImage() {
        _uiState.update { it.copy(isAddingImage = true) }
    }

    private fun cancelImagePicking() {
        _uiState.update { it.copy(isAddingImage = false) }
    }

    private fun addImageFromPath(sourcePath: String) {
        viewModelScope.launch {
            val fileName = "img_${Clock.System.now().toEpochMilliseconds()}.jpg"

            imagePicker.copyToAppStorage(sourcePath, fileName)
                .onSuccess { storedPath ->
                    val thumbnailResult = imagePicker.createThumbnail(storedPath, 200)
                    val thumbnailPath = thumbnailResult.getOrNull()

                    val newImage = ImageAttachment(
                        id = uuid4().toString(),
                        fileName = fileName,
                        filePath = storedPath,
                        thumbnailPath = thumbnailPath,
                        createdAt = Clock.System.now()
                    )

                    _uiState.update { state ->
                        state.copy(
                            images = state.images + newImage,
                            isAddingImage = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isAddingImage = false,
                            error = e.message ?: "Failed to add image"
                        )
                    }
                }
        }
    }

    private fun removeImage(imageId: String) {
        _uiState.update { state ->
            state.copy(images = state.images.filter { it.id != imageId })
        }
    }

    // Location handling
    private fun addLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true) }

            // Check permission
            val permissionStatus = locationService.getPermissionStatus()
            if (permissionStatus != LocationPermissionStatus.GRANTED) {
                val granted = locationService.requestPermission()
                if (!granted) {
                    _uiState.update {
                        it.copy(
                            isLoadingLocation = false,
                            error = "Location permission denied"
                        )
                    }
                    return@launch
                }
            }

            // Get current location
            locationService.getCurrentLocation()
                .onSuccess { locationResult ->
                    // Try to reverse geocode
                    val address = locationService.reverseGeocode(
                        locationResult.latitude,
                        locationResult.longitude
                    ).getOrNull()

                    val location = Location(
                        latitude = locationResult.latitude,
                        longitude = locationResult.longitude,
                        address = address
                    )

                    _uiState.update {
                        it.copy(
                            location = location,
                            isLoadingLocation = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingLocation = false,
                            error = e.message ?: "Failed to get location"
                        )
                    }
                }
        }
    }

    private fun removeLocation() {
        _uiState.update { it.copy(location = null) }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
