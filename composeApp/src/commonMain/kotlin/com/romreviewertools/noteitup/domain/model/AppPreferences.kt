package com.romreviewertools.noteitup.domain.model

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.DEFAULT,
    val fontSize: FontSize = FontSize.MEDIUM
)

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class AccentColor(val colorValue: Long) {
    DEFAULT(0xFF6750A4),  // Material 3 default purple
    BLUE(0xFF1976D2),
    GREEN(0xFF388E3C),
    PURPLE(0xFF7B1FA2),
    ORANGE(0xFFE65100),
    PINK(0xFFC2185B)
}

enum class FontSize(val scaleFactor: Float) {
    SMALL(0.85f),
    MEDIUM(1.0f),
    LARGE(1.15f)
}
