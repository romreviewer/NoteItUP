package com.romreviewertools.noteitup.window

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.romreviewertools.noteitup.presentation.screens.aisettings.AISettingsScreen
import com.romreviewertools.noteitup.presentation.screens.aisettings.AISettingsViewModel
import com.romreviewertools.noteitup.presentation.screens.calendar.CalendarScreen
import com.romreviewertools.noteitup.presentation.screens.calendar.CalendarViewModel
import com.romreviewertools.noteitup.presentation.screens.cloudsync.CloudSyncScreen
import com.romreviewertools.noteitup.presentation.screens.cloudsync.CloudSyncViewModel
import com.romreviewertools.noteitup.presentation.screens.editor.EditorScreen
import com.romreviewertools.noteitup.presentation.screens.editor.EditorViewModel
import com.romreviewertools.noteitup.presentation.screens.folders.FoldersScreen
import com.romreviewertools.noteitup.presentation.screens.folders.FoldersViewModel
import com.romreviewertools.noteitup.presentation.screens.search.SearchScreen
import com.romreviewertools.noteitup.presentation.screens.search.SearchViewModel
import com.romreviewertools.noteitup.presentation.screens.statistics.StatisticsScreen
import com.romreviewertools.noteitup.presentation.screens.statistics.StatisticsViewModel
import com.romreviewertools.noteitup.presentation.screens.tags.TagsScreen
import com.romreviewertools.noteitup.presentation.screens.tags.TagsViewModel
import com.romreviewertools.noteitup.presentation.theme.DiaryTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Renders the content for a desktop window
 */
@Composable
fun WindowContent(
    window: DesktopWindow,
    windowManager: WindowManager,
    onClose: () -> Unit
) {
    DiaryTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (window) {
                is DesktopWindow.Editor -> EditorWindowContent(
                    entryId = window.entryId,
                    isNewEntry = window.isNewEntry,
                    windowManager = windowManager,
                    onClose = onClose
                )
                is DesktopWindow.Calendar -> CalendarWindowContent(onClose)
                is DesktopWindow.Statistics -> StatisticsWindowContent(onClose)
                is DesktopWindow.Search -> SearchWindowContent(onClose)
                is DesktopWindow.Tags -> TagsWindowContent(onClose)
                is DesktopWindow.Folders -> FoldersWindowContent(onClose)
                is DesktopWindow.AISettings -> AISettingsWindowContent(onClose)
                is DesktopWindow.CloudSync -> CloudSyncWindowContent(onClose)
            }
        }
    }
}

@Composable
private fun EditorWindowContent(
    entryId: String?,
    isNewEntry: Boolean,
    windowManager: WindowManager,
    onClose: () -> Unit
) {
    val viewModel: EditorViewModel = koinViewModel()

    EditorScreen(
        viewModel = viewModel,
        entryId = entryId,
        onNavigateBack = onClose,
        onNavigateToAISettings = {
            windowManager.openWindow(DesktopWindow.AISettings)
        }
    )
}

@Composable
private fun CalendarWindowContent(onClose: () -> Unit) {
    val viewModel: CalendarViewModel = koinViewModel()

    CalendarScreen(
        viewModel = viewModel,
        onNavigateBack = onClose,
        onEntryClick = { /* Could open in new editor window */ }
    )
}

@Composable
private fun StatisticsWindowContent(onClose: () -> Unit) {
    val viewModel: StatisticsViewModel = koinViewModel()

    StatisticsScreen(
        viewModel = viewModel,
        onNavigateBack = onClose
    )
}

@Composable
private fun SearchWindowContent(onClose: () -> Unit) {
    val viewModel: SearchViewModel = koinViewModel()

    SearchScreen(
        viewModel = viewModel,
        onNavigateBack = onClose,
        onEntryClick = { /* Could open in new editor window */ }
    )
}

@Composable
private fun TagsWindowContent(onClose: () -> Unit) {
    val viewModel: TagsViewModel = koinViewModel()

    TagsScreen(
        viewModel = viewModel,
        onNavigateBack = onClose
    )
}

@Composable
private fun FoldersWindowContent(onClose: () -> Unit) {
    val viewModel: FoldersViewModel = koinViewModel()

    FoldersScreen(
        viewModel = viewModel,
        onNavigateBack = onClose
    )
}

@Composable
private fun AISettingsWindowContent(onClose: () -> Unit) {
    val viewModel: AISettingsViewModel = koinViewModel()

    AISettingsScreen(
        viewModel = viewModel,
        onNavigateBack = onClose
    )
}

@Composable
private fun CloudSyncWindowContent(onClose: () -> Unit) {
    val viewModel: CloudSyncViewModel = koinViewModel()

    CloudSyncScreen(
        viewModel = viewModel,
        onNavigateBack = onClose
    )
}
