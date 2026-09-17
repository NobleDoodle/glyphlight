package com.glyphlight.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SleepTimerDialog(
    initialMinutes: Int,
    accentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var minutes by remember { mutableIntStateOf(initialMinutes.coerceIn(1, 180)) }

    val shape = RoundedCornerShape(24.dp)
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(1.5.dp, accentColor, shape),
        shape = shape,
        containerColor = Color.Black,
        title = {
            Text(
                text = "SLEEP TIMER",
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BedGlyph(
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(56.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
                ) {
                    StepperButton(
                        symbol = "−",
                        enabled = minutes > 1,
                        onClick = { minutes = (minutes - 1).coerceIn(1, 180) },
                    )
                    Text(
                        text = "$minutes min",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    StepperButton(
                        symbol = "+",
                        enabled = minutes < 180,
                        onClick = { minutes = (minutes + 1).coerceIn(1, 180) },
                    )
                }
                Text(
                    text = "The flashlight will turn off after the specified time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(minutes) },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            ) {
                Text("SET")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        },
    )
}

@Composable
private fun StepperButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(44.dp),
    ) {
        Text(text = symbol, style = MaterialTheme.typography.titleLarge)
    }
}
