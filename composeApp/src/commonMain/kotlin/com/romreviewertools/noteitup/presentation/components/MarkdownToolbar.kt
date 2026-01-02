package com.romreviewertools.noteitup.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

enum class MarkdownFormat {
    BOLD,
    ITALIC,
    HEADING1,
    HEADING2,
    BULLET_LIST,
    NUMBERED_LIST,
    QUOTE,
    CODE,
    LINK
}

@Composable
fun MarkdownToolbar(
    onFormatClick: (MarkdownFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            ToolbarButton(
                icon = Icons.Default.FormatBold,
                contentDescription = "Bold",
                onClick = { onFormatClick(MarkdownFormat.BOLD) }
            )

            ToolbarButton(
                icon = Icons.Default.FormatItalic,
                contentDescription = "Italic",
                onClick = { onFormatClick(MarkdownFormat.ITALIC) }
            )

            ToolbarDivider()

            ToolbarTextButton(
                text = "H1",
                contentDescription = "Heading 1",
                onClick = { onFormatClick(MarkdownFormat.HEADING1) }
            )

            ToolbarTextButton(
                text = "H2",
                contentDescription = "Heading 2",
                onClick = { onFormatClick(MarkdownFormat.HEADING2) }
            )

            ToolbarDivider()

            ToolbarButton(
                icon = Icons.Default.FormatListBulleted,
                contentDescription = "Bullet List",
                onClick = { onFormatClick(MarkdownFormat.BULLET_LIST) }
            )

            ToolbarButton(
                icon = Icons.Default.FormatListNumbered,
                contentDescription = "Numbered List",
                onClick = { onFormatClick(MarkdownFormat.NUMBERED_LIST) }
            )

            ToolbarDivider()

            ToolbarButton(
                icon = Icons.Default.FormatQuote,
                contentDescription = "Quote",
                onClick = { onFormatClick(MarkdownFormat.QUOTE) }
            )

            ToolbarButton(
                icon = Icons.Default.Code,
                contentDescription = "Code",
                onClick = { onFormatClick(MarkdownFormat.CODE) }
            )

            ToolbarButton(
                icon = Icons.Default.Link,
                contentDescription = "Link",
                onClick = { onFormatClick(MarkdownFormat.LINK) }
            )

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ToolbarTextButton(
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ToolbarDivider() {
    Spacer(modifier = Modifier.width(4.dp))
}

/**
 * Applies markdown formatting to the given text field value.
 * Returns the new TextFieldValue with the formatting applied.
 */
fun applyMarkdownFormat(
    textFieldValue: TextFieldValue,
    format: MarkdownFormat
): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection
    val selectedText = if (selection.collapsed) "" else text.substring(selection.min, selection.max)

    return when (format) {
        MarkdownFormat.BOLD -> {
            wrapSelection(textFieldValue, "**", "**", "bold text")
        }
        MarkdownFormat.ITALIC -> {
            wrapSelection(textFieldValue, "*", "*", "italic text")
        }
        MarkdownFormat.HEADING1 -> {
            insertAtLineStart(textFieldValue, "# ")
        }
        MarkdownFormat.HEADING2 -> {
            insertAtLineStart(textFieldValue, "## ")
        }
        MarkdownFormat.BULLET_LIST -> {
            insertAtLineStart(textFieldValue, "- ")
        }
        MarkdownFormat.NUMBERED_LIST -> {
            insertAtLineStart(textFieldValue, "1. ")
        }
        MarkdownFormat.QUOTE -> {
            insertAtLineStart(textFieldValue, "> ")
        }
        MarkdownFormat.CODE -> {
            if (selectedText.contains("\n")) {
                wrapSelection(textFieldValue, "```\n", "\n```", "code")
            } else {
                wrapSelection(textFieldValue, "`", "`", "code")
            }
        }
        MarkdownFormat.LINK -> {
            if (selectedText.isBlank()) {
                val newText = text.substring(0, selection.min) +
                        "[link text](url)" +
                        text.substring(selection.max)
                TextFieldValue(
                    text = newText,
                    selection = TextRange(selection.min + 1, selection.min + 10)
                )
            } else {
                val newText = text.substring(0, selection.min) +
                        "[$selectedText](url)" +
                        text.substring(selection.max)
                TextFieldValue(
                    text = newText,
                    selection = TextRange(
                        selection.min + selectedText.length + 3,
                        selection.min + selectedText.length + 6
                    )
                )
            }
        }
    }
}

private fun wrapSelection(
    textFieldValue: TextFieldValue,
    prefix: String,
    suffix: String,
    placeholder: String
): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection

    return if (selection.collapsed) {
        // No selection, insert placeholder with formatting
        val newText = text.substring(0, selection.min) +
                prefix + placeholder + suffix +
                text.substring(selection.max)
        TextFieldValue(
            text = newText,
            selection = TextRange(
                selection.min + prefix.length,
                selection.min + prefix.length + placeholder.length
            )
        )
    } else {
        // Wrap selected text
        val selectedText = text.substring(selection.min, selection.max)
        val newText = text.substring(0, selection.min) +
                prefix + selectedText + suffix +
                text.substring(selection.max)
        TextFieldValue(
            text = newText,
            selection = TextRange(
                selection.min + prefix.length,
                selection.min + prefix.length + selectedText.length
            )
        )
    }
}

private fun insertAtLineStart(
    textFieldValue: TextFieldValue,
    prefix: String
): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection

    // Find the start of the current line
    val lineStart = text.lastIndexOf('\n', selection.min - 1) + 1

    val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
    val newCursorPos = selection.min + prefix.length

    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursorPos)
    )
}
