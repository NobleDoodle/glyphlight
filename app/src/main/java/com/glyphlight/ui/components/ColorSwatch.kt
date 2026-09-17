package com.glyphlight.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A single round color swatch, optionally showing a selection ring or a delete "x" badge. */
@Composable
fun ColorSwatch(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    selected: Boolean = false,
    showDeleteMark: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) {
                    Modifier.border(2.dp, Color.White, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (showDeleteMark) {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.6f),
                )
            }
        }
    }
}

/** A dotted-outline "custom" swatch that opens the full color picker. */
@Composable
fun CustomColorSwatch(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    color: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, color, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        DottedRing(
            modifier = Modifier.size(size * 0.62f),
            color = color,
            dotCount = 16,
            dotRadiusFraction = 0.05f,
        )
    }
}
