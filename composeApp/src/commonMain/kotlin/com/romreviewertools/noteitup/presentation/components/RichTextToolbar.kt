package com.romreviewertools.noteitup.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState

@Composable
fun RichTextToolbar(
    richTextState: RichTextState,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Bold
        FormatButton(
            icon = Icons.Default.FormatBold,
            contentDescription = "Bold",
            isActive = richTextState.currentSpanStyle.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold,
            onClick = {
                richTextState.toggleSpanStyle(
                    androidx.compose.ui.text.SpanStyle(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }
        )

        // Italic
        FormatButton(
            icon = Icons.Default.FormatItalic,
            contentDescription = "Italic",
            isActive = richTextState.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic,
            onClick = {
                richTextState.toggleSpanStyle(
                    androidx.compose.ui.text.SpanStyle(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }
        )

        // Underline
        FormatButton(
            icon = Icons.Default.FormatUnderlined,
            contentDescription = "Underline",
            isActive = richTextState.currentSpanStyle.textDecoration?.contains(androidx.compose.ui.text.style.TextDecoration.Underline) == true,
            onClick = {
                richTextState.toggleSpanStyle(
                    androidx.compose.ui.text.SpanStyle(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                )
            }
        )

        // Clear Formatting
        TextFormatButton(
            text = "Clear",
            contentDescription = "Clear Formatting",
            onClick = {
                // Remove all span styles by toggling them off
                val currentStyle = richTextState.currentSpanStyle

                // Remove bold if active
                if (currentStyle.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold) {
                    richTextState.toggleSpanStyle(
                        androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }

                // Remove italic if active
                if (currentStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic) {
                    richTextState.toggleSpanStyle(
                        androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                }

                // Remove underline if active
                if (currentStyle.textDecoration?.contains(androidx.compose.ui.text.style.TextDecoration.Underline) == true) {
                    richTextState.toggleSpanStyle(
                        androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                    )
                }

                // Reset to default font size (removes heading)
                if (currentStyle.fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) {
                    richTextState.removeSpanStyle(
                        androidx.compose.ui.text.SpanStyle(fontSize = currentStyle.fontSize)
                    )
                }
            }
        )

        // H1
        TextFormatButton(
            text = "H1",
            contentDescription = "Heading 1",
            isActive = richTextState.currentSpanStyle.fontSize == 24.sp,
            onClick = {
                richTextState.addSpanStyle(
                    androidx.compose.ui.text.SpanStyle(
                        fontSize = 24.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }
        )

        // H2
        TextFormatButton(
            text = "H2",
            contentDescription = "Heading 2",
            isActive = richTextState.currentSpanStyle.fontSize == 20.sp,
            onClick = {
                richTextState.addSpanStyle(
                    androidx.compose.ui.text.SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }
        )

        // H3
        TextFormatButton(
            text = "H3",
            contentDescription = "Heading 3",
            isActive = richTextState.currentSpanStyle.fontSize == 18.sp,
            onClick = {
                richTextState.addSpanStyle(
                    androidx.compose.ui.text.SpanStyle(
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }
        )

        // Color Picker Toggle
        FormatButton(
            icon = Icons.Default.FormatColorText,
            contentDescription = "Text Color",
            isActive = showColorPicker,
            onClick = { showColorPicker = !showColorPicker }
        )

        // Color options (shown when color picker is active)
        if (showColorPicker) {
            ColorButton(
                color = Color.Red,
                onClick = {
                    richTextState.addSpanStyle(androidx.compose.ui.text.SpanStyle(color = Color.Red))
                }
            )
            ColorButton(
                color = Color.Blue,
                onClick = {
                    richTextState.addSpanStyle(androidx.compose.ui.text.SpanStyle(color = Color.Blue))
                }
            )
            ColorButton(
                color = Color(0xFF00AA00),
                onClick = {
                    richTextState.addSpanStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF00AA00)))
                }
            )
            ColorButton(
                color = Color(0xFFFFAA00),
                onClick = {
                    richTextState.addSpanStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFFFFAA00)))
                }
            )
            ColorButton(
                color = Color.Magenta,
                onClick = {
                    richTextState.addSpanStyle(androidx.compose.ui.text.SpanStyle(color = Color.Magenta))
                }
            )
            ColorButton(
                color = Color.Cyan,
                onClick = {
                    richTextState.addSpanStyle(androidx.compose.ui.text.SpanStyle(color = Color.Cyan))
                }
            )
            ColorButton(
                color = Color.Black,
                onClick = {
                    richTextState.addSpanStyle(androidx.compose.ui.text.SpanStyle(color = Color.Black))
                }
            )
        }

        // Unordered List
        FormatButton(
            icon = Icons.Default.FormatListBulleted,
            contentDescription = "Bullet List",
            isActive = richTextState.isUnorderedList,
            onClick = { richTextState.toggleUnorderedList() }
        )

        // Ordered List
        FormatButton(
            icon = Icons.Default.FormatListNumbered,
            contentDescription = "Numbered List",
            isActive = richTextState.isOrderedList,
            onClick = { richTextState.toggleOrderedList() }
        )

        // Code
        FormatButton(
            icon = Icons.Default.FormatQuote,
            contentDescription = "Code",
            isActive = richTextState.isCodeSpan,
            onClick = { richTextState.toggleCodeSpan() }
        )
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isActive) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
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
private fun TextFormatButton(
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isActive) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ColorButton(
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Empty box with color background
    }
}
