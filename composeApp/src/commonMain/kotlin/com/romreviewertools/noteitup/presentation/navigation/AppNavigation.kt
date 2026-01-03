package com.romreviewertools.noteitup.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.romreviewertools.noteitup.presentation.screens.allentries.AllEntriesScreen
import com.romreviewertools.noteitup.presentation.screens.allentries.AllEntriesViewModel
import com.romreviewertools.noteitup.presentation.screens.calendar.CalendarScreen
import com.romreviewertools.noteitup.presentation.screens.calendar.CalendarViewModel
import com.romreviewertools.noteitup.presentation.screens.editor.EditorScreen
import com.romreviewertools.noteitup.presentation.screens.editor.EditorViewModel
import com.romreviewertools.noteitup.presentation.screens.folders.FoldersScreen
import com.romreviewertools.noteitup.presentation.screens.folders.FoldersViewModel
import com.romreviewertools.noteitup.presentation.screens.home.HomeScreen
import com.romreviewertools.noteitup.presentation.screens.home.HomeViewModel
import com.romreviewertools.noteitup.presentation.screens.search.SearchScreen
import com.romreviewertools.noteitup.presentation.screens.search.SearchViewModel
import com.romreviewertools.noteitup.presentation.screens.settings.SettingsScreen
import com.romreviewertools.noteitup.presentation.screens.settings.SettingsViewModel
import com.romreviewertools.noteitup.presentation.screens.export.ExportScreen
import com.romreviewertools.noteitup.presentation.screens.export.ExportViewModel
import com.romreviewertools.noteitup.presentation.screens.statistics.StatisticsScreen
import com.romreviewertools.noteitup.presentation.screens.statistics.StatisticsViewModel
import com.romreviewertools.noteitup.presentation.screens.tags.TagsScreen
import com.romreviewertools.noteitup.presentation.screens.tags.TagsViewModel
import com.romreviewertools.noteitup.presentation.screens.security.SecurityIntent
import com.romreviewertools.noteitup.presentation.screens.security.SecuritySettingsScreen
import com.romreviewertools.noteitup.presentation.screens.security.SecurityViewModel
import com.romreviewertools.noteitup.presentation.screens.cloudsync.CloudSyncScreen
import com.romreviewertools.noteitup.presentation.screens.cloudsync.CloudSyncViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        modifier = modifier
    ) {
        composable<Routes.Home> {
            val viewModel = koinViewModel<HomeViewModel>()
            val settingsViewModel = koinViewModel<SettingsViewModel>()
            HomeScreen(
                viewModel = viewModel,
                settingsViewModel = settingsViewModel,
                onEntryClick = { entryId ->
                    navController.navigate(Routes.EditEntry(entryId))
                },
                onNewEntryClick = {
                    navController.navigate(Routes.NewEntry)
                },
                onSeeAllClick = {
                    navController.navigate(Routes.AllEntries)
                },
                onSearchClick = {
                    navController.navigate(Routes.Search)
                },
                onTagsClick = {
                    navController.navigate(Routes.Tags)
                },
                onFoldersClick = {
                    navController.navigate(Routes.Folders)
                },
                onCalendarClick = {
                    navController.navigate(Routes.Calendar)
                },
                onExportClick = {
                    navController.navigate(Routes.Export)
                },
                onStatisticsClick = {
                    navController.navigate(Routes.Statistics)
                },
                onSecurityClick = {
                    navController.navigate(Routes.Security)
                },
                onCloudSyncClick = {
                    navController.navigate(Routes.CloudSync)
                }
            )
        }

        composable<Routes.AllEntries> {
            val viewModel = koinViewModel<AllEntriesViewModel>()
            AllEntriesScreen(
                viewModel = viewModel,
                onEntryClick = { entryId ->
                    navController.navigate(Routes.EditEntry(entryId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.NewEntry> {
            val viewModel = koinViewModel<EditorViewModel>()
            EditorScreen(
                viewModel = viewModel,
                entryId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.EditEntry> { backStackEntry ->
            val route: Routes.EditEntry = backStackEntry.toRoute()
            val viewModel = koinViewModel<EditorViewModel>()
            EditorScreen(
                viewModel = viewModel,
                entryId = route.entryId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.Search> {
            val viewModel = koinViewModel<SearchViewModel>()
            SearchScreen(
                viewModel = viewModel,
                onEntryClick = { entryId ->
                    navController.navigate(Routes.EditEntry(entryId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.Tags> {
            val viewModel = koinViewModel<TagsViewModel>()
            TagsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.Folders> {
            val viewModel = koinViewModel<FoldersViewModel>()
            FoldersScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.Calendar> {
            val viewModel = koinViewModel<CalendarViewModel>()
            CalendarScreen(
                viewModel = viewModel,
                onEntryClick = { entryId ->
                    navController.navigate(Routes.EditEntry(entryId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.Settings> {
            val viewModel = koinViewModel<SettingsViewModel>()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onExportClick = {
                    navController.navigate(Routes.Export)
                },
                onCloudSyncClick = {
                    navController.navigate(Routes.CloudSync)
                }
            )
        }

        composable<Routes.Statistics> {
            val viewModel = koinViewModel<StatisticsViewModel>()
            StatisticsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.Export> {
            val viewModel = koinViewModel<ExportViewModel>()
            ExportScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.Security> {
            val viewModel = koinViewModel<SecurityViewModel>()
            SecuritySettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onBiometricSetup = {
                    // Authenticate with biometric first, then enable it
                    viewModel.authenticateWithBiometric()
                    viewModel.processIntent(SecurityIntent.SetBiometricEnabled(true))
                }
            )
        }

        composable<Routes.CloudSync> {
            val viewModel = koinViewModel<CloudSyncViewModel>()
            CloudSyncScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
