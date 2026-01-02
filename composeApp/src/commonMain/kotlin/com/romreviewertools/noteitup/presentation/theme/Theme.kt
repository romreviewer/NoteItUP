package com.romreviewertools.noteitup.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.romreviewertools.noteitup.domain.model.AccentColor
import com.romreviewertools.noteitup.domain.model.AppPreferences
import com.romreviewertools.noteitup.domain.model.FontSize
import com.romreviewertools.noteitup.domain.model.ThemeMode

// Composition local for font scale
val LocalFontScale = staticCompositionLocalOf { 1.0f }

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant
)

private fun createLightColorScheme(accentColor: AccentColor): ColorScheme {
    if (accentColor == AccentColor.DEFAULT) return LightColorScheme

    val primary = Color(accentColor.colorValue)
    val primaryContainer = primary.copy(alpha = 0.2f)

    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primaryContainer,
        onPrimaryContainer = primary,
        secondary = md_theme_light_secondary,
        onSecondary = md_theme_light_onSecondary,
        secondaryContainer = md_theme_light_secondaryContainer,
        onSecondaryContainer = md_theme_light_onSecondaryContainer,
        tertiary = md_theme_light_tertiary,
        onTertiary = md_theme_light_onTertiary,
        tertiaryContainer = md_theme_light_tertiaryContainer,
        onTertiaryContainer = md_theme_light_onTertiaryContainer,
        error = md_theme_light_error,
        onError = md_theme_light_onError,
        errorContainer = md_theme_light_errorContainer,
        onErrorContainer = md_theme_light_onErrorContainer,
        background = md_theme_light_background,
        onBackground = md_theme_light_onBackground,
        surface = md_theme_light_surface,
        onSurface = md_theme_light_onSurface,
        surfaceVariant = md_theme_light_surfaceVariant,
        onSurfaceVariant = md_theme_light_onSurfaceVariant,
        outline = md_theme_light_outline,
        outlineVariant = md_theme_light_outlineVariant
    )
}

private fun createDarkColorScheme(accentColor: AccentColor): ColorScheme {
    if (accentColor == AccentColor.DEFAULT) return DarkColorScheme

    val primary = Color(accentColor.colorValue)
    val primaryLight = primary.copy(alpha = 0.8f)
    val primaryContainer = primary.copy(alpha = 0.3f)

    return darkColorScheme(
        primary = primaryLight,
        onPrimary = Color.Black,
        primaryContainer = primaryContainer,
        onPrimaryContainer = primaryLight,
        secondary = md_theme_dark_secondary,
        onSecondary = md_theme_dark_onSecondary,
        secondaryContainer = md_theme_dark_secondaryContainer,
        onSecondaryContainer = md_theme_dark_onSecondaryContainer,
        tertiary = md_theme_dark_tertiary,
        onTertiary = md_theme_dark_onTertiary,
        tertiaryContainer = md_theme_dark_tertiaryContainer,
        onTertiaryContainer = md_theme_dark_onTertiaryContainer,
        error = md_theme_dark_error,
        onError = md_theme_dark_onError,
        errorContainer = md_theme_dark_errorContainer,
        onErrorContainer = md_theme_dark_onErrorContainer,
        background = md_theme_dark_background,
        onBackground = md_theme_dark_onBackground,
        surface = md_theme_dark_surface,
        onSurface = md_theme_dark_onSurface,
        surfaceVariant = md_theme_dark_surfaceVariant,
        onSurfaceVariant = md_theme_dark_onSurfaceVariant,
        outline = md_theme_dark_outline,
        outlineVariant = md_theme_dark_outlineVariant
    )
}

private fun createScaledTypography(fontSize: FontSize): Typography {
    val scale = fontSize.scaleFactor
    return Typography(
        displayLarge = DiaryTypography.displayLarge.copy(
            fontSize = DiaryTypography.displayLarge.fontSize * scale
        ),
        displayMedium = DiaryTypography.displayMedium.copy(
            fontSize = DiaryTypography.displayMedium.fontSize * scale
        ),
        displaySmall = DiaryTypography.displaySmall.copy(
            fontSize = DiaryTypography.displaySmall.fontSize * scale
        ),
        headlineLarge = DiaryTypography.headlineLarge.copy(
            fontSize = DiaryTypography.headlineLarge.fontSize * scale
        ),
        headlineMedium = DiaryTypography.headlineMedium.copy(
            fontSize = DiaryTypography.headlineMedium.fontSize * scale
        ),
        headlineSmall = DiaryTypography.headlineSmall.copy(
            fontSize = DiaryTypography.headlineSmall.fontSize * scale
        ),
        titleLarge = DiaryTypography.titleLarge.copy(
            fontSize = DiaryTypography.titleLarge.fontSize * scale
        ),
        titleMedium = DiaryTypography.titleMedium.copy(
            fontSize = DiaryTypography.titleMedium.fontSize * scale
        ),
        titleSmall = DiaryTypography.titleSmall.copy(
            fontSize = DiaryTypography.titleSmall.fontSize * scale
        ),
        bodyLarge = DiaryTypography.bodyLarge.copy(
            fontSize = DiaryTypography.bodyLarge.fontSize * scale
        ),
        bodyMedium = DiaryTypography.bodyMedium.copy(
            fontSize = DiaryTypography.bodyMedium.fontSize * scale
        ),
        bodySmall = DiaryTypography.bodySmall.copy(
            fontSize = DiaryTypography.bodySmall.fontSize * scale
        ),
        labelLarge = DiaryTypography.labelLarge.copy(
            fontSize = DiaryTypography.labelLarge.fontSize * scale
        ),
        labelMedium = DiaryTypography.labelMedium.copy(
            fontSize = DiaryTypography.labelMedium.fontSize * scale
        ),
        labelSmall = DiaryTypography.labelSmall.copy(
            fontSize = DiaryTypography.labelSmall.fontSize * scale
        )
    )
}

@Composable
fun DiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DiaryTypography,
        content = content
    )
}

@Composable
fun DiaryTheme(
    preferences: AppPreferences,
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()

    val darkTheme = when (preferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDarkTheme
    }

    val colorScheme = if (darkTheme) {
        createDarkColorScheme(preferences.accentColor)
    } else {
        createLightColorScheme(preferences.accentColor)
    }

    val typography = createScaledTypography(preferences.fontSize)

    CompositionLocalProvider(
        LocalFontScale provides preferences.fontSize.scaleFactor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
