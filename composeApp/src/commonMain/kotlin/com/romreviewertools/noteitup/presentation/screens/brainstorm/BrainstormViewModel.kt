package com.romreviewertools.noteitup.presentation.screens.brainstorm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.data.ai.ChatMessage
import com.romreviewertools.noteitup.data.analytics.AnalyticsEvent
import com.romreviewertools.noteitup.data.analytics.AnalyticsService
import com.romreviewertools.noteitup.data.repository.AISettingsRepository
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import com.romreviewertools.noteitup.domain.usecase.ChatUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Brainstorm Chat screen
 */
class BrainstormViewModel(
    private val chatUseCase: ChatUseCase,
    private val aiSettingsRepository: AISettingsRepository,
    private val analyticsService: AnalyticsService,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrainstormUiState())
    val uiState: StateFlow<BrainstormUiState> = _uiState.asStateFlow()

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenViewBrainstorm)
        observeAISettings()
        observeBrainstormMessages()
    }

    private fun observeBrainstormMessages() {
        viewModelScope.launch {
            diaryRepository.getBrainstormMessages().collect { messages ->
                _uiState.update { state ->
                    state.copy(
                        messages = messages.map { msg ->
                            ChatMessageUi(
                                id = msg.id,
                                content = msg.content,
                                isUser = msg.isUser,
                                timestamp = msg.timestamp
                            )
                        }
                    )
                }
            }
        }
    }

    private fun observeAISettings() {
        viewModelScope.launch {
            aiSettingsRepository.aiSettings.collect { settings ->
                val isConfigured = settings.enabled && settings.apiKey.isNotBlank()
                _uiState.update { it.copy(isAIConfigured = isConfigured) }
            }
        }
    }

    private fun checkAIConfiguration() {
        viewModelScope.launch {
            val isConfigured = chatUseCase.isConfigured()
            _uiState.update { it.copy(isAIConfigured = isConfigured) }
        }
    }

    fun onIntent(intent: BrainstormIntent) {
        when (intent) {
            is BrainstormIntent.SendMessage -> sendMessage(intent.message)
            is BrainstormIntent.UseStarterPrompt -> sendMessage(intent.prompt)
            is BrainstormIntent.ClearChat -> clearChat()
            is BrainstormIntent.DismissError -> dismissError()
            is BrainstormIntent.CopyToClipboard -> copyToClipboard(intent.text)
            is BrainstormIntent.InsertToEntry -> insertToEntry(intent.text)
            is BrainstormIntent.RefreshConfiguration -> checkAIConfiguration()
        }
    }

    private fun sendMessage(message: String) {
        if (message.isBlank()) return

        analyticsService.logEvent(AnalyticsEvent.BrainstormMessageSent)

        viewModelScope.launch {
            val userTimestamp = System.currentTimeMillis()
            val userMessageId = userTimestamp.toString()

            // Persist user message (Flow will update UI)
            diaryRepository.insertBrainstormMessage(
                id = userMessageId,
                content = message,
                isUser = true,
                timestamp = userTimestamp
            )

            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            // Build conversation history for API (exclude the message we just added)
            val conversationHistory = _uiState.value.messages
                .filter { it.id != userMessageId }
                .map { msg ->
                    ChatMessage(
                        role = if (msg.isUser) "user" else "assistant",
                        content = msg.content
                    )
                }

            // Get AI response
            val result = chatUseCase.sendMessage(message, conversationHistory)

            result.fold(
                onSuccess = { response ->
                    val assistantTimestamp = System.currentTimeMillis()
                    diaryRepository.insertBrainstormMessage(
                        id = assistantTimestamp.toString(),
                        content = response,
                        isUser = false,
                        timestamp = assistantTimestamp
                    )
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An error occurred"
                        )
                    }
                }
            )
        }
    }

    private fun clearChat() {
        viewModelScope.launch {
            diaryRepository.deleteAllBrainstormMessages()
            _uiState.update { it.copy(error = null) }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun copyToClipboard(text: String) {
        _uiState.update { it.copy(textToCopy = text) }
    }

    fun clearCopyText() {
        _uiState.update { it.copy(textToCopy = null) }
    }

    private fun insertToEntry(text: String) {
        _uiState.update { it.copy(textToInsert = text) }
    }

    fun clearInsertText() {
        _uiState.update { it.copy(textToInsert = null) }
    }
}

/**
 * UI state for Brainstorm screen
 */
data class BrainstormUiState(
    val messages: List<ChatMessageUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAIConfigured: Boolean = true,
    val textToCopy: String? = null,
    val textToInsert: String? = null
)

/**
 * UI representation of a chat message
 */
data class ChatMessageUi(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long
)

/**
 * Intents for Brainstorm screen
 */
sealed interface BrainstormIntent {
    data class SendMessage(val message: String) : BrainstormIntent
    data class UseStarterPrompt(val prompt: String) : BrainstormIntent
    data object ClearChat : BrainstormIntent
    data object DismissError : BrainstormIntent
    data class CopyToClipboard(val text: String) : BrainstormIntent
    data class InsertToEntry(val text: String) : BrainstormIntent
    data object RefreshConfiguration : BrainstormIntent
}