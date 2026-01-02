package com.romreviewertools.noteitup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.romreviewertools.noteitup.domain.model.AppPreferences
import com.romreviewertools.noteitup.domain.model.LockType
import com.romreviewertools.noteitup.domain.repository.PreferencesRepository
import com.romreviewertools.noteitup.presentation.navigation.AppNavigation
import com.romreviewertools.noteitup.presentation.screens.security.LockScreen
import com.romreviewertools.noteitup.presentation.screens.security.SecurityIntent
import com.romreviewertools.noteitup.presentation.screens.security.SecurityViewModel
import com.romreviewertools.noteitup.presentation.theme.DiaryTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    val preferencesRepository = koinInject<PreferencesRepository>()
    val preferences by preferencesRepository.getPreferences()
        .collectAsState(initial = AppPreferences())

    val securityViewModel = koinViewModel<SecurityViewModel>()
    val securityState by securityViewModel.uiState.collectAsState()

    // Check lock status on app launch
    LaunchedEffect(Unit) {
        securityViewModel.processIntent(SecurityIntent.CheckLockStatus)
    }

    DiaryTheme(preferences = preferences) {
        // Show lock screen if app is locked and has PIN set
        if (securityState.isLocked && securityState.hasPinSet) {
            LockScreen(
                viewModel = securityViewModel,
                onBiometricRequest = {
                    securityViewModel.authenticateWithBiometric()
                }
            )
        } else {
            AppNavigation()
        }
    }
}
