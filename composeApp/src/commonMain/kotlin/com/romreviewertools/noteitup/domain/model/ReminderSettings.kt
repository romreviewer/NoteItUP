package com.romreviewertools.noteitup.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class ReminderSettings(
    val enabled: Boolean = false,
    val time: LocalTime = LocalTime(20, 0), // 8 PM default
    val daysOfWeek: Set<DayOfWeek> = DayOfWeek.entries.toSet()
)
