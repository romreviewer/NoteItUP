package com.romreviewertools.noteitup.presentation.screens.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.romreviewertools.noteitup.data.security.BiometricType
import com.romreviewertools.noteitup.domain.model.LockType

@Composable
fun LockScreen(
    viewModel: SecurityViewModel,
    onBiometricRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Auto-trigger biometric authentication on launch when available and enabled
    LaunchedEffect(uiState.biometricAvailable, uiState.biometricEnabled, uiState.isLocked) {
        if (uiState.isLocked &&
            uiState.biometricAvailable &&
            uiState.biometricEnabled &&
            !uiState.isSettingUp &&
            (uiState.lockType == LockType.BIOMETRIC || uiState.lockType == LockType.PIN_AND_BIOMETRIC)
        ) {
            onBiometricRequest()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon/logo placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (uiState.isSettingUp) {
                    when (uiState.setupStep) {
                        SetupStep.ENTER_PIN -> "Create PIN"
                        SetupStep.CONFIRM_PIN -> "Confirm PIN"
                    }
                } else {
                    "Enter PIN"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (uiState.isSettingUp) {
                    when (uiState.setupStep) {
                        SetupStep.ENTER_PIN -> "Enter a 4-6 digit PIN"
                        SetupStep.CONFIRM_PIN -> "Re-enter your PIN"
                    }
                } else {
                    "Enter your PIN to unlock"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN dots indicator
            val pinLength = if (uiState.isSettingUp) {
                when (uiState.setupStep) {
                    SetupStep.ENTER_PIN -> uiState.setupPin.length
                    SetupStep.CONFIRM_PIN -> uiState.confirmPin.length
                }
            } else {
                uiState.pinEntry.length
            }

            PinDotsIndicator(
                length = pinLength,
                maxLength = 6
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Error message
            uiState.pinError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // PIN pad
            PinPad(
                onDigitClick = { digit ->
                    viewModel.processIntent(SecurityIntent.EnterPinDigit(digit))
                },
                onDeleteClick = {
                    viewModel.processIntent(SecurityIntent.DeletePinDigit)
                },
                onBiometricClick = if (!uiState.isSettingUp &&
                    uiState.biometricAvailable &&
                    (uiState.lockType == LockType.BIOMETRIC || uiState.lockType == LockType.PIN_AND_BIOMETRIC)
                ) {
                    onBiometricRequest
                } else null,
                biometricType = uiState.biometricType
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            TextButton(
                onClick = { viewModel.processIntent(SecurityIntent.SubmitPin) },
                enabled = pinLength >= 4
            ) {
                Text(
                    text = if (uiState.isSettingUp) {
                        when (uiState.setupStep) {
                            SetupStep.ENTER_PIN -> "Next"
                            SetupStep.CONFIRM_PIN -> "Confirm"
                        }
                    } else {
                        "Unlock"
                    }
                )
            }

            if (uiState.isSettingUp) {
                TextButton(
                    onClick = { viewModel.processIntent(SecurityIntent.CancelPinSetup) }
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun PinDotsIndicator(
    length: Int,
    maxLength: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(maxLength) { index ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < length) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (index < length) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PinPad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: (() -> Unit)?,
    biometricType: BiometricType = BiometricType.NONE,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row 1: 1 2 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PinButton(text = "1", onClick = { onDigitClick("1") })
            PinButton(text = "2", onClick = { onDigitClick("2") })
            PinButton(text = "3", onClick = { onDigitClick("3") })
        }

        // Row 2: 4 5 6
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PinButton(text = "4", onClick = { onDigitClick("4") })
            PinButton(text = "5", onClick = { onDigitClick("5") })
            PinButton(text = "6", onClick = { onDigitClick("6") })
        }

        // Row 3: 7 8 9
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PinButton(text = "7", onClick = { onDigitClick("7") })
            PinButton(text = "8", onClick = { onDigitClick("8") })
            PinButton(text = "9", onClick = { onDigitClick("9") })
        }

        // Row 4: Biometric/Empty 0 Delete
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                if (onBiometricClick != null) {
                    IconButton(
                        onClick = onBiometricClick,
                        modifier = Modifier.size(72.dp)
                    ) {
                        val icon = when (biometricType) {
                            BiometricType.FACE -> Icons.Default.Face
                            else -> Icons.Default.Fingerprint
                        }
                        val description = when (biometricType) {
                            BiometricType.FACE -> "Use Face ID"
                            BiometricType.FINGERPRINT -> "Use fingerprint"
                            else -> "Use biometric"
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = description,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            PinButton(text = "0", onClick = { onDigitClick("0") })

            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Delete",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun PinButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
