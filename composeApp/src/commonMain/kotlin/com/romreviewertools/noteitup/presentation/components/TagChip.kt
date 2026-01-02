package com.romreviewertools.noteitup.presentation.components

import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.romreviewertools.noteitup.domain.model.Tag

@Composable
fun TagChip(
    tag: Tag,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    if (onClick != null) {
        FilterChip(
            selected = false,
            onClick = onClick,
            label = { Text("#${tag.name}") },
            modifier = modifier
        )
    } else {
        SuggestionChip(
            onClick = {},
            label = { Text("#${tag.name}", style = MaterialTheme.typography.labelSmall) },
            modifier = modifier
        )
    }
}
