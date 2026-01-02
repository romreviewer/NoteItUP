package com.romreviewertools.noteitup.domain.model

data class DetailedStats(
    val totalEntries: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWords: Int = 0,
    val averageWordsPerEntry: Int = 0,
    val moodDistribution: Map<Mood, Int> = emptyMap(),
    val entriesByMonth: List<MonthlyEntryCount> = emptyList(),
    val writingDays: Int = 0,
    val favoriteCount: Int = 0
)

data class MonthlyEntryCount(
    val yearMonth: String,  // Format: "2024-01"
    val count: Int
)
