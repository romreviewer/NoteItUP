package com.romreviewertools.noteitup.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.romreviewertools.noteitup.presentation.components.DiaryEntryCard
import com.romreviewertools.noteitup.presentation.components.GreetingCard
import com.romreviewertools.noteitup.presentation.components.StatsRow
import com.romreviewertools.noteitup.presentation.screens.settings.SettingsContent
import com.romreviewertools.noteitup.presentation.screens.settings.SettingsViewModel

private enum class BottomNavTab {
    HOME, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    onEntryClick: (String) -> Unit,
    onNewEntryClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTagsClick: () -> Unit,
    onFoldersClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onExportClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onSecurityClick: () -> Unit = {},
    onCloudSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavTab.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (selectedTab == BottomNavTab.HOME) "My Diary" else "Settings")
                },
                actions = {
                    if (selectedTab == BottomNavTab.HOME) {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = onCalendarClick) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar")
                        }
                        IconButton(onClick = onFoldersClick) {
                            Icon(Icons.Default.Folder, contentDescription = "Folders")
                        }
                        IconButton(onClick = onTagsClick) {
                            Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "Tags")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == BottomNavTab.HOME,
                    onClick = { selectedTab = BottomNavTab.HOME }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == BottomNavTab.SETTINGS,
                    onClick = { selectedTab = BottomNavTab.SETTINGS }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == BottomNavTab.HOME) {
                FloatingActionButton(
                    onClick = onNewEntryClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Entry")
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            BottomNavTab.HOME -> {
                HomeContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onEntryClick = onEntryClick,
                    onNewEntryClick = onNewEntryClick,
                    onSeeAllClick = onSeeAllClick,
                    onStatisticsClick = onStatisticsClick,
                    paddingValues = paddingValues,
                    modifier = modifier
                )
            }
            BottomNavTab.SETTINGS -> {
                SettingsContent(
                    viewModel = settingsViewModel,
                    onExportClick = onExportClick,
                    onSecurityClick = onSecurityClick,
                    onCloudSyncClick = onCloudSyncClick,
                    paddingValues = paddingValues
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    onEntryClick: (String) -> Unit,
    onNewEntryClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GreetingCard(userName = uiState.userName)
            }

            item {
                StatsRow(
                    stats = uiState.stats,
                    onClick = onStatisticsClick
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Entries",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onSeeAllClick) {
                        Text("See All")
                    }
                }
            }

            if (uiState.entries.isEmpty()) {
                item {
                    EmptyStateCard(onNewEntryClick = onNewEntryClick)
                }
            } else {
                items(
                    items = uiState.entries,
                    key = { it.id }
                ) { entry ->
                    DiaryEntryCard(
                        entry = entry,
                        onEntryClick = onEntryClick,
                        onFavoriteClick = { viewModel.processIntent(HomeIntent.ToggleFavorite(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    onNewEntryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No entries yet",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start writing your first diary entry",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNewEntryClick) {
                Text("Create Entry")
            }
        }
    }
}
