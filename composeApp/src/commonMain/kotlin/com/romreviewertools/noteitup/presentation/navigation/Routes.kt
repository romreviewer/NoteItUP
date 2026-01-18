package com.romreviewertools.noteitup.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object Home : Routes

    @Serializable
    data object AllEntries : Routes

    @Serializable
    data object NewEntry : Routes

    @Serializable
    data class EditEntry(val entryId: String) : Routes

    @Serializable
    data object Search : Routes

    @Serializable
    data object Tags : Routes

    @Serializable
    data object Folders : Routes

    @Serializable
    data object Calendar : Routes

    @Serializable
    data object Settings : Routes

    @Serializable
    data object Statistics : Routes

    @Serializable
    data object Export : Routes

    @Serializable
    data object Security : Routes

    @Serializable
    data object CloudSync : Routes

    @Serializable
    data object AISettings : Routes

    @Serializable
    data object Brainstorm : Routes
}
