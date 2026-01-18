package com.romreviewertools.noteitup.presentation.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuid4
import com.romreviewertools.noteitup.data.analytics.AnalyticsEvent
import com.romreviewertools.noteitup.data.analytics.AnalyticsService
import com.romreviewertools.noteitup.data.location.LocationPermissionStatus
import com.romreviewertools.noteitup.data.location.LocationService
import com.romreviewertools.noteitup.data.media.ImagePicker
import com.romreviewertools.noteitup.data.review.ReviewStateRepository
import com.romreviewertools.noteitup.domain.model.DiaryEntry
import com.romreviewertools.noteitup.domain.model.ImageAttachment
import com.romreviewertools.noteitup.domain.model.Location
import com.romreviewertools.noteitup.domain.model.Mood
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import com.romreviewertools.noteitup.domain.usecase.CreateEntryUseCase
import com.romreviewertools.noteitup.domain.usecase.GetAllFoldersUseCase
import com.romreviewertools.noteitup.domain.usecase.GetAllTagsUseCase
import com.romreviewertools.noteitup.domain.usecase.GetEntryByIdUseCase
import com.romreviewertools.noteitup.domain.usecase.ImproveTextUseCase
import com.romreviewertools.noteitup.domain.usecase.UpdateEntryUseCase
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
    private val improveTextUseCase: ImproveTextUseCase,
    private val repository: DiaryRepository,
    private val imagePicker: ImagePicker,
    private val locationService: LocationService,
    private val reviewStateRepository: ReviewStateRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var originalEntry: DiaryEntry? = null
    private var originalTagIds: Set<String> = emptySet()
    private var originalFolderId: String? = null
    private var originalImages: List<ImageAttachment> = emptyList()
    private var originalLocation: Location? = null

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
            // AI intents
            is EditorIntent.ImproveText -> improveText(intent.improvementType)
            is EditorIntent.DismissAISuggestion -> dismissAISuggestion()
            is EditorIntent.AcceptAISuggestion -> acceptAISuggestion()
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
                    originalLocation = entry.location
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
        updateState { it.copy(title = title) }
    }

    private fun updateContent(content: String) {
        updateState { it.copy(content = content) }
    }

    private fun updateMood(mood: Mood?) {
        updateState { it.copy(mood = mood) }
    }

    private fun toggleTag(tagId: String) {
        updateState { state ->
            val newSelectedTags = if (tagId in state.selectedTagIds) {
                state.selectedTagIds - tagId
            } else {
                state.selectedTagIds + tagId
            }
            state.copy(selectedTagIds = newSelectedTags)
        }
    }

    private fun selectFolder(folderId: String?) {
        updateState { it.copy(selectedFolderId = folderId) }
    }

    private fun toggleFavorite() {
        updateState { it.copy(isFavorite = !it.isFavorite) }
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

                    // Track analytics
                    val event = if (currentState.isNewEntry) {
                        AnalyticsEvent.EntryCreated
                    } else {
                        AnalyticsEvent.EntrySaved
                    }
                    analyticsService.logEvent(event)

                    // Track mood if selected
                    currentState.mood?.let { mood ->
                        analyticsService.logEvent(AnalyticsEvent.MoodSelected(mood.name))
                    }

                    // Increment entry save count and check for review prompt
                    reviewStateRepository.incrementEntriesSavedCount()
                    val shouldShowReview = reviewStateRepository.shouldShowReviewPrompt()

                    if (shouldShowReview) {
                        // Mark prompt as shown to avoid showing again too soon
                        reviewStateRepository.recordPromptShown()
                        analyticsService.logEvent(AnalyticsEvent.ReviewPromptShown)
                    }

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true,
                            shouldShowReviewPrompt = shouldShowReview
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

    /**
     * Called after review prompt is handled (shown or dismissed)
     */
    fun onReviewPromptHandled() {
        _uiState.update { it.copy(shouldShowReviewPrompt = false) }
    }

    /**
     * Called when user completes the review flow
     */
    fun onReviewCompleted() {
        viewModelScope.launch {
            reviewStateRepository.setHasRated(true)
            analyticsService.logEvent(AnalyticsEvent.ReviewCompleted)
        }
        _uiState.update { it.copy(shouldShowReviewPrompt = false) }
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

                    updateState { state ->
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
        updateState { state ->
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

                    updateState {
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
        updateState { it.copy(location = null) }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun improveText(improvementType: com.romreviewertools.noteitup.data.ai.ImprovementType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImprovingText = true, aiError = null, aiSuggestion = null) }

            try {
                val textToImprove = _uiState.value.content
                if (textToImprove.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isImprovingText = false,
                            aiError = "Please enter some text first"
                        )
                    }
                    return@launch
                }

                val result = improveTextUseCase(textToImprove, improvementType)

                _uiState.update {
                    it.copy(
                        isImprovingText = false,
                        aiSuggestion = result.getOrNull(),
                        aiError = result.exceptionOrNull()?.message
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImprovingText = false,
                        aiError = e.message ?: "Failed to improve text"
                    )
                }
            }
        }
    }

    private fun dismissAISuggestion() {
        _uiState.update { it.copy(aiSuggestion = null, aiError = null) }
    }

    private fun acceptAISuggestion() {
        val suggestion = _uiState.value.aiSuggestion
        if (suggestion != null) {
            updateState {
                it.copy(
                    content = suggestion,
                    aiSuggestion = null,
                    aiError = null
                )
            }
        }
    }

    private fun updateState(update: (EditorUiState) -> EditorUiState) {
        _uiState.update { currentState ->
            val newState = update(currentState)
            val hasChanges = checkForChanges(newState)
            println("DEBUG: EditorViewModel - updateState. hasChanges: $hasChanges") 
            newState.copy(hasUnsavedChanges = hasChanges)
        }
    }

    private fun checkForChanges(state: EditorUiState): Boolean {
        if (state.isNewEntry) {
            return state.title.isNotBlank() ||
                    state.content.isNotBlank() ||
                    state.mood != null ||
                    state.selectedFolderId != null ||
                    state.selectedTagIds.isNotEmpty() ||
                    state.images.isNotEmpty() ||
                    state.location != null
        }

        return state.title != (originalEntry?.title ?: "") ||
                state.content != (originalEntry?.content ?: "") ||
                state.mood != originalEntry?.mood ||
                state.isFavorite != (originalEntry?.isFavorite ?: false) ||
                state.selectedFolderId != originalFolderId ||
                state.selectedTagIds != originalTagIds ||
                state.location != originalLocation ||
                state.images != originalImages
    }
}
