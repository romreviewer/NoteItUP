package com.romreviewertools.noteitup.domain.model

enum class Mood(val emoji: String, val label: String) {
    AMAZING("😄", "Amazing"),
    GOOD("🙂", "Good"),
    NEUTRAL("😐", "Neutral"),
    SAD("😢", "Sad"),
    TERRIBLE("😫", "Terrible")
}
